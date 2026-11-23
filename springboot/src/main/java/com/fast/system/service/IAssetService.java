package com.fast.system.service;

import com.fast.system.domain.Asset;

import java.util.List;

/**
 * 资产台账业务接口
 */
public interface IAssetService {

    /** 查询资产列表（受数据权限控制） */
    List<Asset> selectAssetList(Asset asset);

    /** 根据ID查询资产详情 */
    Asset selectAssetById(Long assetId);

    /** 新增资产 */
    int insertAsset(Asset asset);

    /** 修改资产 */
    int updateAsset(Asset asset);

    /** 删除资产 */
    int deleteAssetById(Long assetId);
}
