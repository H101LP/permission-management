<template>
  <div class="page-404">
    <div class="content">
      <h1 class="code">404</h1>
      <h2 class="title">页面走丢啦</h2>
      <p class="desc">抱歉，您访问的页面不存在或已被移除</p>
    </div>
  </div>
</template>

<style scoped>
.page-404 {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}
.content {
  text-align: center;
  padding: 24px;
}
.code {
  font-size: 120px;
  font-weight: 700;
  color: #409eff;
  margin: 0;
  line-height: 1;
}
.title {
  font-size: 28px;
  color: #303133;
  margin: 16px 0 8px;
}
.desc {
  font-size: 15px;
  color: #909399;
  margin-bottom: 32px;
}
.btns {
  display: flex;
  gap: 14px;
  justify-content: center;
}
</style>