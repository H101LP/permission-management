import request from "@/utils/requst.js";

// 查询操作日志列表
export function selectOperationLogList(query) {
    return request({
        url: "/monitor/operlog/selectList",
        method: "get",
        params: query
    })
}
