<template>
<div>
<!--顶部导航栏-->
  <el-header style="background: #ffffff;height:50px;display: flex;position: fixed ;
  left: 0;right: 0;top: 0; box-shadow: 0 1px 4px rgba(0,23,45,0.08);">
    <div style="display: flex;align-items: center;">
      <img :src="logo" alt="logo" style="width: 40px;height: 40px;margin-right: 10px">
      <span style="font-size: 28px;font-weight: bold">后台管理端</span>
    </div>
      <!-- 面包屑 -->
    <div style="flex: 1;display: flex;margin-left: 20px;align-items: center;">
      <el-breadcrumb separator="/">
      <el-breadcrumb-item v-for="(item,index) in breadItems" :key="index">
        <span>{{ item.meta.title }}</span>
      </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
      <!--用户信息区-->
    <div style="display: flex;align-items: center;">
      <span style="margin-right: 15px">您好:{{ userStore.name }}</span>

      <el-button link @click="logout" style="display: flex;padding: 0 20px;align-items: center;">
        <SvgIcon icon-class="logout" />
        <span>退出登录</span>
      </el-button>

      <el-dropdown trigger="click" style="cursor: pointer">
    <div>
     <img :src="userStore.avatar" style="width: 36px;height: 36px">
      <el-icon class="el-icon--right">
        <arrow-down />
      </el-icon>
    </div>
        <template #dropdown>
          <el-dropdown-menu>
            <router-link to="/profile/user">
              <el-dropdown-item>个人中心</el-dropdown-item>
            </router-link>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

    </div>
  </el-header>
<!--主内容区-->
  <main style="margin-left: 240px; position: relative;top: 50px;">
    <AppMain/>
  </main>
<!-- 侧边栏 -->
  <el-aside style="width: 240px; position: fixed;top: 51px;bottom: 0">
    <SidBar />
  </el-aside>

</div>
</template>

<script setup>
import logo from '@/assets/logo/logo.jpg'
import {onMounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import useUserStore from "@/stores/modules/userStore.js";
import {ElMessage, ElMessageBox} from "element-plus";
import SvgIcon from "@/components/SvgIcon/index.vue";
import AppMain from "@/views/layout/components/AppMain.vue";
import SidBar from "./components/Sidebar"

//用户状态信息
const userStore = useUserStore()


//面包屑数组
const breadItems = ref([])

const route = useRoute();
//退出登录方法
const logout = () => {
  ElMessageBox.confirm(
      '确定要退出登录吗？',
      '系统提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        userStore.logOut().then(() => {
          location.href = '/login'
        })
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: '退出登录已取消',
        })
      })
}


//判断当前路由是否是首页
const isDashboard = (route) =>{
  const name = route && route.name
  if(!name) return false
  return name.trim() === 'Index'
}


//获取面包屑
const getBread = () => {
  //1从当前的路由匹配数组中过滤出需要显示的面包屑
  const matched = route.matched.filter(
      (item) => {return item.meta && item.meta.title}
  )
  //2如果不是首页 就在最前面添加首页项
  if(!isDashboard(route)){
    matched.unshift({
      path: '/',
      meta: {title: '首页'}
    })
  }
  //3 更新面包屑数据
  breadItems.value = matched
}


//组件挂载完成后执行
onMounted(()=>{
  //初始化面包屑
  getBread();
})
//监听路由变化
watch(()=> route.path,()=>{
  getBread();
})

</script>


<style scoped>
:deep(.el-header) {
  height: var(--app-header-height) !important;          /* 覆盖内联 50px */
  background: rgba(255, 255, 255, 0.94) !important;     /* 覆盖内联 #ffffff */
  box-shadow: none !important;                          /* 覆盖内联阴影 */
  border-bottom: 1px solid var(--border-default);
  z-index: 1000;
  padding: 0 24px;
}

/* 覆盖内联 width/height: 40px */
:deep(.el-header) > div:first-child > img {
  width: 34px !important;
  height: 34px !important;
  border-radius: 9px;
}

/* 覆盖内联 font-size: 28px; font-weight: bold */
:deep(.el-header) > div:first-child > span {
  font-size: 18px !important;
  font-weight: 600 !important;
}

:deep(.el-header .el-dropdown img) {
  border-radius: 50%;
  object-fit: cover;
}

/* 覆盖内联 top: 50px; margin-left: 240px */
main {
  top: var(--app-header-height) !important;
  margin-left: var(--app-sidebar-width) !important;
  min-height: calc(100vh - var(--app-header-height));
  background: var(--bg-page);
}

/* 覆盖内联 top: 51px */
:deep(.el-aside) {
  top: var(--app-header-height) !important;
  background: #111827;
}
</style>