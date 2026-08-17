import request from "@/utils/requst.js";
//修改个人信息的结构
export function updateProfile(data){
    return request({
        url:"/system/user/profile", //后端接口地址
        method:"put",//请求方法(严格遵守后端)
        data:data //请求参数
    })
}

//重置密码
export function updatePwd(data){
    return request({
        url:"/system/user/profile/updatePwd", //后端接口地址
        method:"put",//请求方法(严格遵守后端)
        data:data //请求参数
    })
}

//为什么要使用   params: query  而不是 data: query ？？ 因为GET请求不能使用data，只能使用params

//查询用户列表
export function selectUserList(query){
    return request({
        url:"/system/user/selectUserList", //后端接口地址
        method:"get",//请求方法(严格遵守后端)
        params: query //请求参数
    })
}
//查询用户列表
export function selectUserById(userId){
    return request({
        url:"/system/user/selectUserById/" + userId, //后端接口地址
        method:"get",//请求方法(严格遵守后端)
    })
}


//新增用户
export function insertUser(data){
    return request({
        url:"/system/user/insertUser", //后端接口地址
        method:"post",//请求方法(严格遵守后端)
        data: data //请求参数
    })
}
//修改用户
export function updateUser(data){
    return request({
        url:"/system/user/updateUser", //后端接口地址
        method:"put",//请求方法(严格遵守后端)
        data: data //请求参数
    })
}

//删除用户
export function deleteUserByUserIds(userIds){
    return request({
        url:"/system/user/deleteUserByUserIds/" + userIds, //后端接口地址
        method:"delete",//请求方法(严格遵守后端)
    })
}