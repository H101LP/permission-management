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
}
