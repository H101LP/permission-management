package com.fast.system.mapper;


import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与角色关联 mapper
 */
@Mapper
public interface UserRoleMapper {
    /**
     * 插入用户和角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return
     */
    int insertUserRole(Long userId, Long roleId);
}
