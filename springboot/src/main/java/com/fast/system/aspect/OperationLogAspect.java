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
 * 操作日志切面。
 * <p>
 * 拦截标注 {@link OperationLog} 的方法，记录操作人、请求信息、请求参数、
 * 执行耗时、执行状态及异常信息，并通过异步服务保存操作日志。
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    /**
     * JSON 序列化工具。
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 异步日志服务。
     */
    @Resource
    private IAsyncLogService asyncLogService;

    /**
     * 环绕通知，拦截标注 {@link OperationLog} 的方法。
     *
     * @param point        连接点
     * @param operationLog 操作日志注解
     * @return 目标方法返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        int status = 1;
        String errorMsg = "";

        try {
            // 执行目标方法
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            // 记录异常状态及错误信息，异常继续抛出，由全局异常处理器处理
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            // 计算执行耗时并收集日志信息
            long costTime = System.currentTimeMillis() - start;
            try {
                // 登录用户和 traceId 依赖 ThreadLocal，需在调用异步方法前获取
                LoginUser loginUser = SecurityUtils.getLoginUser();
                String traceId = MDC.get("traceId");

                saveLog(point, operationLog, status, costTime, errorMsg, loginUser, traceId);
            } catch (Exception e) {
                // 日志保存失败不影响主业务流程
                log.error("保存审计日志失败", e);
            }
        }
    }

    /**
     * 收集并组装操作日志信息。
     *
     * @param point        连接点
     * @param opLog        操作日志注解
     * @param status       执行状态，1 表示成功，0 表示失败
     * @param costTime     执行耗时
     * @param errorMsg     异常信息
     * @param loginUser    当前登录用户
     * @param traceId      链路追踪 ID
     */
    private void saveLog(ProceedingJoinPoint point,
                         OperationLog opLog,
                         int status,
                         long costTime,
                         String errorMsg,
                         LoginUser loginUser,
                         String traceId) {

        // 获取当前请求信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 获取方法参数并转换为 JSON
        Object[] args = point.getArgs();
        String params = "";
        try {
            Object[] filterArgs = Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest))
                    .toArray();

            String rawJson = objectMapper.writeValueAsString(filterArgs);

            // 对密码相关字段进行脱敏处理
            params = rawJson.replaceAll(
                    "(?i)\"(password|oldPassword|newPassword)\"\\s*:\\s*\"[^\"]*\"",
                    "\"$1\":\"******\""
            );
        } catch (Exception e) {
            params = "参数解析失败";
        }

        // 组装操作日志实体
        SysOperationLog logEntity = new SysOperationLog();

        // 设置操作人信息
        if (loginUser != null) {
            logEntity.setUserId(loginUser.getUser().getUserId());
            logEntity.setUserName(loginUser.getUsername());
        } else {
            logEntity.setUserName("匿名");
        }

        // 设置注解信息
        logEntity.setModule(opLog.module());
        logEntity.setBusiness(opLog.business());
        logEntity.setOperType(opLog.type().name());

        // 设置请求信息
        if (request != null) {
            logEntity.setRequestUri(request.getRequestURI());
            logEntity.setRequestMethod(request.getMethod());
        }

        // 设置请求参数、耗时、状态及链路追踪 ID
        logEntity.setRequestParam(params);
        logEntity.setCostTime(costTime);
        logEntity.setStatus(status);
        logEntity.setTraceId(traceId);

        // 设置执行结果
        if (status == 0) {
            logEntity.setResult("失败: " + errorMsg);
        } else {
            logEntity.setResult("成功");
        }

        // 异步保存操作日志
        asyncLogService.saveLog(logEntity);
    }
}