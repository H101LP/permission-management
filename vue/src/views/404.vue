<template>
  <div class="page-404">
    <main class="content">
      <h1 class="code">404</h1>
      <h2 class="title">页面不存在</h2>
      <p class="desc">抱歉，您访问的页面不存在或已被移除</p>
      <router-link to="/" class="home-link">返回首页</router-link>
    </main>
  </div>
</template>

<style scoped>
.page-404 {
  display: grid;
  place-items: center;
  min-height: 100vh;
  min-height: 100svh;
  box-sizing: border-box;
  padding: 24px;
  background: var(--bg-page);
}

.content {
  width: 100%;
  max-width: 480px;
  box-sizing: border-box;
  padding: 48px 32px;
  border: 1px solid var(--border-default);
  border-radius: var(--radius-card);
  background: var(--bg-surface);
  box-shadow: var(--shadow-card);
  text-align: center;
}

.code {
  margin: 0;
  color: var(--brand-500);
  font-size: clamp(88px, 14vw, 120px);
  font-weight: 700;
  letter-spacing: -0.06em;
  line-height: 1;
}

.title {
  margin: 20px 0 8px;
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 650;
  line-height: 1.35;
}

.desc {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

.home-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  margin-top: 24px;
  padding: 0 20px;
  border: 1px solid var(--brand-500);
  border-radius: var(--radius-control);
  color: #ffffff;
  background: var(--brand-500);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: border-color 0.18s ease, background-color 0.18s ease;
}

.home-link:hover {
  border-color: var(--brand-600);
  background: var(--brand-600);
}

@media (max-width: 480px) {
  .page-404 {
    padding: 16px;
  }

  .content {
    padding: 36px 20px;
  }

  .title {
    font-size: 21px;
  }
}
</style>