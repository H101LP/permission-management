/**
 * 整个前端项目的插件工厂 用于统一管理插件
 */

//导入vue的vite插件
import vue from '@vitejs/plugin-vue'

export default function createVitePlugins (viteEnv,isBuild = false){
    //创建插件数组 初始化包含vue插件
    //vue() 使用vite的vue插件  支持vue3的单文件组件
    const vitePlugins = [vue()]
    //添加自动导入插件
    vitePlugins.push()


    //返回插件数组
    return vitePlugins
}