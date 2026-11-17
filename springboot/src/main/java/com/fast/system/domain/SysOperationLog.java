package com.fast.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class SysOperationLog {
    /** 主键ID */
    private Long logId;
    /** 操作人ID */
    private Long userId;
    /** 操作人名称 */
    private String userName;
    /** 模块（如：用户管理） */
    private String module;
    /** 业务（如：新增用户） */
    private String business;
    /** 操作类型（INSERT/UPDATE等） */
    private String operType;
    /** 请求URL */
    private String requestUri;
    /** 请求方式（GET/POST） */
    private String requestMethod;
    /** 请求参数（已脱敏） */
    private String requestParam;
    /** 返回结果 */
    private String result;
    /** 耗时（ms） */
    private Long costTime;
    /** 状态（1成功 0失败） */
    private Integer status;
    /** 链路追踪ID */
    private String traceId;

    /** 操作时间（必须加注解，防前端显示时间戳） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operTime;

    /** 查询专用：开始时间（非表字段） */
    private String beginTime;
    /** 查询专用：结束时间（非表字段） */
    private String endTime;
}