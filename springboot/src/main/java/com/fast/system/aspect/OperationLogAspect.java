package com.fast.system.aspect;

import com.fast.system.annotation.OperationLog;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.SysOperationLog;
import com.fast.system.service.IAsyncLogService;
import com.fast.system.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 操作日志切面（监控摄像头）
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    // 注入 JSON 工具
    @Resource
    private ObjectMapper objectMapper;

    // 注入异步服务接口
    @Resource
    private IAsyncLogService asyncLogService;

    // 环绕通知：拦截所有带有 @OperationLog 注解的方法
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        int status = 1;
        String errorMsg = "";

        try {
            // 1. 放行执行目标方法（执行你的 Controller 里的实际业务）
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            // 如果业务报错，记录状态和错误信息
            status = 0;
            errorMsg = e.getMessage();
            throw e; // 异常继续抛出，交给你之前写的全局异常处理器
        } finally {
            // 2. 无论成功失败，方法结束后计算耗时并收集日志信息
            long costTime = System.currentTimeMillis() - start;
            try {
                // ⚠️ 核心避坑点：在切面主线程提前获取用户和 TraceId！
                // 因为一旦进入异步子线程，ThreadLocal 是拿不到这些数据的，会变成 null
                LoginUser loginUser = SecurityUtils.getLoginUser();
                String traceId = MDC.get("traceId");

                // 组装数据并调用异步方法
                saveLog(point, operationLog, status, costTime, errorMsg, loginUser, traceId);
            } catch (Exception e) {
                log.error("保存审计日志失败", e); // 日志保存失败绝不影响主业务
            }
        }
    }

    /**
     * 收集信息并组装实体
     */
    private void saveLog(ProceedingJoinPoint point, OperationLog opLog, int status, long costTime, String errorMsg, LoginUser loginUser, String traceId) {

        // 3. 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 4. 获取参数并转 JSON
        Object[] args = point.getArgs();
        String params = "";
        try {
            Object[] filterArgs = Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest)).toArray();

            // 先转成 JSON 字符串
            String rawJson = objectMapper.writeValueAsString(filterArgs);

            // ⚠️ 核心黑科技：全局正则脱敏！
            // 把 json 字符串里所有形如 "password":"任意字符" 或者 "oldPassword":"..." 的值替换成 "***"
            // (?i) 表示忽略大小写，不管前端传 password 还是 Password 都能拦住
            params = rawJson.replaceAll("(?i)\"(password|oldPassword|newPassword)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"******\"");

        } catch (Exception e) {
            params = "参数解析失败";
        }

        // 5. 组装实体类对象
        SysOperationLog logEntity = new SysOperationLog();

        // 拿用户信息（防坑：如果未登录接口报错，loginUser 会为空）
        if (loginUser != null) {
            logEntity.setUserId(loginUser.getUser().getUserId());
            logEntity.setUserName(loginUser.getUsername());
        } else {
            logEntity.setUserName("匿名");
        }

        // 拿注解上的信息
        logEntity.setModule(opLog.module());
        logEntity.setBusiness(opLog.business());
        logEntity.setOperType(opLog.type().name());

        // 拿请求信息
        if (request != null) {
            logEntity.setRequestUri(request.getRequestURI());
            logEntity.setRequestMethod(request.getMethod());
        }

        // 拿参数、耗时、状态、TraceId
        logEntity.setRequestParam(params);
        logEntity.setCostTime(costTime);
        logEntity.setStatus(status);
        logEntity.setTraceId(traceId);

        // 如果是失败，把错误信息放到 result 里
        if (status == 0) {
            logEntity.setResult("失败: " + errorMsg);
        } else {
            logEntity.setResult("成功");
        }

        // 6. 调用异步服务，直接丢给子线程去落库，主线程瞬间返回！
        asyncLogService.saveLog(logEntity);
    }
}