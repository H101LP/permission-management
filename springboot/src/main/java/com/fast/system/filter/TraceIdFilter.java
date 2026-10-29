package com.fast.system.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 全链路追踪拦截器
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 优先级最高，必须比 JWT 过滤器还要早执行
public class TraceIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 1. 优先从请求头获取 (支持微服务间调用透传)，如果没有则生成
        String traceId = req.getHeader("traceId");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replaceAll("-", "");
        }

        // 2. 存入 MDC (底层是 ThreadLocal)
        MDC.put("traceId", traceId);

        // 3. 返回给前端响应头，方便用户报错时提供该 ID
        res.setHeader("traceId", traceId);

        try {
            // 4. 放行请求
            chain.doFilter(request, response);
        } finally {
            // 5.请求结束后必须清理，否则 Tomcat 线程复用会导致内存泄漏和串号
            MDC.remove("traceId");
        }
    }
}