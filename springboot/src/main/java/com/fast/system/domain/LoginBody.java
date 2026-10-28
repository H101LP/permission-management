package com.fast.system.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录参数
 */
@Data
public class LoginBody {
    @NotBlank(message = "用户名不能为空")
    private String userName;
    @NotBlank(message = "密码不能为空")
    private String password;
}
