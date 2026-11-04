package com.fast.system.aspect;

import com.fast.system.domain.LoginUser;
import com.fast.system.domain.Role;
import com.fast.system.domain.SysPermissionLog;
import com.fast.system.service.IAsyncLogService;
import com.fast.system.service.IMenuService;
import com.fast.system.service.IRoleService;
import com.fast.system.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
public class PermissionChangeAspect {

    @Resource
    private IRoleService roleService;
    @Resource
    private IMenuService menuService;
    @Resource
    private IAsyncLogService asyncLogService;
    @Resource
    private ObjectMapper objectMapper;

    // 切入点扩展：新增 / 修改 / 删除 三个方法都拦
    @Around("execution(* com.fast.system.service.impl.RoleServiceImpl.insertRole(..)) || " +
            "execution(* com.fast.system.service.impl.RoleServiceImpl.updateRole(..)) || " +
            "execution(* com.fast.system.service.impl.RoleServiceImpl.deleteRoleByRoleIds(..))")
    public Object aroundPermissionChange(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().getName();
        Object[] args = point.getArgs();

        Long roleId = null;
        String roleName = null;
        Long[] newMenuIdsArray = null;
        List<Long> oldMenuIds = new ArrayList<>();

        // 删除角色：参数是 Long[] roleIds，单独取旧权限
        if ("deleteRoleByRoleIds".equals(methodName)) {
            Long[] roleIds = (Long[]) args[0];
            // 简单起见：记录第一个角色；如需逐条记录可循环插入多条日志
            if (roleIds != null && roleIds.length > 0) {
                roleId = roleIds[0];
                Role role = roleService.selectRoleById(roleId);
                if (role != null) {
                    roleName = role.getRoleName();
                }
                oldMenuIds = menuService.selectMenuListByRoleId(roleId);
            }
        } else {
            // 新增 / 修改：第一个参数都是 Role
            Role role = (Role) args[0];
            roleId = role.getRoleId();
            roleName = role.getRoleName();
            newMenuIdsArray = role.getMenuIds();
            if ("insertRole".equals(methodName)) {
                // 新增角色：没有旧权限
                oldMenuIds = new ArrayList<>();
            } else {
                oldMenuIds = menuService.selectMenuListByRoleId(roleId);
            }
        }

        // 放行执行真正的业务方法
        Object result = point.proceed();

        // 删除场景：没有"新权限"，直接记录移除即可
        List<Long> newMenuIds = new ArrayList<>();
        if (newMenuIdsArray != null) {
            newMenuIds.addAll(Arrays.asList(newMenuIdsArray));
        }

        List<Long> addList = new ArrayList<>(newMenuIds);
        addList.removeAll(oldMenuIds);

        List<Long> removeList = new ArrayList<>(oldMenuIds);
        removeList.removeAll(newMenuIds);

        // ★ 权限没变就不记录（updateRole 且勾选未变化时会走这里）
        if (addList.isEmpty() && removeList.isEmpty()) {
            return result;
        }

        LoginUser loginUser = SecurityUtils.getLoginUser();

        SysPermissionLog permLog = new SysPermissionLog();
        if (loginUser != null) {
            permLog.setOperUserId(loginUser.getUserId());
            permLog.setOperUserName(loginUser.getUsername());
        }
        permLog.setTargetRoleId(roleId);
        permLog.setTargetRoleName(roleName);
        permLog.setOldMenus(objectMapper.writeValueAsString(oldMenuIds));
        permLog.setNewMenus(objectMapper.writeValueAsString(newMenuIds));
        permLog.setAddMenus(objectMapper.writeValueAsString(addList));
        permLog.setRemoveMenus(objectMapper.writeValueAsString(removeList));
        permLog.setTraceId(MDC.get("traceId"));

        // 异步落库
        asyncLogService.savePermissionLog(permLog);

        return result;
    }
}
