package com.fast.system.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回响应体
 */
@Data
@NoArgsConstructor
public class AjaxResult<T>{
    private Integer code;
    private String msg;
    private T data;
    private Long timestamp = System.currentTimeMillis();
    // 成功静态工厂
    public static <T> AjaxResult<T> success(T data) {
        AjaxResult<T> result = new AjaxResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }
    public static <T> AjaxResult<T> successMsg(String msg) {
        AjaxResult<T> result = new AjaxResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(msg);
        return result;
    }

    public static <T> AjaxResult<T> success() {
        return success(null);
    }

    // 失败静态工厂
    public static <T> AjaxResult<T> error(ResultCode code, String msg) {
        AjaxResult<T> result = new AjaxResult<>();
        result.setCode(code.getCode());
        result.setMsg(msg);
        return result;
    }

    public static <T> AjaxResult<T> error(String msg) {
        return error(ResultCode.SYSTEM_ERROR, msg);
    }
    // 新增：兼容直接传 Integer 状态码的情况（比如业务异常类抛出来的 code）
    public static <T> AjaxResult<T> error(Integer code, String msg) {
        AjaxResult<T> result = new AjaxResult<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

}
