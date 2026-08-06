package com.fast.system.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 程序基础配置类
 *
 * springboot的基础设置中心  负责配置一些通用的全局的应用程序设置
 */

@Configuration //告诉spring我是配置类
@MapperScan("com.fast.**.mapper") //告诉mybatis去哪里找数据接口
public class ApplicationConfig {

    /**
     * 时区配置bean
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(){
        return jacksonObjectMapperBuilder ->
                jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
    }

}
