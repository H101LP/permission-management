package com.fast.system.exception;

import com.fast.system.domain.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * 用于主动抛出预期内的业务错误（如：用户名已存在、旧密码错误等）
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;
    //构造
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String msg) {
        super(msg);
        this.code = resultCode.getCode();
    }

    public BusinessException(String msg) {
        super(msg);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }
}