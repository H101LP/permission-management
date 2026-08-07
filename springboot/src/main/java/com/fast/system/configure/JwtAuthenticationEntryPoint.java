package com.fast.system.configure;

import com.fast.system.domain.AjaxResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Jwt认证处理点 处理类
 * 作用 当用户访问受保护接口时  如果用户没有认证  则调用这个类统一返回错误信息
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Resource
    private ObjectMapper objectMapper;
    /**
     * 认证入口点方法
     *当用户访问需要认证的接口但是未认证时 会调用这个方法
     * @param request http请求对象
     * @param response 响应对象
     * @param e 认证异常对象
     * @throws IOException IO异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        // 设置HTTP状态码为401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //设置响应内容为JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        //设置字符集编码 UTF-8 避免中文乱码
        response.setCharacterEncoding("UTF-8");
        //返回构建统一错误响应
        String jsonResponse = objectMapper.writeValueAsString(AjaxResult.error(401,"登录已过期，请重新登陆"));
        //JSON响应数据写回响应体
        response.getWriter().println(jsonResponse);
    }
}
