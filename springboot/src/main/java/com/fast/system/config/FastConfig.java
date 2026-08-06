package com.fast.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 * 这是项目读取器  专门读取application.yml文件的配置
 * 可以看错翻译官
 */
@Component
@ConfigurationProperties(prefix = "fast")//核心注解 读取配置文件中以fast开头的配置项
public class FastConfig {

    public String getProfile() {
        return profile;
    }

    public FastConfig setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    /**
     * 文件上传路径配置  对应配置文件中的fast.profile
     * 使用static静态变量 可以直接通过类名访问  比如 fastconfig.profile
     */

    private String profile;//存储上传文件的基本路径
}
