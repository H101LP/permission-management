// permission.js 完整重写（vue-router 5：不再使用 next()，全部 return）

import router from './router'
import { getToken } from "@/utils/auth.js"
import useUserStore from "@/stores/modules/userStore.js"
import { isReLogin } from "@/utils/requst.js"
import { ElMessage } from "element-plus"
import useRouteStore from "@/stores/modules/routeStore.js"

const whiteList = ['/login', '/register']

router.beforeEach(async (to) => {
  const token = getToken()

  // ---------- 1. 未登录 ----------
  if (!token) {
    if (whiteList.includes(to.path)) return true          // 白名单放行
    return { path: '/login', query: { redirect: to.fullPath }, replace: true }
  }

  // ---------- 2. 已登录还去登录/注册页 ----------
  if (to.path === '/login' || to.path === '/register') {
    return { path: '/', replace: true }                   // 回首页（守卫会按角色二次跳转）
  }

  // ---------- 3. 首次进入：拉用户信息 + 动态路由 ----------
  const routeStore = useRouteStore()
  if (routeStore.sidebarRoutes.length === 0) {
    isReLogin.show = true
    try {
      await useUserStore().getInfo()   // 只取副作用（写入 store），不再需要返回值
      const accessRoutes = await routeStore.generateRoutes()
      accessRoutes.forEach(route => router.addRoute(route)) // 动态添加（追加到末尾）

      // 所有角色统一进后台（/index），不再按角色分流到前台装饰页。
      // 三层隔离各自独立生效：
      //   菜单级 —— /getRouters 按 role_menu 过滤，无权限的菜单不进侧边栏
      //   数据级 —— DataScopeInterceptor 按 role.data_scope 过滤行
      //   按钮级 —— v-has-perm 把无权限的按钮从 DOM 摘掉
      // 这样普通用户看到的是"同一套后台界面，但只有他能用的那部分"。

      // 关键：路由刚添加完，用 fullPath 重新导航到当前目标。
      // 不要用 next({ ...to })，展开 to 会把 matched/meta 等内部字段带进 location，容易解析异常
      return to.fullPath
    } catch (err) {
      await useUserStore().logOut()
      ElMessage.error('登录状态已过期，请重新登录')
      return { path: '/login', replace: true }
    } finally {
      isReLogin.show = false
    }
  }

  // ---------- 4. 已加载过动态路由 ----------
  return true
})
