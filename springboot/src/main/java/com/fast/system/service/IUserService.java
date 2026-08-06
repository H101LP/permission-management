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


}
