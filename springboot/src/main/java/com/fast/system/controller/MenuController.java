package com.fast.system.controller;

import com.fast.system.annotation.PreAuthorizePerm;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.Menu;
import com.fast.system.domain.vo.RoleMenuTreeVO;
import com.fast.system.domain.vo.RouterVo;
import com.fast.system.service.IMenuService;
import com.fast.system.service.IPermCacheService;
import com.fast.system.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@Tag(name = "菜单控制器", description = "菜单管理相关接口")
@RestController
@RequestMapping("/system/menu")
public class MenuController extends BaseController {
    @Resource
    private IMenuService menuService;
    /**
     * 查询菜单列表
     */
    @Operation
    @GetMapping("/selectMenuList")
    public AjaxResult selectMenuList(Menu menu) {
        List<Menu> list = menuService.selectMenuList(menu, SecurityUtils.getUserId());
        return success(list);
    }
    /**
     * 新增菜单
     */
    @PreAuthorizePerm("system:menu:add")
    @Operation(summary = "新增菜单")
    @PostMapping("/insertMenu")
    public AjaxResult insertMenu( @RequestBody Menu menu) {
        return toAjax(menuService.insertMenu(menu));
    }
    /**
     * 根据菜单ID查询菜单信息
     */
    @Operation(summary = "根据菜单ID查询菜单信息")
    @GetMapping("/selectMenuByMenuId/{menuId}")
    public AjaxResult selectMenuByMenuId(@PathVariable Long menuId) {
        return success(menuService.selectMenuByMenuId(menuId));
    }
    /**
     * 修改菜单
     */
    @PreAuthorizePerm("system:menu:edit")
    @Operation(summary = "修改菜单")
    @PutMapping("/updateMenu")
    public AjaxResult updateMenu(@RequestBody Menu menu) {
        //上级菜单不能选择自己
        if (menu.getParentId().equals(menu.getMenuId())) {
            return error("上级菜单不能选择自己");
        }
         return toAjax(menuService.updateMenu(menu));
    }
    /**
     * 删除菜单
     */
    @PreAuthorizePerm("system:menu:remove")
    @Operation(summary = "删除菜单")
    @DeleteMapping("/deleteMenuByMenuId/{menuId}")
    public AjaxResult deleteMenuByMenuId(@PathVariable Long menuId) {
        return toAjax(menuService.deleteMenuByMenuId(menuId));
    }
    /**
     * 根据角色ID查询对应的菜单树
     *
     */
    @Operation(summary = "根据角色ID查询对应的菜单树")
    @GetMapping("/selectRoleMenuTree/{roleId}")
    public AjaxResult selectRoleMenuTree(@PathVariable Long roleId) {
        // 1. 查询所有菜单
        List<Menu> menus = menuService.selectMenuList(new Menu(), SecurityUtils.getUserId());
        //封装返回结果
        RoleMenuTreeVO roleMenuTreeVO = new RoleMenuTreeVO();
        roleMenuTreeVO.setCheckedKeys(menuService.selectMenuListByRoleId(roleId));
        roleMenuTreeVO.setMenus(menuService.buildMenuTreeSelect(menus));

        return success(roleMenuTreeVO);
    }
    /**
     * 查询前端所需要的菜单下拉树结构
     */
    @Operation(summary = "查询前端所需要的菜单下拉树结构")
    @GetMapping("/selectRoleMenusTree")
    public AjaxResult selectMenusTree() {
        List<Menu> menus = menuService.selectMenuList(new Menu(), SecurityUtils.getUserId());
        return success(menuService.buildMenuTreeSelect(menus));
    }
    /**
     * 获取路由信息（菜单权限）
     * 功能: 获取当前用户的菜单权限 用于前端动态生成路由
     */
    @Resource
    private IPermCacheService permCacheService;
    @Operation(summary = "获取路由信息（菜单权限）")
    @GetMapping("/getRouters")
    public AjaxResult getRouters() {
       //获取当前用户的ID
        long userId = SecurityUtils.getUserId();
        // 走缓存：命中直接返回，未命中查库并回写
        return success(permCacheService.getOrLoad(userId).getRouters());
    }


}
