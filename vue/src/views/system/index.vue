<template>
  <div class="dashboard">
    <!-- ======================== 第一行：欢迎区域 ======================== -->
    <div class="welcome-area">
      <div class="welcome-text">
        <h2>👋 欢迎回来，{{ userStore.name || '管理员' }}</h2>
        <p class="sub-text">今天是个好日子，系统运行正常，一切都在掌控之中。</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" plain size="large">
          <el-icon><Refresh /></el-icon> 刷新数据
        </el-button>
        <el-button type="success" plain size="large">
          <el-icon><Setting /></el-icon> 系统设置
        </el-button>
      </div>
    </div>

    <!-- ======================== 第二行：统计卡片 ======================== -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="(item, idx) in statsData" :key="idx">
        <el-card class="stat-card" shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-left">
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-sub" :style="{ color: item.trend > 0 ? '#67C23A' : '#F56C6C' }">
                {{ item.trend > 0 ? '↑' : '↓' }} {{ Math.abs(item.trend) }}% 较昨日
              </div>
            </div>
            <div class="stat-icon" :style="{ background: item.bg }">
              <el-icon :size="32"><component :is="item.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ======================== 第三行：图表概览 + 快捷功能 ======================== -->
    <el-row :gutter="20" class="mid-row">
      <!-- 左侧：通知与动态 -->
      <el-col :xs="24" :md="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">📋 最新动态</span>
              <el-button link type="primary" size="small">查看更多</el-button>
            </div>
          </template>
          <el-timeline style="padding: 0;">
            <el-timeline-item
                v-for="(log, idx) in logs"
                :key="idx"
                :timestamp="log.time"
                :type="log.type"
                placement="top"
            >
              {{ log.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <!-- 右侧：快捷入口 -->
      <el-col :xs="24" :md="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <span class="card-title">⚡ 快捷操作</span>
          </template>
          <div class="quick-grid">
            <div
                v-for="action in quickActions"
                :key="action.label"
                class="quick-item"
                @click="action.handler"
            >
              <div class="quick-icon" :style="{ background: action.bg }">
                <el-icon :size="24"><component :is="action.icon" /></el-icon>
              </div>
              <span class="quick-label">{{ action.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ======================== 第四行：系统信息 ======================== -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <span class="card-title">🖥️ 系统信息</span>
          </template>
          <el-descriptions :column="4" border size="small">
            <el-descriptions-item label="系统名称">{{ sysInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="版本号">{{ sysInfo.version }}</el-descriptions-item>
            <el-descriptions-item label="运行时间">{{ sysInfo.uptime }}</el-descriptions-item>
            <el-descriptions-item label="环境">{{ sysInfo.env }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/stores/modules/userStore.js'
import { ElMessage } from 'element-plus'
import {
  User,
  Setting,
  Monitor,
  Document,
  DataAnalysis,
  TrendCharts,
  Refresh,
  Wallet,
  Menu as MenuIcon,
  List,
  Clock,
  Odometer,
  OfficeBuilding
} from '@element-plus/icons-vue'

// -------------------- 状态 --------------------
const router = useRouter()
const userStore = useUserStore()

// -------------------- 统计数据 --------------------
const statsData = ref([
  {
    label: '用户总数',
    value: '12,384',
    trend: 12,
    icon: 'User',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    label: '角色数量',
    value: '8',
    trend: 0,
    icon: 'OfficeBuilding',
    bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    label: '菜单项数',
    value: '26',
    trend: 5,
    icon: 'Menu',
    bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    label: '在线用户',
    value: '32',
    trend: -8,
    icon: 'Odometer',
    bg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

// -------------------- 动态日志 --------------------
const logs = ref([
  { content: '管理员 admin 登录系统', time: '2025-08-21 10:30:12', type: 'primary' },
  { content: '新增角色：运维工程师', time: '2025-08-21 09:15:40', type: 'success' },
  { content: '用户 zhangsan 注册成功', time: '2025-08-21 08:45:23', type: 'warning' },
  { content: '系统定时任务执行完毕', time: '2025-08-21 08:00:00', type: 'info' }
])

// -------------------- 快捷操作 --------------------
const quickActions = ref([
  {
    label: '用户管理',
    icon: 'User',
    bg: '#667eea',
    handler: () => router.push('/system/user')
  },
  {
    label: '角色管理',
    icon: 'OfficeBuilding',
    bg: '#f093fb',
    handler: () => router.push('/system/role')
  },
  {
    label: '菜单管理',
    icon: 'Menu',
    bg: '#4facfe',
    handler: () => router.push('/system/menu')
  },
  {
    label: '个人中心',
    icon: 'Setting',
    bg: '#43e97b',
    handler: () => router.push('/user/profile')
  }
])

// -------------------- 系统信息 --------------------
const sysInfo = ref({
  name: 'RBAC 权限管理系统',
  version: 'v2.0.1',
  uptime: '12 小时 34 分钟',
  env: import.meta.env.MODE || 'development'
})

// -------------------- 生命周期 --------------------
onMounted(() => {
  console.log('后台首页已加载')
  // 可以在此处请求统计数据，例如 getDashboardStats()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

/* -------------------- 欢迎区域 -------------------- */
.welcome-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  padding: 24px 32px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  flex-wrap: wrap;
  gap: 16px;
}

.welcome-text h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 6px 0;
  color: #1f2f3d;
}

.sub-text {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.welcome-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* -------------------- 统计卡片 -------------------- */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-left {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1f2f3d;
  line-height: 1.2;
}

.stat-sub {
  font-size: 13px;
  margin-top: 6px;
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

/* -------------------- 中间面板 -------------------- */
.mid-row {
  margin-bottom: 24px;
}

.panel-card {
  border-radius: 12px;
  height: 100%;
}

.panel-card :deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

.panel-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2f3d;
}

/* 快捷入口网格 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.15s ease;
}

.quick-item:hover {
  background: #eef0f2;
  transform: scale(1.02);
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 8px;
}

.quick-label {
  font-size: 14px;
  color: #1f2f3d;
}

/* -------------------- 时间线微调 -------------------- */
.el-timeline {
  padding-left: 0;
}

.el-timeline-item :deep(.el-timeline-item__timestamp) {
  font-size: 13px;
  color: #aaa;
}

/* -------------------- 响应式适配 -------------------- */
@media (max-width: 768px) {
  .welcome-area {
    flex-direction: column;
    align-items: stretch;
  }
  .stat-value {
    font-size: 22px;
  }
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>