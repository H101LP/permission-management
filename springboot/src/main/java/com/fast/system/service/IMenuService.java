package com.fast.system.service;

import com.fast.system.domain.Menu;
import com.fast.system.domain.TreeSelect;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  菜单服务类
 */

public interface IMenuService {

    /**
     * 查询菜单列表
     * @param menu 菜单信息
     * @return 菜单列表
     */
    List<Menu> selectMenuList(Menu menu, Long userId);

    /**
     * 新增菜单
     * @param menu 菜单信息
     * @return 结果
     */
    int insertMenu(Menu menu);
    /**
     * 根据菜单ID查询菜单信息
      * @param menuId 菜单ID
      * @return 菜单信息
     */
    Menu selectMenuByMenuId(Long menuId);
    /**
     * 修改菜单
     * @param menu 菜单信息
     * @return 结果
     */
    int updateMenu(Menu menu);
    /**
     * 删除菜单
     * @param menuId 菜单ID
     * @return 结果
     */
    int deleteMenuByMenuId(Long menuId);

    /**
     * 根据角色ID查询对应的菜单树
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<Long> selectMenuListByRoleId(Long roleId);

    /**
     *  构建前端所需要的下拉树结构
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    List<TreeSelect> buildMenuTreeSelect(List<Menu> menus);
}
