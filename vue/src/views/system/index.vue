<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-area">
      <div class="welcome-text">
        <h2>欢迎回来，{{ userStore.name || '管理员' }}</h2>
        <p class="sub-text">系统运行正常，可在这里查看数据概览与常用管理入口。</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" plain>
          <el-icon><Refresh /></el-icon> 刷新数据
        </el-button>
        <el-button plain>
          <el-icon><Setting /></el-icon> 系统设置
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="(item, idx) in statsData" :key="idx">
        <el-card class="stat-card" shadow="never">
          <div class="stat-content">
            <div class="stat-left">
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-sub">{{ item.sub }}</div>
            </div>
            <div class="stat-icon">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 权限与快捷操作 -->
    <el-row :gutter="20" class="mid-row">
      <el-col :xs="24" :md="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Setting /></el-icon>
                我的权限
              </span>
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

      <el-col :xs="24" :md="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <span class="card-title">
              <el-icon><MenuIcon /></el-icon>
              快捷操作
            </span>
          </template>
          <div class="quick-grid">
            <button
                v-for="action in quickActions"
                :key="action.label"
                type="button"
                class="quick-item"
                @click="action.handler"
            >
              <span class="quick-icon">
                <el-icon :size="22"><component :is="action.icon" /></el-icon>
              </span>
              <span class="quick-label">{{ action.label }}</span>
            </button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统信息 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <span class="card-title">
              <el-icon><Monitor /></el-icon>
              系统信息
            </span>
          </template>
          <el-descriptions
              class="system-descriptions"
              :column="4"
              border
              size="small"
          >
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
  min-height: 100%;
  box-sizing: border-box;
  padding: 20px;
  background: var(--bg-page);
}

/* 欢迎区域 */
.welcome-area {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
  padding: 18px 22px;
  border: 1px solid var(--border-default);
  border-left: 3px solid var(--brand-500);
  border-radius: var(--radius-card);
  background: var(--bg-surface);
  box-shadow: var(--shadow-card);
}

.welcome-text {
  min-width: 0;
}

.welcome-text h2 {
  margin: 0;
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 650;
  line-height: 1.35;
}

.sub-text {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.welcome-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.welcome-actions :deep(.el-button) {
  height: 34px;
  margin: 0;
  padding: 0 14px;
  border-radius: var(--radius-control);
  font-size: 13px;
  font-weight: 500;
}

.welcome-actions :deep(.el-button .el-icon) {
  margin-right: 6px;
}

/* 统计卡片 */
.stats-row,
.mid-row,
.info-row {
  row-gap: 16px;
}

.stats-row,
.mid-row {
  margin-bottom: 16px;
}

.stats-row :deep(.el-col),
.mid-row :deep(.el-col) {
  display: flex;
}

.stat-card {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  border: 1px solid var(--border-default);
  border-radius: var(--radius-card);
  background: var(--bg-surface);
  box-shadow: var(--shadow-card);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.stat-card:hover {
  border-color: var(--border-strong);
  box-shadow: var(--shadow-card);
}

.stat-card :deep(.el-card__body) {
  height: 100%;
  box-sizing: border-box;
  padding: 18px;
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 72px;
}

.stat-left {
  min-width: 0;
}

.stat-label {
  margin-bottom: 6px;
  color: var(--text-secondary);
  font-size: 13px;
}

.stat-value {
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 650;
  line-height: 1.15;
}

.stat-sub {
  margin-top: 6px;
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.4;
}

.stat-icon,
.quick-icon {
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

/* 权限与快捷操作面板 */
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

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

.perm-wrap {
  display: flex;
  align-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 72px;
}

.perm-wrap :deep(.el-empty) {
  width: 100%;
  padding: 4px 0;
}

.perm-tag {
  max-width: 100%;
  border-radius: 4px;
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: 11px;
  line-height: 20px;
  overflow-wrap: anywhere;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.quick-item {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  min-height: 94px;
  padding: 14px 10px;
  border: 1px solid var(--border-default);
  border-radius: var(--radius-card);
  color: var(--text-primary);
  background: var(--bg-subtle);
  font: inherit;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.18s ease, background-color 0.18s ease;
}

.quick-item:hover {
  border-color: var(--border-strong);
  background: var(--brand-50);
}

.quick-icon {
  width: 38px;
  height: 38px;
}

.quick-label {
  font-size: 13px;
  font-weight: 500;
}

/* 系统信息 */
.system-descriptions {
  --el-descriptions-table-border: 1px solid var(--border-default);
  --el-descriptions-item-bordered-label-background: var(--bg-subtle);
}

.system-descriptions :deep(.el-descriptions__label) {
  width: 96px;
  color: var(--text-secondary);
  font-weight: 500;
}

.system-descriptions :deep(.el-descriptions__content) {
  color: var(--text-primary);
  overflow-wrap: anywhere;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 14px;
  }

  .welcome-area {
    align-items: stretch;
    flex-direction: column;
    padding: 16px 18px;
  }

  .welcome-text h2 {
    font-size: 18px;
  }

  .stat-card :deep(.el-card__body),
  .panel-card :deep(.el-card__body) {
    padding: 16px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-icon {
    width: 42px;
    height: 42px;
  }
}

@media (max-width: 480px) {
  .welcome-actions {
    width: 100%;
  }

  .welcome-actions :deep(.el-button) {
    flex: 1;
  }

  .stat-content {
    gap: 12px;
  }

  .quick-grid {
    gap: 8px;
  }

  .quick-item {
    min-height: 86px;
  }
}
</style>