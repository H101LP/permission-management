import request from "@/utils/requst.js";

// 查询工单列表
export function selectTicketList(query) {
    return request({
        url: "/business/ticket/selectTicketList",
        method: "get",
        params: query
    });
}

// 查询工单详情
export function selectTicketById(ticketId) {
    return request({
        url: "/business/ticket/selectTicketById/" + ticketId,
        method: "get"
    });
}

// 新增工单
export function insertTicket(data) {
    return request({
        url: "/business/ticket/insertTicket",
        method: "post",
        data: data
    });
}

// 修改工单
export function updateTicket(data) {
    return request({
        url: "/business/ticket/updateTicket",
        method: "put",
        data: data
    });
}

// 删除工单
export function deleteTicketById(ticketId) {
    return request({
        url: "/business/ticket/deleteTicketById/" + ticketId,
        method: "delete"
    });
}
