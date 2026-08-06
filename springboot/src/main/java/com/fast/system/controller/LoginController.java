package com.fast.system.controller;

import com.fast.system.configure.TokenService;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.LoginBody;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public AjaxResult login(@RequestBody LoginBody loginBody){
        //验证参数是否为空
        if(loginBody.getUserName()==null||loginBody.getPassword()==null
                || loginBody.getUserName().trim().isEmpty() || loginBody.getPassword().trim().isEmpty()){
            throw new RuntimeException("用户名或密码为空");
        }
        //2验证用户是否存在
        User user = userService.selectByUserName(loginBody.getUserName());
        if(user == null){
            throw new RuntimeException("用户不存在");
        }
        //验证密码是否正确
        if(!loginBody.getPassword().equals(user.getPassword())){
            throw new RuntimeException("密码错误");
        }
        LoginUser loginUser = new LoginUser(user.getUserId(),user);
        //生成JWT令牌
        String token = tokenService.createToken(loginUser);
        //返回成功结果 包含token
        return success().put("token",token);

    }

}
