<template>
  <div class="app-container">

    <!-- 查询区：权限日志没有 module / operType / status，只留操作人 / 角色 / 时间 -->
    <el-form :model="query" ref="queryRef" label-width="80px" inline>
      <el-form-item label="操作人" prop="operUserName">
        <el-input v-model="query.operUserName" placeholder="请输入操作人" clearable style="width: 150px"/>
      </el-form-item>

      <el-form-item label="目标角色" prop="targetRoleName">
        <el-input v-model="query.targetRoleName" placeholder="请输入角色名" clearable style="width: 150px"/>
      </el-form-item>

      <el-form-item label="操作时间">
        <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <el-table :data="logList" style="width: 100%" border v-loading="loading">
      <el-table-column prop="logId" label="日志编号" width="90" align="center"/>
      <el-table-column prop="operUserName" label="操作人" width="120" align="center"/>
      <el-table-column prop="targetRoleName" label="目标角色" width="120" align="center"/>

      <!-- 变更前 -->
      <el-table-column label="变更前" align="center" min-width="150">
        <template #default="scope">
          <el-tag v-for="id in parseMenus(scope.row.oldMenus)" :key="'old' + id"
                  type="info" size="small" style="margin: 2px">
            {{ menuLabel(id) }}
          </el-tag>
          <span v-if="parseMenus(scope.row.oldMenus).length === 0" class="empty-text">无</span>
        </template>
      </el-table-column>

      <!-- 变更后 -->
      <el-table-column label="变更后" align="center" min-width="150">
        <template #default="scope">
          <el-tag v-for="id in parseMenus(scope.row.newMenus)" :key="'new' + id"
                  size="small" style="margin: 2px">
            {{ menuLabel(id) }}
          </el-tag>
          <span v-if="parseMenus(scope.row.newMenus).length === 0" class="empty-text">无</span>
        </template>
      </el-table-column>

      <!-- 新增：绿色 -->
      <el-table-column label="新增" align="center" min-width="130">
        <template #default="scope">
          <el-tag v-for="id in parseMenus(scope.row.addMenus)" :key="'add' + id"
                  type="success" size="small" style="margin: 2px">
            {{ menuLabel(id) }}
          </el-tag>
          <span v-if="parseMenus(scope.row.addMenus).length === 0" class="empty-text">—</span>
        </template>
      </el-table-column>

      <!-- 移除：红色 -->
      <el-table-column label="移除" align="center" min-width="130">
        <template #default="scope">
          <el-tag v-for="id in parseMenus(scope.row.removeMenus)" :key="'rm' + id"
                  type="danger" size="small" style="margin: 2px">
            {{ menuLabel(id) }}
          </el-tag>
          <span v-if="parseMenus(scope.row.removeMenus).length === 0" class="empty-text">—</span>
        </template>
      </el-table-column>

      <el-table-column prop="operTime" label="操作时间" width="170" align="center"/>
      <el-table-column label="操作" width="90" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination :total="total"
                v-model:page="query.pageNum"
                v-model:limit="query.pageSize"
                @pagination="getList"/>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailOpen" title="权限变更详情" size="45%">
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
          <span style="font-family: monospace">{{ detail.traceId }}</span>
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
.empty-text {
  color: #c0c4cc;
  font-size: 12px;
}
.detail-block {
  margin-top: 16px;
}
.detail-title {
  font-weight: 600;
  margin-bottom: 6px;
  color: #303133;
}
.detail-pre {
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px 12px;
  max-height: 220px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.6;
  margin: 0;
}
</style>
