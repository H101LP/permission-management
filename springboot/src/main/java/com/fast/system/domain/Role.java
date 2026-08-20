package com.fast.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 角色对象 对应role表
 */
@Data
public class Role {
    // 角色ID
    private Long roleId;
    // 角色名称
    private String roleName;
    //显示顺序
    private Integer roleSort;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTime;
    //菜单组
    private Long[] menuIds;
}
