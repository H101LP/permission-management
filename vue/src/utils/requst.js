import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";
import { getToken } from "@/utils/auth.js";
import useUserStore from "@/stores/modules/userStore.js";

// ==================== 全局状态 ====================
export let isReLogin = { show: false };

// ==================== Axios 默认配置 ====================
axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8';

// ==================== 创建 Axios 实例 ====================
const service = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API, // 从环境变量读取
    timeout: 20000,
});

// ==================== 处理重新登录弹窗 ====================
const handleReLogin = () => {
    if (isReLogin.show) return;
    isReLogin.show = true;

    ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning',
    })
        .then(() => {
            //用户点击了重新登录
            isReLogin.show = false;
            //调用重新登录的方法
            useUserStore().logOut().then(() => {
                //跳转到登录页
                location.href = '/login';
            })
        })
        .catch(() => {
            isReLogin.show = false;
        });
};

// ==================== 请求拦截器 ====================
service.interceptors.request.use(
    // ----- 成功拦截（发送前处理） -----
    (config) => {
        // 1. 是否需要携带 Token（默认需要，除非显式设置 headers.isToken = false）
        const isToken = config.headers?.isToken !== false;
        // 2. 是否需要防重复提交（默认开启，除非显式设置 headers.isRepeatSubmit = false）
        const isRepeatSubmit = config.headers?.isRepeatSubmit !== false;

        // 添加 Token
        if (isToken && getToken()) {
            config.headers['Authorization'] = 'Bearer ' + getToken();
        }

        // 防重复提交（针对 POST / PUT）
        if (!isRepeatSubmit && ['post', 'put'].includes(config.method)) {
            const requestObj = {
                url: config.url,
                data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
                time: Date.now(),
            };

            const sessionValue = sessionStorage.getItem('sessionObj');
            const sessionObj = sessionValue ? JSON.parse(sessionValue) : null;

            if (sessionObj) {
                const { url: s_url, data: s_data, time: s_time } = sessionObj;
                const interval = 1000; // 1秒内重复视为重复提交

                if (
                    s_url === requestObj.url &&
                    s_data === requestObj.data &&
                    s_time > requestObj.time - interval
                ) {
                    ElMessage.error('请勿重复提交');
                    return Promise.reject(new Error('请勿重复提交'));
                }
            }

            // 保存本次请求记录
            sessionStorage.setItem('sessionObj', JSON.stringify(requestObj));
        }

        return config;
    },

    // ----- 错误拦截（请求发送失败） -----
    (error) => {
        ElMessage.error(error.message || '请求发送失败');
        return Promise.reject(error);
    }
);
/**
 * 第四步响应拦截器 收到响应后干的
 */
service.interceptors.response.use(
    // ----- 成功拦截（响应成功处理） -----
    res => {
        if(['blob'].includes(res.request.responseType)){
            //直接返回原始数据
            return res.data;
        }
        //从响应数据取出业务状态吗和提示信息
        const code = res.data.code || 200;
        const msg = res.data.msg || '操作失败';
        //根据不同的状态码进行不同的处理
        if(code === 401){
            handleReLogin();
            return Promise.reject('登录已过期 请重新登录')
        }
        //如果不是200
        if(code !==200){
            ElMessage.error(msg);
            return Promise.reject(new Error(msg));
        }
        //一切正常
        return res.data;
    },error =>{
        //这里处理网络错误 如500 404
        let {message, response} = error;
        //如果是401错误
        if(response?.status === 401){
            handleReLogin();
            return Promise.reject('登录已过期 请重新登录')
        }
        //错误信息翻译
        const errMap = {
            'Network Error': '网络错误',
            'timeout': '接口请求超时',
            'Request failed with status code': '系统接口' +message.substr(message,length -3 )+'异常',
        }
        //匹配错误信息
        Object.keys(errMap).forEach(key=>{
            if(message.includes(key)){
                message = errMap[key];
            }
        })
        //显示错误提示
        ElMessage.error(message);
        return Promise.reject(error);
    }
);

//导出实例
export default service