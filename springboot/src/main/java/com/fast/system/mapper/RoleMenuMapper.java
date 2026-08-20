package com.fast.system.mapper;

import com.fast.system.domain.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

/**
 * 角色与菜单关联Mapper
 */
@Mapper
public interface RoleMenuMapper {
    /**
     * 根据角色ID删除角色菜单关联
      * @param roleId 角色ID
     * @return 是否成功
     */
    int deleteRoleMenuByRoleId(Long roleId);

    /**
     * 批量新增角色菜单关联
     * @param list 角色菜单关联列表
     * @return 是否成功
     */
    int batchRoleMenu(ArrayList<RoleMenu> list);
}
