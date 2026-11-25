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
import useRouteStore from "@/stores/modules/routeStore.js";
const route = useRoute();

const routeStore = useRouteStore();


//动态路由数据
const sidebarRoutes = computed(()=> routeStore.sidebarRoutes)
/*//模拟静态路由数据
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
])*/



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

/* 以下替换原 67-77 行 */
.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 4px 0;
  border-radius: 8px;
  color: #d5dce8;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #334155;
  color: #ffffff;
  position: relative;
}

.sidebar-menu :deep(.el-menu-item.is-active)::before {
  content: "";
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: #93c5fd;
}

/* ⚠️ 这一条不能加 .sidebar-menu 前缀：
   el-scrollbar 是 .sidebar-menu（el-menu 自身）的父级，不是它的后代 */
:deep(.el-scrollbar__wrap) {
  background: transparent;
}
</style>