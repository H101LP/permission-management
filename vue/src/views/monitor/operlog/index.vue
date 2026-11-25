<template>
  <div class="operlog-page">
    <header class="page-heading">
      <div class="heading-main">
        <div class="heading-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div>
          <h1>操作日志</h1>
          <p>追踪用户操作、接口请求与业务变更</p>
        </div>
      </div>
      <div class="heading-stat">
        <span class="live-dot"></span>
        <span class="heading-stat-label">当前结果</span>
        <strong class="heading-stat-value">{{ total }}</strong>
        <span class="heading-stat-unit">条</span>
      </div>
    </header>

    <section class="query-panel">
      <el-form :model="query" ref="queryRef" inline class="query-form">
        <el-form-item label="模块" prop="module" class="field-module">
          <el-select v-model="query.module" placeholder="全部模块" clearable>
            <el-option label="用户管理" value="用户管理"/>
            <el-option label="角色管理" value="角色管理"/>
            <el-option label="部门管理" value="部门管理"/>
            <el-option label="菜单管理" value="菜单管理"/>
          </el-select>
        </el-form-item>

        <el-form-item label="类型" prop="operType" class="field-type">
          <el-select v-model="query.operType" placeholder="全部类型" clearable>
            <el-option label="新增" value="INSERT"/>
            <el-option label="修改" value="UPDATE"/>
            <el-option label="删除" value="DELETE"/>
            <el-option label="导出" value="EXPORT"/>
            <el-option label="其他" value="OTHER"/>
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status" class="field-status">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option label="成功" :value="1"/>
            <el-option label="失败" :value="0"/>
          </el-select>
        </el-form-item>

        <el-form-item label="操作人" prop="userName" class="field-user">
          <el-input v-model="query.userName" placeholder="请输入操作人" clearable/>
        </el-form-item>

        <el-form-item label="时间" class="field-date">
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
          <h2>日志明细</h2>
          <span class="record-count">共 {{ total }} 条记录</span>
        </div>
        <div class="table-legend">
          <span class="legend-item"><i class="legend-dot is-success"></i>执行成功</span>
          <span class="legend-item"><i class="legend-dot is-danger"></i>执行失败</span>
        </div>
      </div>

      <el-table :data="logList" class="log-table" border row-key="logId" v-loading="loading">
        <el-table-column label="日志编号" width="104" align="left">
          <template #default="scope">
            <span class="log-id">#{{ scope.row.logId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作人" width="128" align="left">
          <template #default="scope">
            <div class="operator-cell">
              <span class="operator-avatar">{{ scope.row.userName?.charAt(0) || '—' }}</span>
              <span class="operator-name">{{ scope.row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="108" align="left"/>
        <el-table-column prop="business" label="业务" width="140" align="left" show-overflow-tooltip/>
        <el-table-column label="类型" width="92" align="center">
          <template #default="scope">
            <span class="type-code">{{ scope.row.operType }}</span>
          </template>
        </el-table-column>
        <el-table-column label="请求方式" width="104" align="center">
          <template #default="scope">
            <span class="method-badge" :class="`method-${scope.row.requestMethod?.toLowerCase()}`">
              {{ scope.row.requestMethod }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="requestUri" label="请求地址" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <code class="request-uri">{{ scope.row.requestUri }}</code>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="94" align="right">
          <template #default="scope">
            <span class="cost-time">{{ scope.row.costTime }}<small>ms</small></span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="92" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" effect="plain" class="status-tag">
              <span class="status-dot"></span>
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="176" align="left">
          <template #default="scope">
            <span class="time-text">{{ scope.row.operTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="88" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="View" class="detail-button" @click="handleDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <span class="empty-icon"><el-icon><Search /></el-icon></span>
            <p>没有找到匹配的操作日志</p>
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
                    @pagination="getList"/>
      </div>
    </section>

    <el-drawer v-model="detailOpen" class="operlog-drawer" size="520px">
      <template #header>
        <div class="drawer-heading">
          <span class="drawer-heading-icon"><el-icon><Document /></el-icon></span>
          <div>
            <h3>日志详情</h3>
            <p>请求链路与执行结果</p>
          </div>
        </div>
      </template>

      <div class="drawer-content">
        <el-descriptions :column="1" border class="log-descriptions">
          <el-descriptions-item label="日志编号">{{ detail.logId }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ detail.userName }}（ID: {{ detail.userId }}）</el-descriptions-item>
          <el-descriptions-item label="模块 / 业务">{{ detail.module }} / {{ detail.business }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">{{ detail.operType }}</el-descriptions-item>
          <el-descriptions-item label="请求">{{ detail.requestMethod }} {{ detail.requestUri }}</el-descriptions-item>
          <el-descriptions-item label="耗时 / 状态">
            {{ detail.costTime }} ms ·
            <el-tag :type="detail.status === 1 ? 'success' : 'danger'" size="small">
              {{ detail.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ detail.operTime }}</el-descriptions-item>
          <el-descriptions-item label="traceId">
            <code class="trace-id">{{ detail.traceId }}</code>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-block">
          <div class="detail-title"><el-icon><Upload /></el-icon><span>请求参数</span></div>
          <pre class="detail-pre">{{ prettyJson(detail.requestParam) }}</pre>
        </div>

        <div class="detail-block">
          <div class="detail-title"><el-icon><Finished /></el-icon><span>执行结果</span></div>
          <pre class="detail-pre">{{ detail.result }}</pre>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import Pagination from "@/components/Pagination/index.vue";
import { onMounted, ref } from "vue";
import { selectOperationLogList } from "~/API/monitor/operlog.js";

const queryRef = ref()
const loading = ref(false)
const logList = ref([])
const total = ref(0)
const detailOpen = ref(false)
const detail = ref({})

// 日期范围是数组 [start, end]，后端要的是两个独立字符串，所以单独存
const dateRange = ref([])

const query = ref({
  pageNum: 1,
  pageSize: 10,
  module: null,
  operType: null,
  status: null,
  userName: null
})

// 查询数据
const getList = () => {
  loading.value = true

  // 把日期数组拆成 beginTime / endTime，这是后端 domain 里的两个查询字段
  const params = { ...query.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }

  selectOperationLogList(params).then(res => {
    logList.value = res.data.rows
    total.value = res.data.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 搜索
const handleQuery = () => {
  query.value.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryRef.value.resetFields()
  dateRange.value = []
  query.value.pageNum = 1
  getList()
}

// 详情
const handleDetail = (row) => {
  detail.value = row
  detailOpen.value = true
}

// JSON 美化：解析失败就原样返回（比如 "参数解析失败" 这种非 JSON 值）
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
})
</script>

<style scoped>
.operlog-page {
  min-height: 100%;
  padding: 20px;
  background:
      radial-gradient(circle at 92% -10%, rgba(49, 87, 213, 0.07), transparent 30%),
      #f7f9fc;
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 12px;
}

.heading-main {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.heading-icon {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  color: #ffffff;
  font-size: 18px;
  background: linear-gradient(135deg, #3157d5, #2547b8);
  box-shadow: 0 5px 12px rgba(49, 87, 213, 0.16);
}

.page-heading h1 {
  margin: 0;
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 650;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.page-heading p {
  margin: 4px 0 0;
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.4;
}

.heading-stat {
  display: flex;
  align-items: center;
  gap: 7px;
  flex: 0 0 auto;
  padding: 7px 11px;
  border: 1px solid var(--border-default);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.82);
}

.heading-stat-label {
  color: var(--text-secondary);
  font-size: 11px;
}

.live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.1);
}

.heading-stat-value {
  color: var(--text-primary);
  font-size: 17px;
  font-weight: 650;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.heading-stat-unit {
  color: var(--text-tertiary);
  font-size: 11px;
}

.query-panel,
.table-panel {
  border: 1px solid var(--border-default);
  border-radius: 11px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(23, 32, 51, 0.02);
}

.query-panel {
  margin-bottom: 12px;
  padding: 10px 14px;
}

.table-title h2 {
  margin: 0;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 600;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
  margin: 0;
}

.query-form.el-form--inline :deep(.el-form-item) {
  display: inline-flex;
  align-items: center;
  flex: 0 0 auto;
  min-width: 0;
  margin: 0;
}

.query-form.el-form--inline :deep(.el-form-item__label) {
  flex: 0 0 auto;
  height: auto;
  padding: 0 7px 0 0;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
  line-height: 32px;
}

.query-form.el-form--inline :deep(.el-form-item__content) {
  flex: 0 0 auto;
  width: auto;
  margin-left: 0;
  line-height: 32px;
}

.field-module :deep(.el-select) {
  width: 124px;
}

.field-type :deep(.el-select) {
  width: 104px;
}

.field-status :deep(.el-select) {
  width: 88px;
}

.field-user :deep(.el-input) {
  width: 124px;
}

.field-date :deep(.el-date-editor) {
  width: 208px;
}

.query-form :deep(.el-input__wrapper),
.query-form :deep(.el-select__wrapper) {
  min-height: 32px;
  border-radius: 6px;
  font-size: 12px;
  box-shadow: 0 0 0 1px var(--border-strong) inset;
  transition: box-shadow 0.18s ease, background-color 0.18s ease;
}

.query-form :deep(.el-input__wrapper:hover),
.query-form :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px #aab7d3 inset;
}

.query-form :deep(.el-input__wrapper.is-focus),
.query-form :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--brand-500) inset, 0 0 0 3px rgba(49, 87, 213, 0.1);
}

.query-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.query-actions :deep(.el-button) {
  min-width: 68px;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.table-panel {
  overflow: hidden;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 62px;
  padding: 0 22px;
  border-bottom: 1px solid #edf0f5;
}

.table-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.record-count {
  color: var(--text-tertiary);
  font-size: 12px;
}

.table-legend {
  display: flex;
  align-items: center;
  gap: 16px;
  color: var(--text-secondary);
  font-size: 11px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.legend-dot.is-success {
  background: #22c55e;
}

.legend-dot.is-danger {
  background: #f04438;
}

.log-table {
  --el-table-border-color: #edf0f5;
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #667085;
  --el-table-row-hover-bg-color: #f8faff;
  --el-table-text-color: #344054;
}

.log-table :deep(.el-table__inner-wrapper::before) {
  background: #edf0f5;
}

.log-table :deep(.el-table__header-wrapper th.el-table__cell) {
  height: 43px;
  padding: 0 14px;
  color: #667085;
  font-size: 12px;
  font-weight: 600;
  background: #f8fafc;
}

.log-table :deep(.el-table__body td.el-table__cell) {
  height: 52px;
  padding: 6px 14px;
  color: #344054;
  font-size: 13px;
  border-bottom: 1px solid #edf0f5;
}

.log-table :deep(.el-table__row:hover > td.el-table__cell) {
  background: #f8faff;
}

.log-table :deep(.el-table__cell .cell) {
  padding: 0;
  line-height: 20px;
}

.log-table :deep(.el-table__fixed-right::before) {
  background: #edf0f5;
}

.log-id {
  color: #667085;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.operator-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
}

.operator-avatar {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 25px;
  height: 25px;
  border: 1px solid #dce3f8;
  border-radius: 8px;
  color: var(--brand-600);
  background: var(--brand-50);
  font-size: 11px;
  font-weight: 600;
}

.operator-name {
  overflow: hidden;
  color: #344054;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-code {
  color: #667085;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  font-size: 11px;
  letter-spacing: 0.02em;
}

.method-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 46px;
  height: 21px;
  padding: 0 7px;
  border-radius: 5px;
  color: #475467;
  background: #f2f4f7;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.method-get {
  color: #175cd3;
  background: #eff8ff;
}

.method-post {
  color: #067647;
  background: #ecfdf3;
}

.method-put {
  color: #b54708;
  background: #fffaeb;
}

.method-delete {
  color: #b42318;
  background: #fef3f2;
}

.request-uri {
  color: #667085;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  font-size: 11px;
}

.cost-time {
  color: #475467;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.cost-time small {
  margin-left: 3px;
  color: #98a2b3;
  font-size: 10px;
}

.status-tag {
  height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  line-height: 20px;
}

.status-tag .status-dot {
  width: 5px;
  height: 5px;
  margin-right: 5px;
  border-radius: 50%;
  background: currentColor;
}

.time-text {
  color: #667085;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.detail-button {
  padding: 0;
  font-weight: 500;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 260px;
  color: var(--text-tertiary);
}

.empty-icon {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  margin-bottom: 12px;
  border: 1px solid var(--border-default);
  border-radius: 14px;
  color: #98a2b3;
  background: #f8fafc;
  font-size: 20px;
}

.empty-state p {
  margin: 0 0 5px;
  color: #475467;
  font-size: 13px;
  font-weight: 500;
}

.empty-state > span:last-child {
  font-size: 12px;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 60px;
  padding: 0 22px;
  border-top: 1px solid #edf0f5;
  background: #ffffff;
}

.footer-summary {
  display: flex;
  align-items: center;
  gap: 7px;
  color: var(--text-tertiary);
  font-size: 11px;
  white-space: nowrap;
}

.footer-dot {
  color: #cbd5e1;
}

.log-pagination {
  margin-left: auto;
}

.log-pagination :deep(.container) {
  padding: 0;
  border-top: 0;
}

.log-pagination :deep(.el-pagination) {
  align-items: center;
  gap: 4px;
}

.log-pagination :deep(.el-pager li) {
  min-width: 28px;
  height: 28px;
  border-radius: 6px;
  line-height: 26px;
}

.log-pagination :deep(.el-pager li.is-active) {
  box-shadow: 0 2px 6px rgba(49, 87, 213, 0.2);
}

.log-pagination :deep(.el-pagination__jump) {
  margin-left: 10px;
}

.log-pagination :deep(.el-pagination__sizes .el-select__wrapper) {
  min-height: 28px;
  border-radius: 6px;
}

@media (max-width: 1000px) {
  .query-form {
    gap: 8px 12px;
  }

  .query-actions {
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .operlog-page {
    padding: 14px;
  }

  .page-heading {
    align-items: flex-start;
    gap: 10px;
  }

  .heading-icon {
    width: 34px;
    height: 34px;
    border-radius: 9px;
    font-size: 16px;
  }

  .page-heading h1 {
    font-size: 18px;
  }

  .page-heading p {
    font-size: 11px;
  }

  .heading-stat {
    gap: 5px;
    padding: 6px 8px;
  }

  .heading-stat-label {
    display: none;
  }

  .query-panel {
    padding: 10px;
  }

  .query-form {
    display: grid;
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .query-form.el-form--inline :deep(.el-form-item) {
    width: 100%;
  }

  .query-form.el-form--inline :deep(.el-form-item__content) {
    flex: 1;
    width: 100%;
  }

  .field-module :deep(.el-select),
  .field-type :deep(.el-select),
  .field-status :deep(.el-select),
  .field-user :deep(.el-input),
  .field-date :deep(.el-date-editor) {
    width: 100%;
  }

  .query-actions {
    width: 100%;
    margin-left: 0;
  }

  .query-actions :deep(.el-button) {
    flex: 1;
  }

  .table-toolbar {
    min-height: 54px;
    padding: 0 14px;
  }

  .table-legend {
    display: none;
  }

  .table-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    padding: 12px 14px;
  }

  .log-pagination {
    align-self: flex-end;
    max-width: 100%;
    overflow-x: auto;
  }
}
</style>

<style>
.operlog-drawer .el-drawer__header {
  margin-bottom: 0;
  padding: 20px 22px 16px;
  border-bottom: 1px solid var(--border-default);
  background: #ffffff;
}

.operlog-drawer .el-drawer__body {
  padding: 20px 22px 28px;
  background: #f8fafc;
}

.operlog-drawer .drawer-heading {
  display: flex;
  align-items: center;
  gap: 11px;
}

.operlog-drawer .drawer-heading-icon {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  color: var(--brand-500);
  background: var(--brand-50);
  font-size: 18px;
}

.operlog-drawer .drawer-heading h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
}

.operlog-drawer .drawer-heading p {
  margin: 3px 0 0;
  color: var(--text-tertiary);
  font-size: 11px;
}

.operlog-drawer .log-descriptions {
  --el-descriptions-table-border: 1px solid var(--border-default);
  --el-descriptions-item-bordered-label-background: #f8fafc;
}

.operlog-drawer .el-descriptions__label {
  width: 112px;
  color: var(--text-secondary);
  font-weight: 500;
}

.operlog-drawer .trace-id {
  padding: 3px 6px;
  border: 1px solid var(--border-default);
  border-radius: 5px;
  color: #475467;
  background: #f8fafc;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  font-size: 11px;
  word-break: break-all;
}

.operlog-drawer .detail-block {
  margin-top: 20px;
}

.operlog-drawer .detail-title {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 8px;
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 600;
}

.operlog-drawer .detail-title .el-icon {
  color: var(--brand-500);
  font-size: 15px;
}

.operlog-drawer .detail-pre {
  margin: 0;
  padding: 14px 16px;
  overflow: auto;
  border: 1px solid #273449;
  border-radius: 9px;
  color: #dbe3f0;
  background: #111827;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  font-size: 12px;
  line-height: 1.7;
  max-height: 320px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
