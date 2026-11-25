<template>
  <div class="front-layout">
    <!-- 顶部导航 -->
    <div class="header-container">
      <div class="header">
        <div class="header-left">
          <img src="@/assets/logo/logo.jpg" alt="系统标识" />
          <div class="titles" @click="router.push('/userPage/home')">前台用户端</div>
        </div>
        <div class="header-center">
          <div class="front-header-nav">
            <el-menu :default-active="route.path" mode="horizontal" :router="true" :ellipsis="false">
              <el-menu-item index="/userPage/home">
                <el-icon>
                  <HomeFilled/>
                </el-icon>
                首页
              </el-menu-item>
              <el-menu-item index="/userPage/self">
                <el-icon>
                  <UserFilled/>
                </el-icon>
                个人中心
              </el-menu-item>
            </el-menu>
          </div>
        </div>
        <div class="header-right">
          <div v-if="!userStore.name" class="auth-buttons">
            <el-button type="primary" @click="router.push('/login')">登录</el-button>
            <el-button plain @click="router.push('/register')">注册</el-button>
          </div>
          <div v-else class="user-info">
            <el-dropdown>
              <div class="header-dropdown">
                <img :src="userStore.avatar" alt="用户头像" />
                <div class="user-name">
                  <span>{{ userStore.name }}</span>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <el-button link class="logout-button" @click="logout">
                      退出
                    </el-button>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <main class="front-content">
      <router-view/>
    </main>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import useUserStore from '@/stores/modules/userStore.js';
import { ElMessageBox } from 'element-plus';
import { HomeFilled, UserFilled } from '@element-plus/icons-vue';
// 缺失组件导入，必须补充，否则el-dropdown系列组件无法渲染
import { ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const logout = () => {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => {

  })
}
</script>

<style scoped>
.front-layout {
  min-height: 100vh;
  background: var(--bg-page);
}

.header-container {
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid var(--border-default);
  background: var(--bg-surface);
  box-shadow: 0 1px 3px rgba(23, 32, 51, 0.04);
}

.header {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1280px;
  height: 64px;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
  gap: 12px;
  min-width: 0;
}

.header-left img,
.header-dropdown img {
  width: 36px;
  height: 36px;
  border: 1px solid var(--border-default);
  border-radius: 8px;
  object-fit: cover;
}

.titles {
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 650;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.18s ease;
}

.titles:hover {
  color: var(--brand-600);
}

.header-center {
  display: flex;
  justify-content: center;
  flex: 1;
  min-width: 0;
  margin-left: 32px;
}

/* 导航样式仅作用于前台导航 */
.front-header-nav {
  width: 240px;
  height: 64px;
}

.front-header-nav :deep(.el-menu) {
  width: 100%;
  height: 64px;
  border-bottom: 0;
  background: transparent;
}

.front-header-nav :deep(.el-menu-item) {
  height: 64px;
  margin: 0;
  padding: 0 16px;
  border-bottom: 2px solid transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  line-height: 64px;
  transition: color 0.18s ease, background-color 0.18s ease, border-color 0.18s ease;
}

.front-header-nav :deep(.el-menu-item .el-icon) {
  width: 1em;
  height: 1em;
  margin-right: 6px;
  color: inherit;
  vertical-align: middle;
}

.front-header-nav :deep(.el-menu-item:not(.is-disabled):hover),
.front-header-nav :deep(.el-menu-item:focus) {
  color: var(--brand-600);
  background: var(--brand-50);
}

.front-header-nav :deep(.el-menu--horizontal > .el-menu-item.is-active) {
  border-bottom-color: var(--brand-500);
  color: var(--brand-600);
  background: var(--brand-50);
}

.header-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex: 0 0 auto;
  min-width: 120px;
  margin-left: 24px;
}

.auth-buttons {
  display: flex;
  gap: 8px;
}

.auth-buttons :deep(.el-button) {
  height: 34px;
  margin: 0;
  padding: 0 16px;
  border-radius: var(--radius-control);
  font-size: 13px;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
}

.header-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-primary);
  cursor: pointer;
}

.header-dropdown img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  color: var(--text-primary);
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-button {
  height: 32px;
  margin: 0;
  padding: 0 14px;
  border-radius: var(--radius-control);
  color: var(--text-secondary);
  font-size: 13px;
  text-decoration: none;
}

.logout-button:hover {
  color: var(--brand-600);
}

.front-content {
  min-height: calc(100vh - 64px);
  background: var(--bg-page);
}

@media (max-width: 960px) {
  .header {
    padding: 0 18px;
  }

  .header-center {
    margin-left: 16px;
  }

  .front-header-nav :deep(.el-menu-item) {
    padding: 0 12px;
  }

  .header-right {
    margin-left: 16px;
  }

  .user-name {
    max-width: 90px;
  }
}

@media (max-width: 768px) {
  .header {
    flex-wrap: wrap;
    height: auto;
    min-height: 64px;
    padding: 0 16px;
  }

  .header-left,
  .header-right {
    height: 64px;
  }

  .header-left img {
    width: 32px;
    height: 32px;
  }

  .titles {
    font-size: 16px;
  }

  .header-center {
    order: 3;
    justify-content: flex-start;
    flex-basis: 100%;
    height: 48px;
    margin-left: 0;
    overflow-x: auto;
    border-top: 1px solid var(--border-default);
  }

  .front-header-nav,
  .front-header-nav :deep(.el-menu) {
    width: 100%;
    min-width: max-content;
    height: 48px;
  }

  .front-header-nav :deep(.el-menu-item) {
    height: 48px;
    padding: 0 14px;
    line-height: 48px;
  }

  .header-right {
    margin-left: auto;
  }

  .front-content {
    min-height: calc(100vh - 112px);
  }
}

@media (max-width: 480px) {
  .header {
    padding: 0 14px;
  }

  .header-left {
    gap: 9px;
  }

  .auth-buttons {
    gap: 6px;
  }

  .auth-buttons :deep(.el-button) {
    padding: 0 12px;
  }

  .user-name {
    max-width: 72px;
  }
}
</style>