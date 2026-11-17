<template>
  <div class="app-container">

    <!-- 顶部查询区 -->
    <el-form :model="query" ref="queryRef" label-width="80px" inline>
      <el-form-item label="操作模块" prop="module">
        <el-select v-model="query.module" placeholder="全部模块" clearable style="width: 140px">
          <el-option label="用户管理" value="用户管理"/>
          <el-option label="角色管理" value="角色管理"/>
          <el-option label="部门管理" value="部门管理"/>
          <el-option label="菜单管理" value="菜单管理"/>
        </el-select>
      </el-form-item>

      <el-form-item label="操作类型" prop="operType">
        <el-select v-model="query.operType" placeholder="全部类型" clearable style="width: 120px">
          <el-option label="新增" value="INSERT"/>
          <el-option label="修改" value="UPDATE"/>
          <el-option label="删除" value="DELETE"/>
          <el-option label="导出" value="EXPORT"/>
          <el-option label="其他" value="OTHER"/>
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 100px">
          <el-option label="成功" :value="1"/>
          <el-option label="失败" :value="0"/>
        </el-select>
      </el-form-item>

      <el-form-item label="操作人" prop="userName">
        <el-input v-model="query.userName" placeholder="请输入操作人" clearable style="width: 140px"/>
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
      <el-table-column prop="userName" label="操作人" width="110" align="center"/>
      <el-table-column prop="module" label="模块" width="110" align="center"/>
      <el-table-column prop="business" label="业务" width="130" align="center"/>
      <el-table-column prop="operType" label="类型" width="90" align="center"/>
      <el-table-column prop="requestMethod" label="请求方式" width="100" align="center"/>
      <el-table-column prop="requestUri" label="请求地址" min-width="200" show-overflow-tooltip/>
      <el-table-column label="耗时" width="90" align="center">
        <template #default="scope">
          <span>{{ scope.row.costTime }} ms</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operTime" label="操作时间" width="170" align="center"/>
      <el-table-column label="操作" width="90" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination :total="total"
                v-model:page="query.pageNum"
                v-model:limit="query.pageSize"
                @pagination="getList"/>

    <!--
      详情抽屉。
      为什么用 pre + 滚动：
      request_param / result 是 text 字段，一个用户新增请求的 JSON 可能几百字符，
      直接铺在普通 div 里会把抽屉撑破。pre 保留换行，max-height + overflow 保证不撑破。
    -->
    <el-drawer v-model="detailOpen" title="日志详情" size="45%">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="日志编号">{{ detail.logId }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.userName }}（ID: {{ detail.userId }}）</el-descriptions-item>
        <el-descriptions-item label="模块 / 业务">{{ detail.module }} / {{ detail.business }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ detail.operType }}</el-descriptions-item>
        <el-descriptions-item label="请求">
          {{ detail.requestMethod }} {{ detail.requestUri }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时 / 状态">
          {{ detail.costTime }} ms ·
          <el-tag :type="detail.status === 1 ? 'success' : 'danger'" size="small">
            {{ detail.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.operTime }}</el-descriptions-item>
        <el-descriptions-item label="traceId">
          <span style="font-family: monospace">{{ detail.traceId }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="detail-block">
        <div class="detail-title">请求参数</div>
        <pre class="detail-pre">{{ prettyJson(detail.requestParam) }}</pre>
      </div>

      <div class="detail-block">
        <div class="detail-title">执行结果</div>
        <pre class="detail-pre">{{ detail.result }}</pre>
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
  max-height: 300px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.6;
  margin: 0;
}
</style>
