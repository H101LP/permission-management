import request from "@/utils/requst.js";
//登录方法
export function register(data){
    return request({
        url:"/register", //后端接口地址
        headers:{ //请求头
            isToken:false, //是否需要token
        },
        method:"post",//请求方法(严格遵守后端)
        data:data //请求参数
    })
}