package com.fast.system.service;

import com.fast.system.domain.User;

/**
 * 用户业务接口
 */
public interface IUserService {
    /**
     * 根据用户名查询用户
     * @param userName 用户名
     * @return 用户对象信息
     */
    public User selectByUserName(String userName);

    /**
     * 根据用户ID查询用户
     * @param userId 用户ID
     * @return 用户对象信息
     */
    User selectByUserUserId(long userId);

    /**
     * 注册用户
     * @param newUser 新用户对象
     * @return  注册结果
     */
    boolean registerUser(User newUser);

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    int updateUserAvatar(Long userId, String avatar);

    /**
     * 修改用户信息
     * @param user 用户对象
     * @return 是否成功
     */
    int updateUser(User user);
}
