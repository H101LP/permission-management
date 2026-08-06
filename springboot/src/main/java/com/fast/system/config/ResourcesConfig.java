package com.fast.system.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/**
 * 资源处理配置类
 * 1.配置静态资源访问类路径
 * 2.配置跨域访问规则（允许前端应用访问后端API）
 */

@Configuration//告诉spring我是配置类
public class ResourcesConfig implements WebMvcConfigurer {
    private final FastConfig fastConfig;
    public ResourcesConfig(FastConfig fastConfig) {
        this.fastConfig = fastConfig;
    }
    /**
     * 配置静态资源处理器
     * 这个方法用于告诉Spring当用户请求某些路径时  应该去服务器的哪个文件夹找对应的文件
     * @param registry  资源处理注册器 相当于文件管家
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:"+ fastConfig.getProfile()+"/");
    }
    /**
     * 跨域配置源
     * 配置哪些网站可以访问后端的API  起到通行证的作用
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        //1 创建跨域配置对象
        CorsConfiguration config = new CorsConfiguration();
        //2 允许所有来源
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        //3 允许携带所有请求头
        config.setAllowedHeaders(Collections.singletonList("*"));
        //4 允许所有方法（GET POST PUT DELETE ...）
        config.setAllowedMethods(Collections.singletonList("*"));
        //5 是否允许发送凭证
        config.setAllowCredentials(true);
        //预检请求的缓存时间
        config.setMaxAge(1800L);

        //创建基于URL的跨域配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //对所有路径应用跨域规则
        source.registerCorsConfiguration("/**", config);
        return source;

    }
}
