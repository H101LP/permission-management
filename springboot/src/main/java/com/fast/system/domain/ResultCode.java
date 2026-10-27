package com.fast.system.domain;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已失效"),
    FORBIDDEN(403, "权限不足，无法访问"),
    BUSINESS_ERROR(500, "业务处理异常"),
    SYSTEM_ERROR(500, "系统内部错误");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
