package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.service.IRoleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return success(roleService.selectRoleList());
    }
}
