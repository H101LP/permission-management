package com.fast.system.service.impl;

import com.fast.system.constants.RoleIdConstants;
import com.fast.system.domain.Menu;
import com.fast.system.mapper.MenuMapper;
import com.fast.system.mapper.UserRoleMapper;
import com.fast.system.service.IMenuService;
import com.sun.net.httpserver.Authenticator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MenuServiceImpl implements IMenuService {
    @Resource
    private MenuMapper menuMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    /**
     * 查询菜单列表
     */
    @Override
    public List<Menu> selectMenuList(Menu menu, Long userId) {
       //根据用户ID查询对应的角色信息
        Long roleId = userRoleMapper.selectRoleIdByUserId(userId);
        //管理员显示所有菜单信息
        if(Objects.equals(roleId, RoleIdConstants.ADMIN_ROLE_ID)){
           return menuMapper.selectMenuListByUserId(menu);
        }else {
            menu.setUserId(userId);
            return menuMapper.selectMenuListByUserId(menu);
        }
    }

    /**
     * 新增菜单
     * @param menu 菜单信息
     * @return
     */
    @Override
    public int insertMenu(Menu menu) {
        return menuMapper.insertMenu(menu);
    }

    /**
     * 根据菜单ID查询菜单信息
     */
    @Override
    public Menu selectMenuByMenuId(Long menuId) {
        return menuMapper.selectMenuByMenuId(menuId);
    }
}
