package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import com.fast.system.domain.User;
import com.fast.system.mapper.UserRoleMapper;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户信息
 */
@RestController
@RequestMapping("/system/user")
public class UserController extends BaseController {
    @Resource
    private IUserService userService;
    @Resource
    private UserRoleMapper userRoleMapper;
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
    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("selectUserById/{userId}")
    public AjaxResult selectUserById(@PathVariable Long userId) {
        User user = userService.selectByUserUserId(userId);
        return success(user);
    }
    /**
     * 新增用户
     */
    @PostMapping("insertUser")
    public AjaxResult insertUser(@RequestBody User user){
        return toAjax(userService.insertUser(user));
    }
    /**
     * 修改用户
     */
    @PutMapping("updateUser")
    public AjaxResult updateUser(@RequestBody User user){
        return toAjax(userService.updateUser(user));
    }
    /**
     *删除用户
     */
    @DeleteMapping("/deleteUserByUserIds/{userIds}")
    public AjaxResult deleteUserByUserIds(@PathVariable Long[] userIds){
       return toAjax(userService.deleteUserByUserIds(userIds));
    }



}
