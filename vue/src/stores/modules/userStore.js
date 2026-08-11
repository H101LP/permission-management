import {defineStore} from "pinia";
import {getToken, setToken} from "@/utils/auth.js";
import {getInfo, login, logout} from "~/API/login.js";
import defaultAvatar from '@/assets/images/profile.jpg'


const useUserStore = defineStore(
    'user', //全局必须唯一
    {
        //第一部分 状态定义
        state: () => ({
            token: getToken(), //登录后的ID
            id: '', //用户ID
            name: '', //用户名
            avatar: '',//用户头像地址
        }),
        //第二部分:操作方法 （处理数据函数）
        actions: {
            //登录方法
            login(userInfo) {
                //返回一个Promise
                return new Promise((resolve, reject) => {
                    //调用登录接口
                    login(userInfo).then(res => {
                        //登陆成功
                        //保存token到本地存储
                        setToken(res.token)
                        //更新Store的token
                        this.token = res.token;
                        //表示成功
                        resolve();
                    }).catch(error => {
                        reject(error);
                    });
                });
            },
            //获取用户详细信息
            getInfo() {
                //返回一个Promise
                return new Promise((resolve, reject) => {
                    //调用获取用户信息接口
                    getInfo().then(res => {
                        const user = res.data;
                        //处理头像地址
                        let avater = user.avatar || " ";
                        //判断头像地址是否完整
                        if(avater.indexOf('http://') === -1 && avater.indexOf('https://') === -1){
                            //头像地址不完整 是相对路径
                            if(avater){
                                avater = import.meta.env.VITE_APP_BASE_API + avater;
                            }else {
                                //如果没用头像 就使用默认头像
                                avater = defaultAvatar;
                            }
                        }
                        //更新用户信息
                        this.id = user.id;
                        this.name = user.name;
                        this.avatar = avater;
                        //表示成功
                        resolve(res); //返回完整响应数据
                    }).catch(error => {
                        //获取失败
                        reject(error);
                    });
                });
            },
            //退出登录
            logOut(){
                return new Promise((resolve, reject) => {
                    //调用退出登录接口 (告诉服务器退出)
                    logout(this.token).then(res =>{
                        //退出成功
                        //清空token
                        this.token = ' ';
                        //删除本地存储token
                        removeToken();
                        //调用成功
                        resolve();
                    }).catch(error =>{
                        reject(error);
                    })

                })

            }
        },
    }
)

//导出
export default useUserStore;
