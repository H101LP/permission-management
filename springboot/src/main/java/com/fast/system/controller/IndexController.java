package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.User;
import com.fast.system.domain.vo.UserPermCacheVO;
import com.fast.system.mapper.MenuMapper;
import com.fast.system.service.IPermCacheService;
import com.fast.system.service.IUserService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


/**
 * 首页控制器 用于处理根路径请求
 *
 */
@RestController
@RequestMapping("/")
public class IndexController extends BaseController {
    @Resource
    private IUserService userService;
    @Resource
    private MenuMapper menuMapper;
    // 权限缓存服务
    @Resource
    private IPermCacheService permCacheService;
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
    public AjaxResult getInfo(HttpServletRequest request){
        long userId = SecurityUtils.getUserId();
        User user1 = userService.selectByUserUserId(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("user", user1);
        // 复用 Filter 已查好的快照；取不到再兜底查（防御性，正常不会走到）
        UserPermCacheVO snapshot = (UserPermCacheVO) request.getAttribute(IPermCacheService.REQUEST_SNAPSHOT_KEY);
        if (snapshot == null) {
            snapshot = permCacheService.getOrLoad(userId);
        }
        map.put("permissions", snapshot.getPerms());
        return success(map);
    }
}
