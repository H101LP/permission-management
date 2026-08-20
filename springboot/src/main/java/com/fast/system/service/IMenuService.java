package com.fast.system.service;

import com.fast.system.domain.Menu;
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
     */
    Menu selectMenuByMenuId(Long menuId);
}
