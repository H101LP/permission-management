package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器 用于处理根路径请求
 *
 */
@RestController
@RequestMapping("/")
public class IndexController extends BaseController {
    @GetMapping
    public AjaxResult home() {
        return AjaxResult.success("恭喜你成功启动了后端");
    }
    @GetMapping("/test")
    public AjaxResult test(){

        return success("注册成功");
    }
}
