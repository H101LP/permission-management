package com.fast.system.aspect;

import com.fast.system.annotation.PreAuthorizePerm;
import com.fast.system.domain.LoginUser;
import com.fast.system.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 权限校验切面（保安查证）
 */
@Slf4j
@Aspect
@Component
public class PreAuthorizeAspect {

    // 环绕通知：拦截所有带有 @PreAuthorizePerm 注解的方法
    @Around("@annotation(preAuthorizePerm)")
    public Object around(ProceedingJoinPoint point, PreAuthorizePerm preAuthorizePerm) throws Throwable {

        // 1. 获取当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            // 如果没登录，直接拒绝
            throw new AccessDeniedException("未登录，无法访问");
        }

        // 2. 获取当前用户拥有的权限集合（就是刚才装进去的通行证）
        Set<String> permissions = loginUser.getPermissions();

        // 3. 获取接口需要的权限标识（比如 "system:user:add"）
        String requiredPerm = preAuthorizePerm.value();

        // 4. 核心逻辑：判断是否有权限
        // 必须是超级管理员，或者拥有对应权限，才放行
        if (loginUser.getUserId() != null && loginUser.getUserId() == 1L) {
            log.info("超级管理员访问，直接放行。需要权限: {}", requiredPerm);
        } else if (permissions != null && permissions.contains(requiredPerm)) {
            log.info("权限校验通过，用户: {}，需要的权限: {}", loginUser.getUsername(), requiredPerm);
        } else {
            // 没有权限，抛出异常，交给全局异常处理器返回 403
            log.warn("权限校验失败，用户: {}，试图访问: {}，缺少权限: {}", loginUser.getUsername(), point.getSignature().getName(), requiredPerm);
            throw new AccessDeniedException("权限不足，无法访问");
        }

        // 5. 校验通过，放行执行目标方法
        return point.proceed();
    }
}