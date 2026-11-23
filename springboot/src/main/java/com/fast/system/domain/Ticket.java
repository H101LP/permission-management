package com.fast.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class Ticket {
    // 工单ID
    private Long ticketId;
    // 工单编号
    private String ticketNo;
    // 工单标题
    private String ticketTitle;
    // 工单类型 0故障 1需求 2咨询
    private Integer ticketType;
    // 部门ID
    private Long deptId;
    // 处理人用户ID
    private Long ownerUserId;
    // 状态 0待处理 1处理中 2已关闭
    private Integer status;
    // 备注
    private String remark;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
