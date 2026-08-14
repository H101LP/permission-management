package com.fast.system.controller;

import com.fast.system.domain.TableDataInfo;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户信息
 */
@RestController
@RequestMapping("/system/user")
public class UserController extends BaseController {
    @Resource
    private IUserService userService;
    /**
     * 查询用户列表
     */
    @GetMapping("/selectUserList")
    public TableDataInfo selectUserList(User user) {
        startPage();
        //查询用户列表
        List<User> list = userService.selectUserList(user);
        return getDataTable(list);
    }

}
