package com.fast.system.exception;

import com.fast.system.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 * 专门负责捕获系统内发生的 各种异常
 *
 */
//这是spring框架提供的注解 表示这是一个全局异常处理类
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理运行时异常
     * @param e 异常对象
     * @return 统一返回结果
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e){
        log.info("运行时异常: {}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     *  处理Exception异常
     * @param e 异常对象
     * @return 统一返回结果
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e){
        log.info("异常: {}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }
}
