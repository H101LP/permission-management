import request from "@/utils/requst.js";


//查询所有角色列表
export function selectAllRole(){
    return request({
        url:"/system/role/selectAllRole", //后端接口地址
        method:"get",//请求方法(严格遵守后端)
    })
}
//查询角色列表
export function selectRoleList(query) {
    return request({
        url: "/system/role/selectRoleList", //后端接口地址
        method: "get",//请求方法(严格遵守后端)
        params: query //请求参数
    })
}