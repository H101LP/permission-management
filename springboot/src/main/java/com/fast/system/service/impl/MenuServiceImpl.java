package com.fast.system.service.impl;

import com.fast.system.constants.RoleIdConstants;
import com.fast.system.domain.Menu;
import com.fast.system.domain.TreeSelect;
import com.fast.system.mapper.MenuMapper;
import com.fast.system.mapper.UserRoleMapper;
import com.fast.system.service.IMenuService;
import com.sun.net.httpserver.Authenticator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    /**
     * 修改菜单
     */
    @Override
    public int updateMenu(Menu menu) {
        return menuMapper.updateMenu(menu);
    }

    /**
     * 删除菜单
     */
    @Override
    public int deleteMenuByMenuId(Long menuId) {
        return menuMapper.deleteMenuByMenuId(menuId);
    }

    /**
     * 根据角色ID查询对应的菜单树
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        return menuMapper.selectMenuListByRoleId(roleId);
    }
    /**
     * 构建前端所需要的下拉树结构
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */

    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<Menu> menus) {
        //步骤1 将平铺的菜单列表转换为树形的菜单列表
        List<Menu> menuTrees = buildMenuTree(menus);
        //步骤2 将树形Menu列表转换为TreeSelect列表
        return menuTrees.stream()
                .map(TreeSelect::new)
                .toList();
    }
    /**
     * 构建前端所需要的树结构
     */
    public List<Menu> buildMenuTree(List<Menu> menus) {
        //去除重复的菜单项 以menuId作为判断依据
        LinkedHashMap<Long, Menu> uniqeMenusMap = new LinkedHashMap<>();
        for (Menu menu : menus) {
            //如果菜单ID存在 跳过
            if (!uniqeMenusMap.containsKey(menu.getMenuId())) {
                uniqeMenusMap.put(menu.getMenuId(), menu);
            }
        }
        //转换为去重后的列表
        ArrayList<Menu> uniqeMenus = new ArrayList<>(uniqeMenusMap.values());
        //存放最终构建好的树形菜单列表
        ArrayList<Menu> returnList = new ArrayList<>();
        //将菜单列表转换为Map 快速查找
        LinkedHashMap<Long, Menu> menuMap = new LinkedHashMap<>();
        for (Menu menu : uniqeMenus) {
            menuMap.put(menu.getMenuId(), menu);
            //确保每个菜单都有初始化的children列表
            if(menu.getChildren() == null){
                menu.setChildren(new ArrayList<>());
            }else {
                //清空现有的children列表 避免重复添加
                menu.getChildren().clear();
            }
        }
        //查找顶级菜单
        HashSet<Long> addedTopMenuIds = new HashSet<>();
        for (Menu menu : uniqeMenus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                //这是一个顶级菜单 如果还没有添加过 则加入到结果列表
                if (!addedTopMenuIds.contains(menu.getMenuId())) {
                    returnList.add(menu);
                    addedTopMenuIds.add(menu.getMenuId());
                }
            } else {
                //这是一个子菜单 找到它的父菜单并添加到父菜单的children列表中
                Menu parentMenu = menuMap.get(menu.getParentId());
                if (parentMenu != null) {
                    parentMenu.getChildren().add(menu);
                }else {
                    //如果没有找到父菜单 可以暂时作为顶级菜单处理
                    returnList.add(menu);
                    addedTopMenuIds.add(menu.getMenuId());
                }
            }
        }
        return returnList;
    }

}
