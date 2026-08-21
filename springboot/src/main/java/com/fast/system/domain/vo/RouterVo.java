package com.fast.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 路由配置信息
 * 用来描述一个完整的路由结构
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo {
    //路由名称
    private String name;
    //路由地址
    private String path;
    //路由对应的前端组件
    private String component;
    //是否总是显示为嵌套模式
    private boolean alwaysShow;
    //路由元信息  路由的额外配置信息
    private MetaVo meta;
    //子路由
    private List<RouterVo> children;
}
