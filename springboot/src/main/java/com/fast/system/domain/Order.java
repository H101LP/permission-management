package com.fast.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Order {
    // 订单ID
    private Long orderId;
    // 订单编号（唯一）
    private String orderNo;
    // 订单名称/摘要
    private String orderName;
    // 客户名称（纯文本，不建客户表）
    private String customerName;
    // 部门ID
    private Long deptId;
    // 负责人用户ID
    private Long ownerUserId;
    // 金额
    private BigDecimal amount;
    // 状态 0待确认 1已确认 2已完成
    private Integer status;
    // 备注
    private String remark;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
