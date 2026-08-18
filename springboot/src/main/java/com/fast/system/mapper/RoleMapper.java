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
}
