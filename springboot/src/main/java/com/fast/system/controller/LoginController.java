package com.fast.system.controller;

import com.fast.system.configure.TokenService;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.LoginBody;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.User;
import com.fast.system.exception.BusinessException;
import com.fast.system.mapper.MenuMapper;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 登录控制器
 */
@RestController
public class LoginController extends BaseController {
    @Resource
    private IUserService userService;
    @Resource
    private TokenService tokenService;
    @Resource
    private MenuMapper menuMapper;
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
        //查询用户的所有权限
        List<String> permsList = menuMapper.selectMenuPermsByUserId(user.getUserId());
        //转为Set
        Set<String> permsSet = new HashSet<>();
        if(permsList != null && !permsList.isEmpty()){
            permsSet.addAll(permsList);
        }
        if (user.getUserId() != null && user.getUserId() == 1L) {
            permsSet.add("*:*:*");
        }
        //装载到LoginUser中
        loginUser.setPermissions(permsSet);

        System.out.println("当前登录用户权限集合: " + permsSet);
        //将用户部门ID装入LoginUser中
        loginUser.setDeptId(user.getDeptId());
        //判断是否为管理员级别
        loginUser.setAdmin(user.getUserId() != null && user.getUserId() == 1L);
        //生成JWT令牌
        String token = tokenService.createToken(loginUser);
        //返回成功结果 包含token
        Map<String, Object> map = new HashMap<>();
        map.put("permissions", permsSet);
        map.put("token", token); return success(map);

    }

}
