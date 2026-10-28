package com.fast.system.controller;

import com.fast.system.configure.TokenService;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.LoginBody;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.User;
import com.fast.system.exception.BusinessException;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 */
@RestController
public class LoginController extends BaseController {
    @Resource
    private IUserService userService;
    @Resource
    private TokenService tokenService;
    /**
     * 登录接口
     */
    @PostMapping("/login")
    public AjaxResult login(@Validated @RequestBody LoginBody loginBody){
        //2验证用户是否存在
        User user = userService.selectByUserName(loginBody.getUserName());
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        //验证密码是否正确
        if(!loginBody.getPassword().equals(user.getPassword())){
            throw new BusinessException("密码错误");
        }
        LoginUser loginUser = new LoginUser(user.getUserId(),user);
        //生成JWT令牌
        String token = tokenService.createToken(loginUser);
        //返回成功结果 包含token
        Map<String, Object> map = new HashMap<>();
        map.put("token", token); return success(map);

    }

}
