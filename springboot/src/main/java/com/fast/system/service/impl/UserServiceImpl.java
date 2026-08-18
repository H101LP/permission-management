package com.fast.system.service.impl;

import com.fast.system.domain.User;
import com.fast.system.mapper.UserMapper;
import com.fast.system.mapper.UserRoleMapper;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 */

@Service
public class UserServiceImpl implements IUserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Override
    public User selectByUserName(String userName) {
        return userMapper.selectByUserName(userName);
    }

    @Override
    public User selectByUserUserId(long userId) {
        return userMapper.selectByUserUserId(userId);
    }

    @Override
    public boolean registerUser(User newUser) {
        //根据用户名查询用户信息
        User user = userMapper.selectByUserName(newUser.getUserName());
        if (user != null) {
           throw new RuntimeException("用户名已存在");
        }
        return userMapper.insertUser(newUser) > 0;
    }

    @Override
    public int updateUserAvatar(Long userId, String avatar) {
        return userMapper.updateUserAvatar(userId, avatar);
    }

    @Override
    public int updateUser(User user)
    {
        return userMapper.updateUser(user);
    }

    @Override
    public int resetUserPwd(Long userId, String newPassword) {
        return userMapper.resetUserPwd(userId, newPassword);
    }

    @Override
    public List<User> selectUserList(User user) {
        return userMapper.selectUserList(user);

    }
    @Transactional//事务管理
    @Override
    public int insertUser(User user) {
        //新增用户
        userMapper.insertUser(user);
        //新增用户和角色关联
        return userRoleMapper.insertUserRole(user.getUserId(), user.getRoleId());

    }

    @Override
    public int deleteUserByUserIds(Long[] userIds) {
        return userMapper.deleteUserByUserIds(userIds);
    }
}
