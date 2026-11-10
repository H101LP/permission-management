package com.fast.system.service.impl;

import com.fast.system.constants.RoleIdConstants;
import com.fast.system.domain.User;
import com.fast.system.exception.BusinessException;
import com.fast.system.mapper.UserMapper;
import com.fast.system.mapper.UserRoleMapper;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Resource
    private PasswordEncoder passwordEncoder;
    @Override
    public User selectByUserName(String userName) {
        return userMapper.selectByUserName(userName);
    }

    @Override
    public User selectByUserUserId(long userId) {
        return userMapper.selectByUserUserId(userId);
    }
    @Transactional
    @Override
    public boolean registerUser(User newUser) {
        //根据用户名查询用户信息
        User user = userMapper.selectByUserName(newUser.getUserName());
        if (user != null) {
           throw new BusinessException("用户名已存在");
        }
        // 加密后再落库
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        int i = userMapper.insertUser(newUser);
        userRoleMapper.insertUserRole(newUser.getUserId(), RoleIdConstants.USER_ROLE_ID);
        return i > 0;
    }

    @Override
    public int updateUserAvatar(Long userId, String avatar) {
        return userMapper.updateUserAvatar(userId, avatar);
    }

    @Override
    @Transactional
    public int updateUser(User user) {
        // 若前端带了密码，统一加密，避免明文落库
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        int rows = userMapper.updateUser(user);

        // 2. 如果前端传了 roleId，说明要改角色
        if (user.getRoleId() != null) {
            // 先删旧关联
            userRoleMapper.deleteUserRoleByUserId(user.getUserId());
            // 再插新关联
            userRoleMapper.insertUserRole(user.getUserId(), user.getRoleId());
        }

        return rows;
    }

    @Override
    public int resetUserPwd(Long userId, String newPassword) {
        return userMapper.resetUserPwd(userId, passwordEncoder.encode(newPassword));
    }

    @Override
    public List<User> selectUserList(User user) {
        return userMapper.selectUserList(user);

    }
    @Transactional//事务管理
    @Override
    public int insertUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.insertUser(user);
        return userRoleMapper.insertUserRole(user.getUserId(), user.getRoleId());
    }
    @Transactional//事务管理
    @Override
    public int deleteUserByUserIds(Long[] userIds) {
        //批量删除用户与角色的关联信息
        userRoleMapper.deleteUserRoles(userIds);
        return userMapper.deleteUserByUserIds(userIds);
    }
}
