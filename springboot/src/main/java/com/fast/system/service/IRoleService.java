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
}
