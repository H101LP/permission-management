package com.fast.system.domain;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> rows;       // 当前页数据
    private Long total;         // 总条数
    private Integer pageNum;    // 当前页码
    private Integer pageSize;   // 每页条数
}
