import request from "@/utils/requst.js";

//查询全部菜单
export function selectMenuList(quer) {
    return request({
        url: "/system/menu/selectMenuList", //后端接口地址
        method: "get",//请求方法(严格遵守后端)
        params: quer //请求参数
    })
}