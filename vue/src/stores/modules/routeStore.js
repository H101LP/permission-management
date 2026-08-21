import {defineStore} from "pinia";
import {getRouters} from "~/API/system/menu.js";
import {constantRouters} from "@/router/index.js";
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
                return new Promise(resolve => {
                    //调用API获取用户专属路由
                    getRouters().then(res => {
                        //深拷贝一份数据 避免修改原始数据
                        const routerData = JSON.parse(JSON.stringify(res.data));
                        //转换数据格式 ：将数据转换为路由格式
                        const sidebarRoutes = convertToRoutes(routerData);
                        //保存到仓库
                        this.setRoutes(sidebarRoutes)
                        //侧边栏显示  包括 固定路由和用户路由
                        this.setSidebarRoutes(constantRouters.concat(sidebarRoutes))
                        //告诉调用者处理完毕
                        resolve(sidebarRoutes);
                    })
                })
            }
        }
    }
)
/**
 * 核心数据转换方法 把后端返回的路由数据转换为前端路由能用的格式
 * @param routes 后端返回的路由数据
 */
const convertToRoutes = (routes) =>{
    //对每个路由对象进行处理
    return routes.map(route => {
        //把字符串转换为真正的组件
        if(route.component ){
         if(route.component === 'Layout'){
             //如果是Layout组件 则使用Layout组件
             route.component = Layout;
         }else {
             //如果是页面组件
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
        //从完整路径中提取相对路径，并去掉开头的斜杠，保持和后端 component 字段格式一致
        const dir = path.split('views')[1].split('.vue')[0].replace(/^\//, '')
        if(dir === view){
            return () => modules[path]()
        }
    }
    return null;
}


//导出路由仓库
export default useRouteStore;

