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

    /**
     * 根据用户ID删除用户和角色关联
      * @param userId 用户ID
     * @return 结果
     */
    int deleteUserRoleByUserId(Long userId);

    /**
     * 根据用户ID批量删除用户和角色关联
     * @param userIds 用户ID数组
     * @return 结果
     */
    int deleteUserRoles(Long[] userIds);
}
