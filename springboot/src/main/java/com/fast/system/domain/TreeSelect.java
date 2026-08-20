package com.fast.system.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 树结构实体类
 */
@Data
public class TreeSelect {
    //节点ID  对应菜单的menuid
    private Long id;
    //节点名称
    private String label;
    //节点子信息
    // @JsonInclude注解用于控制是否序列化空值，NON_EMPTY表示不序列化空集合
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;

    /**
     * 构造函数 作用：将菜单对象转换为树结构对象
     * @param menu 菜单对象
     * 转换过程: 将菜单对象的menuid赋值给树结构对象的id，
     *         将菜单对象的menuname赋值给树结构对象的label，
     *         将菜单对象的children赋值给树结构对象的children
     *         递归转换子菜单：将菜单对象的children赋值给树结构对象的children，
     *      对children进行遍历，将每个菜单对象转换为树结构对象，直到没有子菜单为止
     */
    public TreeSelect(Menu menu) {
        //1 设置当前节点的ID = 菜单对象的menuid
        this.id = menu.getMenuId();
        //2 设置当前节点的名称 = 菜单对象的menuname
        this.label = menu.getMenuName();
        //3 处理子菜单方法 这个地方使用了递归:
        this.children = menu.getChildren().stream().map(TreeSelect::new).toList();
    }
}
