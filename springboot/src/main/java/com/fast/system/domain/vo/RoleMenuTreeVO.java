package com.fast.system.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoleMenuTreeVO {
    // 角色已分配的菜单ID数组
    private List<Long> checkedKeys;

    // 完整的菜单树结构
    private List<?> menus;
}
