package com.fast.system.utils;

import com.fast.system.domain.LoginUser;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全服务工具类
 * 作用 提供便捷的方法获取当前登录用户信息
 * 使用场景：获取当前登录用户信息
 */
public class SecurityUtils {
    /**
     * 获取当前登录用户
     * 从Security的上下文获取已经认证的用户信息
     * @return loginUser 包含了用户的完整信息
     */
    public static LoginUser getLoginUser(){
        return  (LoginUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    /**
     * 获取当前登录用户ID
     * 从Security的认证信息中获取用户ID
     * @return 用户ID
     */
    public static Long getUserId(){
        //获取用户对象 返回其中的id
        return getLoginUser().getUserId();
    }
}
