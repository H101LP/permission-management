package com.fast.system.configure;

import com.fast.system.domain.LoginUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * token服务
 * 作用 JWT令牌的生成 解析 验证
 * 相当系统检查站
 */
@Component
public class TokenService {
    //令牌自定义标识
    @Value("${token.header}")
    private String header;
    //令牌密钥
    @Value("${token.secret}")
    private String secret;
    //令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    //JWT签名密钥 将String密钥转换成key类型
    private Key secretKey;
    //JSON处理器
    @Resource
    private ObjectMapper objectMapper;


    //系统启动时初始化密钥
    @PostConstruct//在创建对象之后执行
    public void init() {
        //将配置的令牌密钥转换为key类型
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * 创建令牌
     * 功能:根据登录信息生成令牌
     * 使用场景:用户登录成功后生成令牌
     */
    public String createToken(LoginUser loginUser){
        //获取当前时间戳
        long now = System.currentTimeMillis();
        //计算过期时间戳
        long expirationTime = now + TimeUnit.MINUTES.toMillis(this.expireTime);
        //更新用户信息中的时间
        loginUser.setLoginTime(now);
        loginUser.setExpireTime(expirationTime);

        //准备JWT的声明数据
        HashMap<String, Object> claims = new HashMap<>();
        //将登录信息转换为JSON字符串
        try {
            claims.put("user_key", objectMapper.writeValueAsString(loginUser));
        } catch (Exception e) {
            throw new RuntimeException("序列化用户信息失败"+e);
        }
        //构建JWT token
        return Jwts.builder()
                .setClaims(claims) //设置声明数据
                .setExpiration(new Date(expirationTime)) //设置令牌过期时间
                .signWith(secretKey) //设置签名密钥
                .compact();//生成字符串
    }

}
