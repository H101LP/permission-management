package com.fast.system.mapper;

import com.fast.system.domain.User;
import org.apache.ibatis.annotations.Mapper;
/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询用户
     * @param userName 用户名
     * @return 用户
     */
    public User selectByUserName(String userName);

    /**
     * 根据用户ID查询用户
     * @param userId
     * @return
     */
    User selectByUserUserId(long userId);

    /**
     * 新增用户
     * @param user 用户
     * @return 是否新增成功
     */
    int insertUser(User user);

    int updateUserAvatar(Long userId, String avatar);

    /**
     * 修改用户信息
     * @param user 用户对象
     * @return 是否修改成功
     */
    int updateUser(User user);
}
