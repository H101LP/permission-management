package com.fast.system.domain;

import lombok.Data;

/**
 * 登录参数
 */
@Data
public class LoginBody {
    private String userName;
    private String password;
}
