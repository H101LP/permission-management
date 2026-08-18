package com.fast.system.service.impl;

import com.fast.system.mapper.RoleMapper;
import com.fast.system.service.IRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 角色处理类
 */
@Service
public class RoleServiceImpl implements IRoleService {
    @Resource
    private RoleMapper roleMapper;
}
