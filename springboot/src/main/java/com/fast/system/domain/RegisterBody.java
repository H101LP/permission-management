package com.fast.system.domain;

import lombok.Data;

/**
 * 注册参数
 */
@Data
public class RegisterBody {
    // 用户名
    private String userName;
    // 密码
    private String password;
}
