<template>
  <div class="management-page">
    <header class="page-heading">
      <div class="heading-main">
        <span class="heading-icon"><el-icon><Key /></el-icon></span>
        <div>
          <h1>权限日志</h1>
          <p>追踪角色菜单授权变更与操作链路</p>
        </div>
      </div>
      <div class="heading-stat">
        <span class="live-dot"></span>
        <span>变更记录</span>
        <strong>{{ total }}</strong>
        <span>条</span>
      </div>
    </header>

    <!-- 查询区：权限日志没有 module / operType / status，只留操作人 / 角色 / 时间 -->
    <section class="query-panel">
      <el-form :model="query" ref="queryRef" inline class="query-form">
        <el-form-item label="操作人" prop="operUserName" class="field-md">
          <el-input v-model="query.operUserName" placeholder="请输入操作人" clearable />
        </el-form-item>

        <el-form-item label="目标角色" prop="targetRoleName" class="field-md">
          <el-input v-model="query.targetRoleName" placeholder="请输入角色名" clearable />
        </el-form-item>

        <el-form-item label="操作时间" class="field-date">
          <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <div class="query-actions">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </div>
      </el-form>
    </section>

    <section class="table-panel">
      <div class="table-toolbar">
        <div class="table-title">
          <h2>授权变更记录</h2>
          <span class="record-count">共 {{ total }} 条记录</span>
        </div>
      </div>

      <el-table :data="logList" class="data-table" border v-loading="loading">
        <el-table-column prop="logId" label="日志编号" width="90" align="center" />
        <el-table-column prop="operUserName" label="操作人" width="120" align="center" />
        <el-table-column prop="targetRoleName" label="目标角色" width="120" align="center" />

        <!-- 变更前 -->
        <el-table-column label="变更前" align="center" min-width="150">
          <template #default="scope">
            <el-tag v-for="id in parseMenus(scope.row.oldMenus)" :key="'old' + id"
                    type="info" size="small" class="permission-tag">
              {{ menuLabel(id) }}
            </el-tag>
            <span v-if="parseMenus(scope.row.oldMenus).length === 0" class="cell-muted">无</span>
          </template>
        </el-table-column>

        <!-- 变更后 -->
        <el-table-column label="变更后" align="center" min-width="150">
          <template #default="scope">
            <el-tag v-for="id in parseMenus(scope.row.newMenus)" :key="'new' + id"
                    size="small" class="permission-tag">
              {{ menuLabel(id) }}
            </el-tag>
            <span v-if="parseMenus(scope.row.newMenus).length === 0" class="cell-muted">无</span>
          </template>
        </el-table-column>

        <!-- 新增：绿色 -->
        <el-table-column label="新增" align="center" min-width="130">
          <template #default="scope">
            <el-tag v-for="id in parseMenus(scope.row.addMenus)" :key="'add' + id"
                    type="success" size="small" class="permission-tag">
              {{ menuLabel(id) }}
            </el-tag>
            <span v-if="parseMenus(scope.row.addMenus).length === 0" class="cell-muted">—</span>
          </template>
        </el-table-column>

        <!-- 移除：红色 -->
        <el-table-column label="移除" align="center" min-width="130">
          <template #default="scope">
            <el-tag v-for="id in parseMenus(scope.row.removeMenus)" :key="'rm' + id"
                    type="danger" size="small" class="permission-tag">
              {{ menuLabel(id) }}
            </el-tag>
            <span v-if="parseMenus(scope.row.removeMenus).length === 0" class="cell-muted">—</span>
          </template>
        </el-table-column>

        <el-table-column prop="operTime" label="操作时间" width="170" align="center" />
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="View" class="row-actions" @click="handleDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <span class="empty-icon"><el-icon><Search /></el-icon></span>
            <p>没有找到匹配的权限日志</p>
            <span>请调整筛选条件后再试</span>
          </div>
        </template>
      </el-table>

      <div class="table-footer">
        <div class="footer-summary">
          <span>第 {{ query.pageNum }} 页</span>
          <span class="footer-dot">·</span>
          <span>每页 {{ query.pageSize }} 条</span>
        </div>
        <pagination class="log-pagination"
                    :total="total"
                    v-model:page="query.pageNum"
                    v-model:limit="query.pageSize"
                    @pagination="getList" />
      </div>
    </section>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailOpen" class="detail-drawer" size="520px">
      <template #header>
        <div class="drawer-heading">
          <span class="drawer-heading-icon"><el-icon><Key /></el-icon></span>
          <div>
            <h3>权限变更详情</h3>
            <p>角色菜单授权差异与操作链路</p>
          </div>
        </div>
      </template>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="日志编号">{{ detail.logId }}</el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ detail.operUserName }}（ID: {{ detail.operUserId }}）
        </el-descriptions-item>
        <el-descriptions-item label="目标角色">
          {{ detail.targetRoleName }}（ID: {{ detail.targetRoleId }}）
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.operTime }}</el-descriptions-item>
        <el-descriptions-item label="traceId">
          <code class="trace-id">{{ detail.traceId }}</code>
        </el-descriptions-item>
      </el-descriptions>

      <div class="detail-block">
        <div class="detail-title">变更前（{{ parseMenus(detail.oldMenus).length }} 项）</div>
        <pre class="detail-pre">{{ prettyJson(detail.oldMenus) }}</pre>
      </div>

      <div class="detail-block">
        <div class="detail-title">变更后（{{ parseMenus(detail.newMenus).length }} 项）</div>
        <pre class="detail-pre">{{ prettyJson(detail.newMenus) }}</pre>
      </div>

      <div class="detail-block">
        <div class="detail-title">新增（{{ parseMenus(detail.addMenus).length }} 项）</div>
        <pre class="detail-pre">{{ prettyJson(detail.addMenus) }}</pre>
      </div>

      <div class="detail-block">
        <div class="detail-title">移除（{{ parseMenus(detail.removeMenus).length }} 项）</div>
        <pre class="detail-pre">{{ prettyJson(detail.removeMenus) }}</pre>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import Pagination from "@/components/Pagination/index.vue";
import { onMounted, ref } from "vue";
import { selectPermissionLogList } from "~/API/monitor/permlog.js";
import { selectRoleMenusTree } from "~/API/system/menu.js";

const queryRef = ref()
const loading = ref(false)
const logList = ref([])
const total = ref(0)
const detailOpen = ref(false)
const detail = ref({})
const dateRange = ref([])

const query = ref({
  pageNum: 1,
  pageSize: 10,
  operUserName: null,
  targetRoleName: null
})

// id -> 菜单名 映射表，用于把 [19,20] 显示成"操作日志 / 权限日志"
const menuNameMap = ref({})

/**
 * 把后端存的 JSON 字符串数组拆成真数组。
 * 后端存的是 "[1,2,3]" 这种字符串；解析失败（null / 空串 / 脏数据）返回空数组，
 * 避免模板里 v-for 报错。
 */
const parseMenus = (str) => {
  if (!str) return []
  try {
    const arr = JSON.parse(str)
    if (Array.isArray(arr)) return arr
    return [arr]
  } catch (e) {
    // 兼容 "1,2,3" 这种没有方括号的脏数据
    return String(str).split(',').map(s => s.trim()).filter(Boolean)
  }
}

/**
 * 菜单 ID 转名称；查不到就退回显示 ID，保证不空白。
 */
const menuLabel = (id) => {
  return menuNameMap.value[id] || ('ID:' + id)
}

/**
 * 拉一次菜单树，递归展平成 id -> name 的 map。
 * 只调一次（onMounted），不随翻页重复请求。
 */
const loadMenuNameMap = () => {
  selectRoleMenusTree().then(res => {
    const map = {}
    const walk = (nodes) => {
      if (!nodes || !nodes.length) return
      nodes.forEach(n => {
        const id = n.id ?? n.menuId
        const label = n.label ?? n.menuName
        if (id != null && label) map[id] = label
        walk(n.children)
      })
    }
    walk(res.data)
    menuNameMap.value = map
  }).catch(() => {
    // 拿不到菜单树不阻塞主流程，tag 会退化成显示 ID
  })
}

const getList = () => {
  loading.value = true
  const params = { ...query.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }

  selectPermissionLogList(params).then(res => {
    logList.value = res.data.rows
    total.value = res.data.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

const handleQuery = () => {
  query.value.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryRef.value.resetFields()
  dateRange.value = []
  query.value.pageNum = 1
  getList()
}

const handleDetail = (row) => {
  detail.value = row
  detailOpen.value = true
}

const prettyJson = (str) => {
  if (!str) return '—'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch (e) {
    return str
  }
}

onMounted(() => {
  getList()
  loadMenuNameMap()
})
</script>

<style scoped>
.permission-tag {
  margin: 2px;
}
</style>
