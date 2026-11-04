package com.fast.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 权限变更审计日志实体类
 */
@Data
public class SysPermissionLog {
    /** 日志主键 */
    private Long logId;

    /** 操作人ID */
    private Long operUserId;

    /** 操作人名称 */
    private String operUserName;

    /** 被操作的角色ID */
    private Long targetRoleId;

    /** 被操作的角色名称 */
    private String targetRoleName;

    /** 变更前的菜单ID集合(JSON字符串) */
    private String oldMenus;

    /** 变更后的菜单ID集合(JSON字符串) */
    private String newMenus;

    /** 新增的权限ID(JSON字符串) */
    private String addMenus;

    /** 移除的权限ID(JSON字符串) */
    private String removeMenus;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operTime;

    /** 链路追踪ID */
    private String traceId;
}