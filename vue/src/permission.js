//导入路由实例
import router from './router'
//白名单 定义不需要登录就可以访问的界面
const whiteList = ['/login', '/register']

//判断是否在白名单
function isWhiteList(path) {
  return whiteList.includes(path)
}
//全局路由守卫
router.beforeEach((to, from, next) => {
  if (isWhiteList(to.path)) {
    next()
  } else {
    next('/login')
  }
})
