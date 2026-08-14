<template>
<router-link :to="to">
<!--用于显示包裹在组件标签内的内容  -->
  <slot></slot>
</router-link>
</template>

<script setup>
//只接收一个to属性 用于指定要跳转的路由路径
defineProps({
  //to属性表示链接的目标地址
  to:{
    type:String,
    required:true
  }
})
</script>
