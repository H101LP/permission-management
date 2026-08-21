//导入路由实例
import router from './router'
import {getToken} from "@/utils/auth.js";
import useUserStore from "@/stores/modules/userStore.js";
import {isReLogin} from "@/utils/requst.js";
import {ElMessage} from "element-plus";
import useRouteStore from "@/stores/modules/routeStore.js";
//白名单 定义不需要登录就可以访问的界面
const whiteList = ['/login', '/register']

//判断是否在白名单
function isWhiteList(path) {
  return whiteList.includes(path)
}
//全局路由守卫
router.beforeEach((to, from, next) => {
 //先检查用户是否有token
  if(getToken()){
    //情况1.1 ：用户有token
    if(to.path === '/login' || to.path === '/register'){
      //已经登录还要去登录页
      next({path:'/'})
    }
    //1.2 访问白名单页面
    else if(isWhiteList(to.path)){
      next();//允许访问
    }
    //情况1.3 用户已登录 访问需要权限的页面
    else{
      if(useRouteStore().sidebarRoutes.length === 0){
        //用户信息为空 需要先获取用户信息
        //设置正在重新登录的标志
        isReLogin.show = true;
        //调用获取用户信息的方法
        useUserStore().getInfo().then(res =>{
          //清除标志
          isReLogin.show = false;
// 调用路由状态工具方法（只调用一次）
          useRouteStore().generateRoutes().then(accessRoutes => {
            // 动态添加路由
            accessRoutes.forEach(route => {
              router.addRoute(route)
            })

            // ✅ 获取角色名称（根据你的实际数据结构调整）
            // 如果你在 userStore.getInfo 里已经剥过层了，直接用 res.roleName
            // 如果没剥过，用 res.data.roleName
            const useRoleName = res.roleName || res.data?.roleName || 'user'

            // 根据角色确定跳转路径
            let redirectPath = to.path
            if (to.path === '/' || to.path === '/index') {
              // 目前 admin 和 user 都跳转 /index，后续可以根据角色扩展
              redirectPath = '/index'
            }

            // ✅ 只调用一次 next！
            if (redirectPath !== to.path) {
              next({ path: redirectPath, replace: true })
            } else {
              next({ ...to, replace: true })
            }
          })

        }).catch(err =>{
          //获取用户信息失败 token可能过期
          useUserStore().logOut().then(() => {
            ElMessage.error(err);
            //跳转到登录页
            next({path: '/login'})
          })

        })
      }else {
        //用户信息已存在
        next();
      }
    }

  }else{
    //没token
    //在不在白名单
    if(isWhiteList(to.path)){
      next();
    }else{
      //不在白名单 跳转到登录页
      next({path: '/login'})
    }
  }
})
