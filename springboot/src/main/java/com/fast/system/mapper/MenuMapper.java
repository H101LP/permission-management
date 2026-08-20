package com.fast.system.mapper;

import com.fast.system.domain.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单Mapper
 */

@Mapper
public interface MenuMapper {
    /**
     * 根据用户ID查询菜单列表
      * @param menu 菜单信息
     * @return 菜单列表
     */
    List<Menu> selectMenuListByUserId(Menu menu);
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
}
