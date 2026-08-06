package com.fast.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 *
 * 认证配置类
 * 哪些要公开接口 哪些需要登录
 *如何检查认证
 * 如何进行异常处理
 */
@EnableMethodSecurity(securedEnabled = true)
@Configuration
public class SecurityConfig {
    /**
     * 认证过滤器链
     * 1.跨域检查->取消CSRF 防护->响应头配置->禁用Session-使用无状态认证（JWT）->构建
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
            //开启跨域支持
        http.cors(Customizer.withDefaults())
                //禁用CSRF防护
                .csrf(AbstractHttpConfigurer::disable)
                //响应头配置 固定
                .headers(headers->headers
                        .frameOptions(fram->fram.sameOrigin()))
                //禁用Session-使用无状态认证（JWT） 固定写法
                //每次请求都会去验证用户
                .sessionManagement(session->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                return http.build();

    }
}
