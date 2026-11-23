package com.fast.system.service.impl;

import com.fast.system.domain.Asset;
import com.fast.system.exception.BusinessException;
import com.fast.system.mapper.AssetMapper;
import com.fast.system.service.IAssetService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 资产台账业务实现
 * 单表增删改，不加 @Transactional   (事务)
 */
@Service
public class AssetServiceImpl implements IAssetService {

    @Resource
    private AssetMapper assetMapper;

    @Override
    public List<Asset> selectAssetList(Asset asset) {
        return assetMapper.selectAssetList(asset);
    }

    @Override
    public Asset selectAssetById(Long assetId) {
        return assetMapper.selectAssetById(assetId);
    }

    @Override
    public int insertAsset(Asset asset) {
        if (asset.getAssetName() == null || asset.getAssetName().trim().isEmpty()) {
            throw new BusinessException("资产名称不能为空");
        }
        // dept_id 是 NOT NULL：前端没选部门时落到当前登录用户所在部门
        if (asset.getDeptId() == null) {
            asset.setDeptId(SecurityUtils.getLoginUser().getDeptId());
        }
        if (asset.getStatus() == null) {
            asset.setStatus(0);
        }
        return assetMapper.insertAsset(asset);
    }

    @Override
    public int updateAsset(Asset asset) {
        if (asset.getAssetId() == null) {
            throw new BusinessException("资产ID不能为空");
        }
        // XML 里 asset_name 恒定更新，这里必须拦住空值
        if (asset.getAssetName() == null || asset.getAssetName().trim().isEmpty()) {
            throw new BusinessException("资产名称不能为空");
        }
        return assetMapper.updateAsset(asset);
    }

    @Override
    public int deleteAssetById(Long assetId) {
        return assetMapper.deleteAssetById(assetId);
    }
}
