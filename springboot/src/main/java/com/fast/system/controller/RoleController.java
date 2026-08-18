package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.Role;
import com.fast.system.domain.TableDataInfo;
import com.fast.system.service.IRoleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色信息控制器
 */
@RestController
@RequestMapping("/system/role")
public class RoleController extends BaseController {
    @Resource
    private IRoleService roleService;
    /**
     * 查询所有角色列表
     */
    @GetMapping("/selectAllRole")
    public AjaxResult selectAllRole() {
        return success(roleService.selectRoleList(new Role()));
    }
    /**
     * 查询角色列表
     */
    @GetMapping("/selectRoleList")
    public TableDataInfo selectRoleList(Role role) {
        startPage();
        List<Role> list = roleService.selectRoleList(role);
        return getDataTable(list);
    }
    /**
     * 根据角色ID查询角色信息
     */
    @GetMapping("/selectRoleById/{roleId}")
    public AjaxResult selectRoleById(@PathVariable Long roleId) {
        return success(roleService.selectRoleById(roleId));
    }
    /**
     * 新增角色信息
     */
    @PostMapping("/insertRole")
    public AjaxResult insertRole(@RequestBody Role role) {
        return toAjax(roleService.insertRole(role));
    }
}
