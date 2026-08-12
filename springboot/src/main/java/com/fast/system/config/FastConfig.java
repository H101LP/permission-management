package com.fast.system.config;

import lombok.Data;
import lombok.Getter;
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
    /**
     * 文件上传路径，对应 application.yml 中的 fast.profile
     */
    private String profile;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
