package com.fast.system.configure;

import com.fast.system.domain.LoginUser;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * JWT认证过滤器
 * 功能:拦截所有HTTP请求 从请求头中获取令牌并验证
 *  检查每一个访客的通行证
 * 使用场景:
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Resource
    private TokenService tokenService;
    /**
     * 拦截所有HTTP请求  过滤器核心方法   从请求头提取JWT token ,验证用户信息,设置security用户信息
     * @param request 请求对象
     * @param response 响应对象
     * @param chain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        //1 从HTTP请求中解析用户信息
        LoginUser loginUser = tokenService.getLoginUser(request);
        //2 判断 是否需要设置认证信息
        if(Objects.nonNull(loginUser) && SecurityContextHolder.getContext().getAuthentication() == null){
            try {
                //3 验证token是否过期
                tokenService.verifyToken(loginUser);
                //4 构建sercurity认证令牌
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                //5 设置认证详情
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails( request));
                //6将认证信息设置到上下文
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e){
                //7处理验证失败的情况
                SecurityContextHolder.clearContext();
            }
        }
        //8 继续处理下一个过滤器链条
        chain.doFilter(request, response);
    }
}
