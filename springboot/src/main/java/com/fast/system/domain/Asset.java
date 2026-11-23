package com.fast.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Asset {
    // 资产ID
    private Long assetId;
    // 资产名称
    private String assetName;
    // 资产编码
    private String assetCode;
    // 部门ID
    private Long deptId;
    // 负责人用户ID
    private Long ownerUserId;
    // 状态
    private Integer status;
    // 价格
    private BigDecimal price;
    // 备注
    private String remark;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
