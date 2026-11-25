<template>
  <div class="user-home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>{{ greeting }}，{{ userStore.name || '用户' }}</h1>
          <p class="welcome-sub">欢迎使用 {{ siteTitle }}，这里汇总了您的常用入口与系统动态。</p>
        </div>
        <div class="welcome-extra">
          <el-tag class="welcome-tag" type="primary" effect="light">
            角色：{{ userRole }}
          </el-tag>
          <el-tag class="welcome-tag" type="info" effect="light">
            {{ currentDate }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 数据总览卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="(stat, idx) in stats" :key="idx">
        <el-card class="stat-card" shadow="never">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stat.value }}</span>
              <span class="stat-label">{{ stat.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card class="panel-card quick-card" shadow="never">
      <template #header>
        <span class="card-title">
          <el-icon><Setting /></el-icon>
          快捷入口
        </span>
      </template>
      <div class="quick-actions">
        <el-button v-for="act in quickActions" :key="act.label"
                   class="quick-action"
                   :type="act.type" plain
                   @click="act.handler">
          {{ act.label }}
        </el-button>
      </div>
    </el-card>

    <!-- 动态资讯与系统通知 -->
    <el-row :gutter="20" class="info-row">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="panel-card info-card">
          <template #header>
            <span class="card-title">
              <el-icon><Document /></el-icon>
              最新动态
            </span>
          </template>
          <el-timeline>
            <el-timeline-item
                v-for="(item, idx) in timeline"
                :key="idx"
                :timestamp="item.time"
                :type="item.type"
                :icon="item.icon"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="panel-card info-card">
          <template #header>
            <span class="card-title">
              <el-icon><Bell /></el-icon>
              系统公告
            </span>
          </template>
          <div class="notice-list">
            <div v-for="(notice, idx) in notices" :key="idx" class="notice-item">
              <span class="notice-dot"></span>
              <span class="notice-text">{{ notice }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/stores/modules/userStore.js'
import { ElMessage } from 'element-plus'
import {
  Document,
  User,
  Clock,
  Check,
  Setting,
  Bell,
  DataLine
} from '@element-plus/icons-vue'

// ---------- 状态 ----------
const router = useRouter()
const userStore = useUserStore()

// ---------- 常量 ----------
const siteTitle = import.meta.env.VITE_APP_TITLE || '后台管理系统'

// ---------- 计算属性 ----------
// 当前时间问候
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 用户角色
const userRole = computed(() => {
  // 从 userStore 或 getInfo 结果中取角色，这里假设有 roleName
  return userStore.roleName || '普通用户'
})

// ---------- 数据 ----------
// 统计卡片
const stats = ref([
  { label: '总访问量', value: '12,384', icon: 'DataLine', color: '#409EFF' },
  { label: '活跃用户', value: '2,315', icon: 'User', color: '#67C23A' },
  { label: '今日新增', value: '128', icon: 'Document', color: '#E6A23C' },
  { label: '待处理', value: '5', icon: 'Clock', color: '#F56C6C' }
])

// 快捷入口
const quickActions = ref([
  {
    label: '个人中心',
    type: 'primary',
    handler: () => router.push('/userPage/self')
  },
  {
    label: '修改密码',
    type: 'warning',
    handler: () => {
      // 可以触发修改密码弹窗，或跳转
      ElMessage.info('跳转到修改密码页面')
    }
  },
  {
    label: '查看通知',
    type: 'success',
    handler: () => ElMessage.info('暂无新通知')
  },
  {
    label: '系统设置',
    type: 'info',
    handler: () => ElMessage.info('系统设置开发中')
  }
])

// 时间线动态
const timeline = ref([
  {
    content: '你登录了系统',
    time: '2025-08-21 10:30',
    type: 'primary',
    icon: 'el-icon-message'
  },
  {
    content: '更新了个人信息',
    time: '2025-08-20 15:22',
    type: 'success',
    icon: 'el-icon-success'
  },
  {
    content: '查看了用户管理',
    time: '2025-08-19 09:15',
    type: 'warning',
    icon: 'el-icon-warning'
  }
])

// 公告
const notices = ref([
  '系统将于 2025-08-25 凌晨升级，届时服务暂停',
  '新版用户界面已上线，欢迎体验',
  '请及时更新个人资料，确保信息准确'
])

// ---------- 生命周期 ----------
onMounted(() => {
  // 可以在这里拉取额外的数据，例如通知数量或动态统计
})
</script>

<style scoped>
.user-home {
  min-height: 100%;
  box-sizing: border-box;
  padding: 20px;
  background: var(--bg-page);
}

/* 欢迎横幅 */
.welcome-banner {
  margin-bottom: 16px;
  padding: 20px 24px;
  overflow: hidden;
  border: 1px solid var(--border-default);
  border-left: 4px solid var(--brand-600);
  border-radius: var(--radius-card);
  background: var(--brand-50);
  box-shadow: var(--shadow-card);
}

.welcome-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px 24px;
}

.welcome-text {
  min-width: 0;
}

.welcome-text h1 {
  margin: 0;
  color: var(--text-primary);
  font-size: 22px;
  font-weight: 650;
  line-height: 1.35;
}

.welcome-sub {
  margin: 7px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.welcome-extra {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.welcome-tag {
  height: 28px;
  padding: 0 10px;
  border-color: var(--border-default);
  border-radius: var(--radius-control);
  color: var(--text-secondary);
  background: var(--bg-surface);
  font-size: 12px;
  line-height: 26px;
}

.welcome-tag.el-tag--primary {
  border-color: var(--border-default);
  color: var(--brand-600);
}

/* 统计卡片 */
.stats-row {
  row-gap: 16px;
  margin-bottom: 16px;
}

.stats-row :deep(.el-col),
.info-row :deep(.el-col) {
  display: flex;
}

.stat-card {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  border: 1px solid var(--border-default);
  border-radius: var(--radius-card);
  background: var(--bg-surface);
  box-shadow: var(--shadow-card);
  transition: border-color 0.18s ease;
}

.stat-card:hover {
  border-color: var(--border-strong);
}

.stat-card :deep(.el-card__body) {
  height: 100%;
  box-sizing: border-box;
  padding: 18px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 60px;
}

.stat-icon {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 46px;
  height: 46px;
  border: 1px solid var(--border-default);
  border-radius: var(--radius-card);
  color: var(--brand-500);
  background: var(--brand-50);
}

.stat-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-value {
  color: var(--text-primary);
  font-size: 26px;
  font-weight: 650;
  line-height: 1.15;
}

.stat-label {
  margin-top: 6px;
  color: var(--text-secondary);
  font-size: 13px;
}

/* 快捷入口与信息面板 */
.panel-card {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  border: 1px solid var(--border-default);
  border-radius: var(--radius-card);
  background: var(--bg-surface);
  box-shadow: var(--shadow-card);
}

.panel-card :deep(.el-card__header) {
  padding: 14px 18px;
  border-bottom: 1px solid var(--border-default);
  background: var(--bg-surface);
}

.panel-card :deep(.el-card__body) {
  padding: 18px;
}

.quick-card {
  margin-bottom: 16px;
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
}

.card-title .el-icon {
  color: var(--brand-500);
  font-size: 16px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-actions :deep(.quick-action) {
  --el-button-text-color: var(--brand-600);
  --el-button-bg-color: var(--brand-50);
  --el-button-border-color: var(--border-default);
  --el-button-hover-text-color: var(--brand-600);
  --el-button-hover-bg-color: var(--brand-50);
  --el-button-hover-border-color: var(--brand-500);
  --el-button-active-text-color: var(--brand-600);
  --el-button-active-bg-color: var(--brand-50);
  --el-button-active-border-color: var(--brand-600);
  height: 36px;
  min-width: 120px;
  margin: 0;
  padding: 0 16px;
  border-radius: var(--radius-control);
  font-size: 13px;
  font-weight: 500;
}

.info-row {
  row-gap: 16px;
}

.info-card :deep(.el-timeline) {
  padding-left: 0;
}

.info-card :deep(.el-timeline-item__node) {
  border-color: var(--brand-500);
  background: var(--brand-500);
}

.info-card :deep(.el-timeline-item__content) {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.info-card :deep(.el-timeline-item__timestamp) {
  color: var(--text-tertiary);
  font-size: 11px;
}

.notice-list {
  display: flex;
  flex-direction: column;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 11px 0;
  border-bottom: 1px solid var(--border-default);
}

.notice-item:first-child {
  padding-top: 0;
}

.notice-item:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.notice-dot {
  flex-shrink: 0;
  width: 6px;
  height: 6px;
  margin-top: 7px;
  border-radius: 50%;
  background: var(--brand-500);
}

.notice-text {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .user-home {
    padding: 14px;
  }

  .welcome-banner {
    padding: 18px;
  }

  .welcome-text h1 {
    font-size: 20px;
  }

  .stat-card :deep(.el-card__body),
  .panel-card :deep(.el-card__body) {
    padding: 16px;
  }

  .stat-value {
    font-size: 22px;
  }

  .stat-icon {
    width: 42px;
    height: 42px;
  }
}

@media (max-width: 480px) {
  .welcome-content {
    align-items: flex-start;
    flex-direction: column;
  }

  .quick-action {
    flex: 1;
    min-width: 104px;
  }
}
</style>