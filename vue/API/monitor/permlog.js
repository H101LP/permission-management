import request from "@/utils/requst.js";

//查询权限变更日志列表
export function selectPermissionLogList(query) {
    return request({
        url: "/monitor/permlog/selectList", //后端接口地址
        method: "get",//请求方法(严格遵守后端)
        params: query //请求参数：GET 只能用 params，不能用 data
    })
}
