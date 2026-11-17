package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.PageResult;
import com.fast.system.domain.SysOperationLog;
import com.fast.system.service.ISysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志查询。
 * <p>
 * 只做"读"，所以不加 @PreAuthorizePerm：
 * 菜单可见性由 role_menu 控制（没给角色分配这个菜单，侧边栏就看不到），
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/monitor/operlog")
public class OperationLogController extends BaseController {

    @Resource
    private ISysOperationLogService operationLogService;

    /**
     * 查询操作日志列表。
     * 入参 SysOperationLog 由 Spring 自动从 query string 绑定，
     * pageNum / pageSize 由 startPage() 从 request 里读，不在实体里。
     */
    @Operation(summary = "查询操作日志列表")
    @GetMapping("/selectList")
    public AjaxResult<PageResult<SysOperationLog>> selectList(SysOperationLog log) {
        startPage();                                  // 开分页（读 pageNum/pageSize）
        List<SysOperationLog> list = operationLogService.selectOperationLogList(log);
        return getDataTable(list);                    // 包成 AjaxResult<PageResult>
    }
}
