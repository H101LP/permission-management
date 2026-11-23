import request from "@/utils/requst.js";

// 查询资产列表（分页 + 条件）
export function selectAssetList(query) {
    return request({
        url: "/business/asset/selectAssetList",
        method: "get",
        params: query
    });
}

// 查询资产详情
export function selectAssetById(assetId) {
    return request({
        url: "/business/asset/selectAssetById/" + assetId,
        method: "get"
    });
}

// 新增资产
export function insertAsset(data) {
    return request({
        url: "/business/asset/insertAsset",
        method: "post",
        data: data
    });
}

// 修改资产
export function updateAsset(data) {
    return request({
        url: "/business/asset/updateAsset",
        method: "put",
        data: data
    });
}

// 删除资产
export function deleteAssetById(assetId) {
    return request({
        url: "/business/asset/deleteAssetById/" + assetId,
        method: "delete"
    });
}
