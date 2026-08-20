package com.fast.system.service.impl;

import com.fast.system.domain.Role;
import com.fast.system.domain.RoleMenu;
import com.fast.system.mapper.RoleMapper;
import com.fast.system.mapper.RoleMenuMapper;
import com.fast.system.service.IRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色处理类
 */
@Service
public class RoleServiceImpl implements IRoleService {
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;


    /**
     * 查询角色列表
     * @return 角色列表
     */
    @Override
    public List<Role> selectRoleList(Role role) {
        return roleMapper.selectRoleList(role);
    }

    /**
     * 根据角色ID查询角色信息
     * @param roleId 角色ID
     * @return 角色信息
     */
    @Override
    public Role selectRoleById(Long roleId) {
        return roleMapper.selectRoleById(roleId);
    }
    /**
     * 新增角色信息
     * @param role 角色信息
     * @return 结果
     */
    @Transactional
    @Override
    public int insertRole(Role role) {
        //新增角色
        roleMapper.insertRole(role);
        //新增角色菜单信息
        return insetRoleMenu(role);
    }
    /**
     * 修改角色信息
     * @param role 角色信息
     * @return 结果
     */
    @Transactional
    @Override
    public int updateRole(Role role) {
        //修改角色
        roleMapper.updateRole(role);
        //根据角色ID删除角色菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        //新增角色菜单信息
        return insetRoleMenu(role);
    }
    /**
     * 新增角色菜单信息
     *
     */
    public int insetRoleMenu(Role role) {
        int rows =1;
        //新增角色菜单信息
        ArrayList<RoleMenu> list = new ArrayList<>();
        for (Long menuId : role.getMenuIds()) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if(list.size() > 0){
            //批量新增
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }




    /**
     * 删除角色信息
     * @param roleIds 角色ID
     * @return 结果
     */
    @Override
    public int deleteRoleByRoleIds(Long[] roleIds) {
        return roleMapper.deleteRoleByRoleIds(roleIds);
    }
}
