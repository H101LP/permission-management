import request from "@/utils/requst.js";

// 查询订单列表
export function selectOrderList(query) {
    return request({
        url: "/business/order/selectOrderList",
        method: "get",
        params: query
    });
}

// 查询订单详情
export function selectOrderById(orderId) {
    return request({
        url: "/business/order/selectOrderById/" + orderId,
        method: "get"
    });
}

// 新增订单
export function insertOrder(data) {
    return request({
        url: "/business/order/insertOrder",
        method: "post",
        data: data
    });
}

// 修改订单
export function updateOrder(data) {
    return request({
        url: "/business/order/updateOrder",
        method: "put",
        data: data
    });
}

// 删除订单
export function deleteOrderById(orderId) {
    return request({
        url: "/business/order/deleteOrderById/" + orderId,
        method: "delete"
    });
}
