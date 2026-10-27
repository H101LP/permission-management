import {defineStore} from "pinia";
import {getRouters} from "~/API/system/menu.js";
import Layout from '@/views/layout/index.vue'

//自动导入views目录下所有vue文件
const modules = import.meta.glob('./../../views/**/*.vue')

const useRouteStore = defineStore(
    'permission', //全局唯一
    {
        //第一部分 状态定义
        state: () => ({
            routes: [], // 存储所有路由（包括基础路由和动态路由即用户专属路由）
            sidebarRoutes: [], // 存储侧边栏路由 用于渲染侧边栏
        }),
        actions:{
            //设置路由数据  --更新仓库里的路由列表
            setRoutes(routes){
                this.routes = routes;
            },
            //设置侧边栏路由数据  专门用来显示菜单的路由
            setSidebarRoutes(routes){
                this.sidebarRoutes = routes;
            },

            //核心方法 生成用户的专属路由
            generateRoutes(){
                return new Promise((resolve, reject) => {
                    //调用API获取用户专属路由
                    getRouters().then(res => {
                        //深拷贝一份数据 避免修改原始数据
                        const routerData = JSON.parse(JSON.stringify(res.data));

                        //转换数据格式 ：将数据转换为路由格式
                        const sidebarRoutes = convertToRoutes(routerData);

                        //保存到仓库
                        this.setRoutes(sidebarRoutes)

                        // ⚠️ 核心修改：侧边栏直接使用后端返回的菜单，不要再 concat 静态路由了！
                        this.setSidebarRoutes(sidebarRoutes)

                        //告诉调用者处理完毕
                        resolve(sidebarRoutes);
                    }).catch(err => reject(err))
                })
            }
        }
    }
)

/**
 * 核心数据转换方法 把后端返回的路由数据转换为前端路由能用的格式
 */
const convertToRoutes = (routes) =>{
    return routes.map(route => {
        //把字符串转换为真正的组件
        if(route.component ){
            if(route.component === 'Layout'){
                route.component = Layout;
            }else {
                route.component = loadView(route.component)
            }
        }
        //2.递归处理子路由
        if(route.children && route.children.length > 0){
            route.children = convertToRoutes(route.children);
        }else{
            delete route.children;
        }
        return route;
    })
}

//动态加载视图组件
const loadView = (view) => {
    for (const path in modules){
        const dir = path.split('views')[1].split('.vue')[0].replace(/^\//, '')
        if(dir === view){
            return () => modules[path]()
        }
    }
    // 🚨 如果没找到组件，必须报错，否则会导致 Vue Router 崩溃白屏！
    console.error('❌ 致命错误：找不到前端组件文件，后端传递的 component 路径是:', view);
    return () => import('@/views/404'); // 兜底返回404页面，防止整个路由崩溃
}

export default useRouteStore;