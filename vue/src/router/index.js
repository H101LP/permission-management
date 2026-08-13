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
  },
  {
    path:'/',
    component: Layout,
    //重定向
    redirect:'index',
    children: [
      {
        path: 'index',
        name: 'Index',
        meta: { title: '首页' }
      },
    ]
  },
  {
    path:'/user',
    component: Layout,
    hidden:true,
    children: [
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/system/user/profile'),
        meta: { title: '个人中心' }
      },
    ]
  },
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
