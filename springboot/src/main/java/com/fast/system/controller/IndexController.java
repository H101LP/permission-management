package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

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

    /**
     * 测试接口
     */
    @GetMapping("/text")
    public AjaxResult test(){
        return success(SecurityUtils.getUserId());

    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/getInfo")
    public AjaxResult getInfo(){
        //获取当前用户ID
        long userId = SecurityUtils.getUserId();
        //根据用户ID查询用户信息
        User user1 = userService.selectByUserUserId(userId);
        //返回用户信息
        return success(user1);
    }
}
