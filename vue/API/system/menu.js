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