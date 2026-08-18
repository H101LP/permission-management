package com.fast.system.service.impl;

import com.fast.system.domain.Role;
import com.fast.system.mapper.RoleMapper;
import com.fast.system.service.IRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色处理类
 */
@Service
public class RoleServiceImpl implements IRoleService {
    @Resource
    private RoleMapper roleMapper;


    /**
     * 查询角色列表
     * @return 角色列表
     */
    @Override
    public List<Role> selectRoleList(Role role) {
        return roleMapper.selectRoleList(role);
    }

    /**
     * 根据角色ID查询角色信息
     * @param roleId 角色ID
     * @return 角色信息
     */
    @Override
    public Role selectRoleById(Long roleId) {
        return roleMapper.selectRoleById(roleId);
    }
    /**
     * 新增角色信息
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public int insertRole(Role role) {
        return roleMapper.insertRole(role);
    }
}
