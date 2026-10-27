package com.fast.system.exception;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 拦截参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult<Void> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.warn("参数校验异常: {}", msg);
        return AjaxResult.error(ResultCode.PARAM_ERROR, msg);
    }

    // 2. 拦截权限不足异常 (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限拦截: {}", e.getMessage());
        return AjaxResult.error(ResultCode.FORBIDDEN, "权限不足，无法访问");
    }

    // 3. 拦截自定义业务异常
    @ExceptionHandler(BusinessException.class)
    public AjaxResult<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return AjaxResult.error(e.getCode(), e.getMessage());
    }

    // 4. 拦截请求方式错误异常（比如把 POST 接口用 GET 请求了，防止 405 报错泄露）
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方式错误: {}", e.getMessage());
        return AjaxResult.error(ResultCode.PARAM_ERROR, "不支持的请求方式");
    }

    // 5. 拦截 JSON 解析异常（前端传的 JSON 格式不对，比如少个括号）
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public AjaxResult<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("JSON解析失败: {}", e.getMessage());
        return AjaxResult.error(ResultCode.PARAM_ERROR, "请求参数格式错误");
    }

    // 6. 终极兜底
    @ExceptionHandler(Exception.class)
    public AjaxResult<Void> handleException(Exception e) {
        log.error("系统未知异常: ", e);
        return AjaxResult.error(ResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
    }
}