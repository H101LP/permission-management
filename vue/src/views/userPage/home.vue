<template>
  <div class="user-home">
    <!-- ===================== 欢迎横幅 ===================== -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>{{ greeting }}，{{ userStore.name || '用户' }} 👋</h1>
          <p class="welcome-sub">欢迎来到 {{ siteTitle }}，今日好心情！</p>
        </div>
        <div class="welcome-extra">
          <el-tag type="success" size="large" effect="light">
            角色：{{ userRole }}
          </el-tag>
          <el-tag type="info" size="large" effect="light">
            {{ currentDate }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- ===================== 数据总览卡片 ===================== -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="(stat, idx) in stats" :key="idx">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="28"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stat.value }}</span>
              <span class="stat-label">{{ stat.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===================== 快捷入口 ===================== -->
    <el-card class="quick-card" shadow="never">
      <template #header>
        <span class="card-title">🚀 快捷入口</span>
      </template>
      <div class="quick-actions">
        <el-button v-for="act in quickActions" :key="act.label"
                   :type="act.type" plain size="large"
                   icon="el-icon-plus" @click="act.handler">
          {{ act.label }}
        </el-button>
      </div>
    </el-card>

    <!-- ===================== 动态资讯 & 系统通知 ===================== -->
    <el-row :gutter="20" class="info-row">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-title">📰 最新动态</span>
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
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-title">📌 系统公告</span>
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
    handler: () => router.push('/user/profile')
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
  padding: 20px;
}

/* ---------- 欢迎横幅 ---------- */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px 40px;
  margin-bottom: 24px;
  color: #fff;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.welcome-text h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.welcome-sub {
  font-size: 16px;
  opacity: 0.85;
  margin: 0;
}

.welcome-extra {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.welcome-extra .el-tag {
  font-size: 15px;
  padding: 8px 18px;
}

/* ---------- 统计卡片 ---------- */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #1f2f3d;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* ---------- 快捷入口 ---------- */
.quick-card {
  border-radius: 12px;
  margin-bottom: 24px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.quick-actions .el-button {
  min-width: 120px;
  justify-content: center;
}

/* ---------- 动态资讯 & 公告 ---------- */
.info-row {
  margin-top: 8px;
}

.info-card {
  border-radius: 12px;
  height: 100%;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
}

.el-timeline {
  padding-left: 0;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #1f2f3d;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409EFF;
  flex-shrink: 0;
}

.notice-text {
  color: #606266;
}

/* ---------- 响应式调整 ---------- */
@media (max-width: 768px) {
  .welcome-banner {
    padding: 24px 20px;
  }
  .welcome-text h1 {
    font-size: 22px;
  }
  .welcome-extra {
    margin-top: 12px;
  }
  .stat-value {
    font-size: 20px;
  }
}
</style>