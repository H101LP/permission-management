package com.fast.system.controller;

import com.fast.system.annotation.PreAuthorizePerm;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.Dept;
import com.fast.system.service.IDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 部门控制器
 */
@Tag(name = "部门控制器", description = "部门管理相关接口")
@RestController
@RequestMapping("/system/dept")
public class DeptController extends BaseController {

    @Resource
    private IDeptService deptService;

    /**
     * 查询部门列表（树形返回，不分页）
     * 注意：不要调 startPage()，分页会把树切断
     */
    @Operation(summary = "查询部门列表")
    @GetMapping("/selectDeptList")
    public AjaxResult selectDeptList(Dept dept) {
        return success(deptService.selectDeptTree(dept));
    }

    /** 根据ID查询 */
    @Operation(summary = "根据部门ID查询")
    @GetMapping("/selectDeptById/{deptId}")
    public AjaxResult selectDeptById(@PathVariable Long deptId) {
        return success(deptService.selectDeptById(deptId));
    }

    /** 部门树（给上级部门下拉用，和列表返回同一种结构） */
    @Operation(summary = "查询部门树")
    @GetMapping("/selectDeptTree")
    public AjaxResult selectDeptTree(Dept dept) {
        return success(deptService.selectDeptTree(dept));
    }

    /** 新增 */
    @PreAuthorizePerm("system:dept:add")
    @Operation(summary = "新增部门")
    @PostMapping("/insertDept")
    public AjaxResult insertDept(@RequestBody Dept dept) {
        return toAjax(deptService.insertDept(dept));
    }

    /** 修改 */
    @PreAuthorizePerm("system:dept:edit")
    @Operation(summary = "修改部门")
    @PutMapping("/updateDept")
    public AjaxResult updateDept(@RequestBody Dept dept) {
        return toAjax(deptService.updateDept(dept));
    }

    /** 删除 */
    @PreAuthorizePerm("system:dept:remove")
    @Operation(summary = "删除部门")
    @DeleteMapping("/deleteDeptById/{deptId}")
    public AjaxResult deleteDeptById(@PathVariable Long deptId) {
        return toAjax(deptService.deleteDeptById(deptId));
    }
}
