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

//根据角色ID查询角色信息
export function selectRoleById(roleId) {
    return request({
        url: "/system/role/selectRoleById/" + roleId, //后端接口地址
        method: "get",//请求方法(严格遵守后端)
    })
}
//新增角色信息
export function insertRole(data) {
    return request({
        url: "/system/role/insertRole", //后端接口地址
        method: "post",//请求方法(严格遵守后端)
        data: data //请求参数
    })
}

//修改角色信息
export function updateRole(data) {
    return request({
        url: "/system/role/updateRole", //后端接口地址
        method: "post",//请求方法(严格遵守后端)
        data: data //请求参数
    })
}