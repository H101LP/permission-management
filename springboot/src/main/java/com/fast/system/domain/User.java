package com.fast.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long userId;

    private String userName;

    private Integer sex;

    private String avatar;

    private String password;

    //创建时间
    //可以控制时间格式 GMT+8  表示东八区时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //角色ID（用于关联角色信息）
    private Long roleId;

}
