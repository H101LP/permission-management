package com.fast.system.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录用户身份类  封装登录用户信息
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private Long userId;

    //登陆时间 毫秒
    private Long loginTime;

    //过期时间
    private Long expireTime;

    //用户信息
    private User user;

    //带参数的构造方法
    public LoginUser(Long userId,User user){
        this.userId = userId;
        this.user = user;
    }





    /**
     * 用户权限集合
     * @return null
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /**
     * 获取密码
     * @return
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    /**
     * 获取用户名
     * @return
     */
    @Override
    public String getUsername() {
        return user.getUserName();
    }
}
