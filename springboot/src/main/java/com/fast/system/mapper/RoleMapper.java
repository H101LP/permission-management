package com.fast.system.mapper;

import com.fast.system.domain.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色Mapper
 */
@Mapper
public interface RoleMapper {
    List<Role> selectRoleList(Role role);
}
