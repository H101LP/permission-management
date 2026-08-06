package com.fast.system.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 资源处理配置类
 * 1.配置静态资源访问类路径
 * 2.配置跨域访问规则（允许前端应用访问后端API）
 */

@Configuration//告诉spring我是配置类
public class ResourcesConfig implements WebMvcConfigurer {
    @Resource
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
}
