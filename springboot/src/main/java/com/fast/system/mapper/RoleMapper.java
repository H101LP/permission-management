package com.fast.system.mapper;

import com.fast.system.domain.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色Mapper
 */
@Mapper
public interface RoleMapper {
    /**
     * 查询角色列表
     * @param role
     * @return
     */
    List<Role> selectRoleList(Role role);

    /**
     *  根据角色ID查询角色信息
     * @param roleId
     * @return
     */
    Role selectRoleById(Long roleId);

    /**
     * 新增角色信息
     * @param role 角色信息
     * @return 结果
     */
    int insertRole(Role role);
    /**
     * 修改角色信息
     * @param role 角色信息
     * @return 结果
     */
    int updateRole(Role role);

    /**
     * 删除角色信息
     * @param roleIds
     * @return
     */
    int deleteRoleByRoleIds(Long[] roleIds);
}
