package com.fast.system.aspect;

import com.fast.system.domain.LoginUser;
import com.fast.system.domain.Role;
import com.fast.system.domain.SysPermissionLog;
import com.fast.system.service.IAsyncLogService;
import com.fast.system.service.IMenuService;
import com.fast.system.service.IPermCacheService;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    @Resource
    private IPermCacheService permCacheService;

    // 切入点
    @Around("execution(* com.fast.system.service.impl.RoleServiceImpl.insertRole(..)) || " +
            "execution(* com.fast.system.service.impl.RoleServiceImpl.updateRole(..)) || " +
            "execution(* com.fast.system.service.impl.RoleServiceImpl.deleteRoleByRoleIds(..)) || " +
            "execution(* com.fast.system.service.impl.MenuServiceImpl.insertMenu(..)) || " +
            "execution(* com.fast.system.service.impl.MenuServiceImpl.updateMenu(..)) || " +
            "execution(* com.fast.system.service.impl.MenuServiceImpl.deleteMenuByMenuId(..))")
    public Object aroundPermissionChange(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().getName();
        Object[] args = point.getArgs();

        // ★ 1. 菜单变更：只清路由缓存
        if (methodName.startsWith("insertMenu")
                || methodName.startsWith("updateMenu")
                || methodName.startsWith("deleteMenuByMenuId")) {
            Object result = point.proceed();
            runAfterCommit(permCacheService::evictAll);
            return result;
        }

        // ★ 2. 角色变更：准备抓取旧数据
        Long roleId = null;
        String roleName = null;
        Long[] newMenuIdsArray = null;
        List<Long> oldMenuIds = new ArrayList<>();

        // 删除角色：参数是 Long[] roleIds
        if ("deleteRoleByRoleIds".equals(methodName)) {
            Long[] roleIds = (Long[]) args[0];
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
                oldMenuIds = new ArrayList<>(); // 新增没有旧权限
            } else {
                oldMenuIds = menuService.selectMenuListByRoleId(roleId);
            }
        }

        // 放行执行真正的业务方法
        Object result = point.proceed();

        // ★ 2.1 删除角色：角色已不存在，无论菜单 diff 是否为空，
        //       所有被删角色的用户缓存都必须清掉（批量删除时逐个清）
        if ("deleteRoleByRoleIds".equals(methodName)) {
            Long[] deletedRoleIds = (Long[]) args[0];
            if (deletedRoleIds != null && deletedRoleIds.length > 0) {
                runAfterCommit(() -> {
                    for (Long id : deletedRoleIds) {
                        permCacheService.evictByRoleId(id);
                    }
                });
            }
        }

        // ★ 3. 核心对比（Diff 算法）
        List<Long> newMenuIds = new ArrayList<>();
        if (newMenuIdsArray != null) {
            newMenuIds.addAll(Arrays.asList(newMenuIdsArray));
        }

        List<Long> addList = new ArrayList<>(newMenuIds);
        addList.removeAll(oldMenuIds);

        List<Long> removeList = new ArrayList<>(oldMenuIds);
        removeList.removeAll(newMenuIds);

        // ★ 4. 如果没有发生任何变化，直接返回，不写日志也不删缓存
        if (addList.isEmpty() && removeList.isEmpty()) {
            return result;
        }

        // ★ 5. 既然变了，就写审计日志
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

        // 异步落库（只写一次！）
        asyncLogService.savePermissionLog(permLog);

        // ★ 6. 事务提交后：按 roleId 扩散删除该角色所有用户的缓存
        //    删除分支已在前面按 roleIds 逐个清除，这里只处理新增/修改
        if (!"deleteRoleByRoleIds".equals(methodName)) {
            Long finalRoleId = roleId;
            if (finalRoleId != null) {
                runAfterCommit(() -> permCacheService.evictByRoleId(finalRoleId));
            }
        }

        return result;
    }

    /**
     * 事务已开启则提交后执行；无事务（如菜单方法未加 @Transactional）则立即执行
     */
    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}