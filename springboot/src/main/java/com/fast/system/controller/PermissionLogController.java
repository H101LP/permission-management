package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.PageResult;
import com.fast.system.domain.SysPermissionLog;
import com.fast.system.service.ISysPermissionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限变更日志查询。
 */
@Tag(name = "权限日志")
@RestController
@RequestMapping("/monitor/permlog")
public class PermissionLogController extends BaseController {

    @Resource
    private ISysPermissionLogService permissionLogService;

    @Operation(summary = "查询权限变更日志列表")
    @GetMapping("/selectList")
    public AjaxResult<PageResult<SysPermissionLog>> selectList(SysPermissionLog log) {
        startPage();
        List<SysPermissionLog> list = permissionLogService.selectPermissionLogList(log);
        return getDataTable(list);
    }
}
