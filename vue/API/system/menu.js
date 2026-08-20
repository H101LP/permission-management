import request from "@/utils/requst.js";

//查询全部菜单
export function selectMenuList(quer) {
    return request({
        url: "/system/menu/selectMenuList", //后端接口地址
        method: "get",//请求方法(严格遵守后端)
        params: quer //请求参数
    })
}

//新增菜单
export function insertMenu(data) {
    return request({
        url: "/system/menu/insertMenu", //后端接口地址
        method: "post",//请求方法(严格遵守后端)
        data: data //请求参数
    })
}

//根据菜单ID 查询菜单
export function selectMenuByMenuId(menuId) {
    return request({
        url: "/system/menu/selectMenuByMenuId/" + menuId, //后端接口地址
        method: "get",//请求方法(严格遵守后端)
    })
}

//修改菜单
export function updateMenu(data) {
    return request({
        url: "/system/menu/updateMenu", //后端接口地址
        method: "put",//请求方法(严格遵守后端)
        data: data //请求参数
    })
}

//删除菜单
export function deleteMenuByMenuId(menuId) {
    return request({
        url: "/system/menu/deleteMenuByMenuId/" + menuId, //后端接口地址
        method: "delete",//请求方法(严格遵守后端)
    })
}

//根据角色ID查询对应的菜单树
export function selectRoleMenuTree(roleId) {
    return request({
        url: "/system/menu/selectRoleMenuTree/" + roleId, //后端接口地址
        method: "get",//请求方法(严格遵守后端)
    })
}

//查询前端所需要的下拉树结构
export function selectRoleMenusTree() {
    return request({
        url: "/system/menu/selectRoleMenusTree", //后端接口地址
        method: "get",//请求方法(严格遵守后端)
    })
}