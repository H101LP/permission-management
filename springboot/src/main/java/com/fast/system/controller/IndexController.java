package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器 用于处理根路径请求
 *
 */
@RestController
@RequestMapping("/")
public class IndexController extends BaseController {
    @Resource
    private IUserService userService;
    @GetMapping
    public AjaxResult home() {
        return AjaxResult.success("恭喜你成功启动了后端");
    }
    @GetMapping("/selectUserByUserName/{userName}")
    public AjaxResult test(@PathVariable String userName){
        User user = userService.selectByUserName(userName);
        if(user == null){
            return error("用户不存在");
        }
        return success(user);
    }
}
