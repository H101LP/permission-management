package com.fast.system.mapper;

import com.fast.system.annotation.DataScope;
import com.fast.system.domain.Asset;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AssetMapper {
    // 查询资产列表
    @DataScope(deptAlias = "a")          // 别名必须与 XML 的 FROM asset a 一致
    List<Asset> selectAssetList(Asset asset);

    // 查询资产详细
    Asset selectAssetById(Long assetId);

    // 新增资产
    int insertAsset(Asset asset);

    // 修改资产
    int updateAsset(Asset asset);

    // 删除资产
    int deleteAssetById(Long assetId);
}
