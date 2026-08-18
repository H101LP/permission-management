package com.fast.system.service;

import com.fast.system.domain.Role;

import java.util.List;

/**
 * 角色 Service 接口类
 */
public interface IRoleService {
    /**
     * 查询角色列表
     * @return
     */
    List<Role> selectRoleList(Role role);

    /**
     * 根据角色ID查询角色信息
     * @param roleId 角色ID
      * @return 角色信息
     */
    Role selectRoleById(Long roleId);
}
