<template>
  <div class="dashboard">
    <!-- ======================== 第一行：欢迎区域 ======================== -->
    <div class="welcome-area">
      <div class="welcome-text">
        <h2>👋 欢迎回来，{{ userStore.name || '管理员' }}</h2>
        <p class="sub-text">今天是个好日子，系统运行正常，一切都在掌控之中。 这是管理员界面</p>
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
              <div class="stat-sub" style="color: #909399">{{ item.sub }}</div>
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
              <span class="card-title">🔐 我的权限</span>
              <el-tag size="small" type="info">共 {{ permissionList.length }} 项</el-tag>
            </div>
          </template>
          <div class="perm-wrap">
            <el-tag
                v-for="p in permissionList"
                :key="p"
                size="small"
                :type="p === '*:*:*' ? 'danger' : 'primary'"
                effect="plain"
                class="perm-tag"
            >
              {{ p }}
            </el-tag>
            <el-empty
                v-if="permissionList.length === 0"
                description="尚未分配任何权限"
                :image-size="60"
            />
          </div>
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
            <el-descriptions-item label="当前账号">{{ sysInfo.user }}</el-descriptions-item>
            <el-descriptions-item label="环境">{{ sysInfo.env }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/stores/modules/userStore.js'
import { ElMessage } from 'element-plus'
import { selectUserList } from '~/API/system/user.js'
import { selectRoleList } from '~/API/system/role.js'
import { selectMenuList } from '~/API/system/menu.js'
import { selectOperationLogList } from '~/API/monitor/operlog.js'
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

// -------------------- 统计数据（全部来自后端接口，没有一处写死） --------------------
// 注意：第一张卡"可见用户"取的是 selectUserList 的 total。
// 这个接口走 @DataScope，普通用户只会拿到本部门的条数，admin 拿到全表条数。
// 所以同一张卡片、同一个页面，两个角色看到的数字天然不同 —— 这就是数据权限的可视化。
const statsData = ref([
  {
    label: '可见用户',
    value: '—',
    sub: '当前数据范围内',
    icon: 'User',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    label: '角色数量',
    value: '—',
    sub: '权限配置项',
    icon: 'OfficeBuilding',
    bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    label: '可见菜单',
    value: '—',
    sub: '按角色授权范围',
    icon: 'Menu',
    bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    label: '操作日志',
    value: '—',
    sub: '审计记录条数',
    icon: 'Document',
    bg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

// -------------------- 我的权限（来自 /getInfo，与后端 @PreAuthorizePerm 校验的是同一份数据） --------------------
const permissionList = computed(() => {
  const perms = userStore.permissions || []
  return [...perms].sort()
})

// -------------------- 快捷操作（按权限过滤，没权限的入口直接不显示） --------------------
// 不过滤的话，普通用户点"角色管理"会因为菜单没授权而落到 404
const allQuickActions = [
  { label: '用户管理', icon: 'User', bg: '#667eea', path: '/system/user', perm: 'system:user:add' },
  { label: '角色管理', icon: 'OfficeBuilding', bg: '#f093fb', path: '/system/role', perm: 'system:role:add' },
  { label: '菜单管理', icon: 'Menu', bg: '#4facfe', path: '/system/menu', perm: 'system:menu:add' },
  { label: '个人中心', icon: 'Setting', bg: '#43e97b', path: '/profile/user', perm: null }
]
const quickActions = computed(() => {
  const perms = userStore.permissions || []
  const isAdmin = perms.includes('*:*:*')
  return allQuickActions
      .filter(a => !a.perm || isAdmin || perms.includes(a.perm))
      .map(a => ({ ...a, handler: () => router.push(a.path) }))
})

// -------------------- 系统信息 --------------------
const sysInfo = computed(() => ({
  name: 'RBAC 权限管理系统',
  version: 'v2.0.1',
  user: userStore.name || '—',
  env: import.meta.env.MODE === 'production' ? '生产环境' : '开发环境'
}))

// -------------------- 统计数据加载（真实接口，四个数字全部来自后端） --------------------
const loadStats = async () => {
  // 用户数：走 @DataScope，普通用户拿到的就是本部门条数
  try {
    const res = await selectUserList({ pageNum: 1, pageSize: 1 })
    statsData.value[0].value = res.data?.total ?? '—'
  } catch (e) {
    statsData.value[0].value = '—'
  }
  // 角色数
  try {
    const res = await selectRoleList({ pageNum: 1, pageSize: 1 })
    statsData.value[1].value = res.data?.total ?? '—'
  } catch (e) {
    statsData.value[1].value = '—'
  }
  // 菜单数：该接口返回数组，取长度
  try {
    const res = await selectMenuList({})
    statsData.value[2].value = Array.isArray(res.data) ? res.data.length : '—'
  } catch (e) {
    statsData.value[2].value = '—'
  }
  // 操作日志条数
  try {
    const res = await selectOperationLogList({ pageNum: 1, pageSize: 1 })
    statsData.value[3].value = res.data?.total ?? '—'
  } catch (e) {
    statsData.value[3].value = '—'
  }
}

// -------------------- 生命周期 --------------------
onMounted(() => {
  loadStats()
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

/* 权限点标签区 */
.perm-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 60px;
  align-items: flex-start;
}

.perm-tag {
  font-family: ui-monospace, Menlo, Consolas, monospace;
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