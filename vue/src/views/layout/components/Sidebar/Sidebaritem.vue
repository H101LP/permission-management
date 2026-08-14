<template>
<!--第一步 检查当前菜单项是否应该显示-->
  <div v-if="!item.hidden">
    <!--情况1 只有一个子项时显示-->
    <template v-if="shouldShowSingleItem">
    <app-link :to="singletItemPath">
      <el-menu-item :index="singletItemPath">
        <svg-icon :icon-class="onlyOnechild.meta.icon || (item.meta && item.meta.icon)"
                  style="margin-right: 10px"/>
        <template #title>
          <span style="margin-left: 2px;">
            {{ onlyOnechild.meta.title}}
          </span>
        </template>
      </el-menu-item>
    </app-link>
    </template>
    <!--情况2 当菜单项有多个子项时显示-->
    <el-sub-menu v-else :index="resolvePath(item.path)" teleported>
      <template v-if="item.meta" #title>
        <svg-icon :icon-class="item.meta.icon" style="margin-right: 10px"/>
        <span style="margin-left: 2px;">{{ item.meta.title}}</span>
      </template>
        <!--递归显示子菜单项-->
        <sidebaritem v-for="child in item.children"
                     :key ="child.path"
                     :item="child"
                     :base-path="resolvePath(child.path)"
                     is-next/>

    </el-sub-menu>

  </div>
</template>

<script setup>
import {computed} from "vue";
import AppLink from "@/views/layout/components/Sidebar/AppLink.vue";
import SvgIcon from "@/components/SvgIcon/index.vue";

const props = defineProps({
  //菜单项的数据对象
  item: {
    type: Object,
    required: true
  },
  //是否是嵌套调用
  isNext:{
    type: Boolean,
    default: false
  },
  //基础路径 用于拼接完整的路由路径
  basePath: {
    type: String,
    default: ''
  }
})

//计算当前菜单项唯一显示的子项
const onlyOnechild = computed(()=>{
  //获取当前菜单项的子项数组 如果没有子项就使用空数组
  const children = props.item.children || []
  //过滤出所有不需要隐藏的子项
  const showingChildren = children.filter(item => !item.hidden)
  //情况1 如果只有一个需要显示的子项
  if (showingChildren.length === 1) {
    return showingChildren[0]
  }
//情况2 如果没有需要显示的子项目
  if (showingChildren.length === 0){
    return {
      ...props.item, //复制父项的所有属性
      path: '',
      noshowingChildren: true //没有需要显示的子项
    }
  }
  //情况3 如果有多个需要显示的子项
  return null;
})

//判断是否应该将当前菜单项显示单个菜单项
const shouldShowSingleItem = computed(() => {
//条件1  存在onlyOneChild (有唯一的子项或者没有子项)
//条件2 唯一的子项没有子菜单 或者标记了noshowingChildren
//条件3 父项没有设置显示为折叠菜单
  return onlyOnechild.value && (!onlyOnechild.value.children || onlyOnechild.value.noshowingChildren)
      && (!props.item.alwaysShow)

})

//计算单个菜单项 点击后应该跳转的完整路径
const singletItemPath = computed(() => {
  return resolvePath(onlyOnechild.value.path)
})
//解析并拼接路由路径 处理特殊情况
function resolvePath(routePath) {
  //拼接基础路径和相对路径
  const fullPath = props.basePath + '/' + routePath;
  //如果路径为空 直接返回
  if(!fullPath) return fullPath;
  //处理特殊情况
  return fullPath.replace('//','/')
      .replace(/\/$/, '')

}

</script>


<style scoped>

</style>