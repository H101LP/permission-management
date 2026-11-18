package com.fast.system.config;

import com.fast.system.configure.JwtAuthenticationEntryPoint;
import com.fast.system.configure.JwtAuthenticationTokenFilter;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
    @Resource
    private JwtAuthenticationTokenFilter authenticationTokenFilter ;
    //认证入口点
    @Resource
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * 密码加密
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength=10 是默认值 单次加密约 50~100ms 防暴力破解的代价
        return new BCryptPasswordEncoder();
    }
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
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        //5异常处理  当认证失败的处理方式
                .exceptionHandling(e ->e.authenticationEntryPoint(authenticationEntryPoint))
                //6 路径权限配置
                .authorizeHttpRequests(req ->req
                        //公开接口
                                .requestMatchers("/login","/logout","/register","/profile/**").permitAll().requestMatchers(
                                        "/doc.html",
                                        "/webjars/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                ).permitAll()
                        //其他接口需要认证
                                .anyRequest().authenticated()
                        )
                // ⚠️ 必须显式 disable，仅仅"不写 .logout(...)"是不够的：
                // Spring Security 6 的 LogoutConfigurer 默认启用，LogoutFilter 依然挂在过滤器链上，
                // 会抢在 DispatcherServlet 之前处理 /logout 并 302 到 /login?logout，
                // 导致 LoginController 里的自定义登出（写 jti 黑名单）永远执行不到。
                // 实测：只注释掉 .logout(...) 时，POST /logout 返回 302，Redis 里没有 token:blacklist:* 键。
                .logout(AbstractHttpConfigurer::disable)
                //添加JWT的过滤器
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);


                return http.build();

    }
}
