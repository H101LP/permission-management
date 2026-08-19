package com.fast.system.service.impl;

import com.fast.system.mapper.MenuMapper;
import com.fast.system.service.IMenuService;
import jakarta.annotation.Resource;

public class MenuServiceImpl implements IMenuService {
    @Resource
    private MenuMapper menuMapper;
}
