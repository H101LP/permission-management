package com.fast.system.service.impl;

import com.fast.system.domain.User;
import com.fast.system.mapper.UserMapper;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */

@Service
public class UserServiceImpl implements IUserService {
    @Resource
    private UserMapper userMapper;
    @Override
    public User selectByUserName(String userName) {
        return userMapper.selectByUserName(userName);
    }

    @Override
    public User selectByUserUserId(long userId) {
        return userMapper.selectByUserUserId(userId);
    }
}
