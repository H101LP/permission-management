import request from "@/utils/requst.js";
//修改个人信息的结构
export function updateProfile(data){
    return request({
        url:"/system/user/profile", //后端接口地址
        method:"put",//请求方法(严格遵守后端)
        data:data //请求参数
    })
}