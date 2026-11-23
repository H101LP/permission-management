package com.fast.system.controller;

import com.fast.system.annotation.OperationLog;
import com.fast.system.annotation.PreAuthorizePerm;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.Asset;
import com.fast.system.domain.PageResult;
import com.fast.system.enums.OperType;
import com.fast.system.service.IAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资产管理
 */
@Tag(name = "资产管理")
@RestController
@RequestMapping("/business/asset")
public class AssetController extends BaseController {

    @Resource
    private IAssetService assetService;

    /**
     * 查询资产列表
     * 不加 @PreAuthorizePerm：menu 表没有 business:asset:list 这个权限字符，
     * 加了之后部门管理员会直接 403。可见性由菜单级（C 类型授权）控制。
     */
    @Operation(summary = "查询资产列表")
    @GetMapping("/selectAssetList")
    public AjaxResult<PageResult<Asset>> selectAssetList(Asset asset) {
        startPage();
        List<Asset> list = assetService.selectAssetList(asset);
        return getDataTable(list);
    }

    @Operation(summary = "根据ID查询资产详情")
    @GetMapping("/selectAssetById/{assetId}")
    public AjaxResult selectAssetById(@PathVariable Long assetId) {
        return success(assetService.selectAssetById(assetId));
    }

    @PreAuthorizePerm("business:asset:add")
    @Operation(summary = "新增资产")
    @OperationLog(module = "资产管理", business = "新增资产", type = OperType.INSERT)
    @PostMapping("/insertAsset")
    public AjaxResult insertAsset(@RequestBody Asset asset) {
        return toAjax(assetService.insertAsset(asset));
    }

    @PreAuthorizePerm("business:asset:edit")
    @Operation(summary = "修改资产")
    @OperationLog(module = "资产管理", business = "修改资产", type = OperType.UPDATE)
    @PutMapping("/updateAsset")
    public AjaxResult updateAsset(@RequestBody Asset asset) {
        return toAjax(assetService.updateAsset(asset));
    }

    @PreAuthorizePerm("business:asset:remove")
    @Operation(summary = "删除资产")
    @OperationLog(module = "资产管理", business = "删除资产", type = OperType.DELETE)
    @DeleteMapping("/deleteAssetById/{assetId}")
    public AjaxResult deleteAssetById(@PathVariable Long assetId) {
        return toAjax(assetService.deleteAssetById(assetId));
    }
}
