package com.fast.system.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class Dept {
    //部门ID
    private Long deptId;
    //部门名称
    private String deptName;
    //父部门ID
    private Long parentId;
    //祖级列表
    private String ancestors;
    //排序
    private Integer orderNum;
    //部门领导
    private String leader;
    //部门状态
    private String status;
    //创建时间
    private String createTime;
    //子部门列表 用于生成树形列表
    private List<Dept> children = new ArrayList<Dept>();
}
