<template>
  <el-scrollbar>
    <el-menu :default-active="activeMenu" class="sidebar-menu">
      <Sidebaritem v-for="(route,index) in sidebarRoutes"
                   :key = "route.path + index"
                   :item="route"
                   :base-path="route.path"/>
    </el-menu>
  </el-scrollbar>
</template>
<script setup>
//计算当前应该高亮哪个菜单项
import {computed, ref} from "vue";
import {useRoute} from "vue-router";
import Sidebaritem from "@/views/layout/components/Sidebar/Sidebaritem.vue";
const route = useRoute();

//模拟静态路由数据
const sidebarRoutes = ref([
    {
      path: '/index',
      meta: {title: '首页' ,icon:'首页',hidden: false}
    },
  {
    path: '/system',
    meta: {title: '系统管理' ,icon:'系统管理',hidden: false},
    children: [
      {
        path: 'user',
        meta: {title: '用户管理' ,icon:'用户管理',hidden: false}
      },
      {
        path: 'role',
        meta: {title: '角色管理' ,icon:'角色管理',hidden: false}
      },
      {
        path: 'menu',
        meta: {title: '菜单管理' ,icon:'菜单管理',hidden: false}
      },
    ]
  },
])



const activeMenu = computed(() => {
  //从当前路由对象解构数据
  const {meta, path} = route;
  return meta.activeMenu || path;
})


</script>


<style scoped>
.sidebar-menu{
  padding: 8px 0;
  border-right: none;
}
.sidebar-menu :deep(.el-menu-item){
  color: #666;
  height: 55px;
}
.sidebar-menu :deep(.el-menu-item).is-active{
  background: var(--el-color-primary) !important;
  color: white;
  position: relative;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}
</style>