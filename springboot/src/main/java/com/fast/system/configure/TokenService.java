package com.fast.system.configure;

import com.fast.system.domain.LoginUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
@Slf4j
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
    /**
     * 获取用户登录信息
     * 功能:根据令牌获取用户登录信息
     * 使用场景 拦截器中调用 验证用户是否登录
     *
     */
    public LoginUser getLoginUser(HttpServletRequest request){
        //从请求头中获取token字符串
        String token = getToken(request);
        //判断token是否为空
        if(!StringUtils.hasText(token)){
            return null;  //用户未登录
        }
        try {
            //解析token获取声明数据
            Claims claims = parseToken(token);
            //将声明数据转换为JSON字符串
            String userjson = claims.get("user_key", String.class);
            //将JSON字符串反序列化
            return objectMapper.readValue(userjson, LoginUser.class);
        } catch (Exception e) {
            //捕获所有异常
            log.info("获取用户信息失败:{}",String.valueOf( e));

        }
        //解释失败
        return null;
    }
    /**
     * 解析令牌
     * 功能:根据令牌获取声明数据
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) //设置签名密钥
                .build()
                .parseClaimsJws(token)//解析令牌
                .getBody();
    }

    /**
     * 获取令牌
     * 功能:从请求头中获取令牌
     */
    public String getToken(HttpServletRequest request){
        String token = request.getHeader(this.header);
        if(StringUtils.hasText(token) && token.startsWith("Bearer ")){
            return token.substring(7);
        }
        return token;
    }
    /**
     * 验证令牌有效期
     * 功能:验证令牌是否过期
     */
    public void verifyToken(LoginUser loginUser){
        //获取过期时间
        long expireTime = loginUser.getExpireTime();
        //获取当前时间戳
        long nowTime = System.currentTimeMillis();
        //判断是否过期
        if(expireTime - nowTime <= 0){
            throw new RuntimeException("Token已过期");
        }
    }

}
