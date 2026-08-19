package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.Menu;
import com.fast.system.service.IMenuService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/system/menu")
public class MenuController extends BaseController {
    @Resource
    private IMenuService menuService;
    /**
     * 查询菜单列表
     */
    @GetMapping("/selectMenuList")
    public AjaxResult selectMenuList(Menu menu) {
        List<Menu> list = menuService.selectMenuList(menu, SecurityUtils.getUserId());
        return success(list);
    }
    /**
     * 新增菜单
     */
    @PostMapping("/insertMenu")
    public AjaxResult insertMenu( @RequestBody Menu menu) {
        return toAjax(menuService.insertMenu(menu));
    }

}
