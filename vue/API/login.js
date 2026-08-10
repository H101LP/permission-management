import requst from "@/utils/requst.js";
//登录方法
export function login(data){
    return requst({
        url:"/login", //后端接口地址
        headers:{ //请求头
          isToken:false, //是否需要token
          repeatSubmit: false  //是否需要防止重复提交
        },
        method:"post",//请求方法(严格遵守后端)
        data:data //请求参数
    })
}

//查询
export function getInfo(data){
    return requst({
        url:"/getInfo", //后端接口地址
        method:"get",//请求方法(严格遵守后端)
    })
}

//退出登录
export function logout(data){
    return requst({
        url:"/logout", //后端接口地址
        method:"POST",//请求方法(严格遵守后端)
    })
}