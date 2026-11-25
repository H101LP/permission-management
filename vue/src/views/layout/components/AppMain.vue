<template>
<div style="padding: 16px">
  <router-view v-slot = "{ Component, route }" >
  <el-card style="min-height: calc(100vh - 85px)">
    <component v-if="!route.meta.link" :is="Component" :key ="router.path" />
  </el-card>
  </router-view>
</div>


</template>
<script setup>

import router from "@/router/index.js";
</script>


<style scoped>
/* ⚠️ 必须用属性选择器锁定「外层那一张」card，不能裸写 :deep(.el-card) */
:deep(.el-card[style*="calc(100vh - 85px)"]) {
  min-height: calc(100vh - 104px) !important;  /* 覆盖内联 calc(100vh - 85px) */
  border: 1px solid var(--border-default);
  border-radius: 12px;
  box-shadow: var(--shadow-card);
}
/* 只清外层 card 的 body padding；页面卡片保留 EP 默认 20px */
:deep(.el-card[style*="calc(100vh - 85px)"] > .el-card__body) {
  padding: 0;
}
</style>