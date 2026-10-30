package com.fast.system.domain;

import lombok.Data;

import java.util.Date;

@Data
public class SysOperationLog {
    private Long logId;
    private Long userId;
    private String userName;
    private String module;
    private String business;
    private String operType;
    private String requestUri;
    private String requestMethod;
    private String requestParam;
    private String result;
    private Long costTime;
    private Integer status;
    private String traceId;
    private Date operTime;
}
