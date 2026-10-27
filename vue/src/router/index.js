import { createRouter, createWebHistory } from 'vue-router'
//导入布局组件
import Layout from '@/views/layout/index.vue'
//路由配置
export const constantRouters = [
  {
    //登录页面
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true,
  },
  {
    //注册页面
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true,
  },{
    // ⚠️ 只保留一个纯重定向，不要加 component: Layout！
    // 这样访问 / 时，路由守卫会自动把它导到后端的动态路由 /index 去
    path: '/',
    redirect: '/index',
    hidden: true
  },
 /* {
    path:'/',
    component: Layout,
    redirect:'index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/system/index'),
        name: 'Index',
        meta: { title: '首页', icon: '首页' }
      },
    ]
  },*/
  {
    // ⚠️ 把后台个人中心的路径改掉，不要和前台的 /user 冲突
    path:'/profile',
    component: Layout,
    hidden:true,
    children: [
      {
        path: 'user',
        name: 'Profile',
        component: () => import('@/views/system/user/profile'),
        meta: { title: '个人中心' }
      },
    ]
  },
  {
    // 前台用户端
    path: '/userPage', // ⚠️ 加上 Page，避免和后台路由冲突
    component: () => import('@/views/userPage/index.vue'),
    redirect: '/userPage/home',
    hidden: true,
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/userPage/home.vue'),
        hidden: true,
        meta: { title: '首页' , icon: '首页' }
      },
      {
        path:'self',
        component: () => import('@/views/system/user/profile'),
        hidden: true,
      }
    ]
  },
  // ⚠️ 404 兜底必须放在数组的【最后一项】！
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/404'),
    hidden: true
  }
]

//创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes: constantRouters,
  //滚动行为配置
  scrollBehavior(to, from, savedPosition) {
    if(savedPosition){
      //如果有保存的位置 就恢复到之前的位置
      return savedPosition
    }
  }
})

export default router
