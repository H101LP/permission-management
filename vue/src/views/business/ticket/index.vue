<template>
  <div class="management-page">
    <header class="page-heading">
      <div class="heading-main">
        <div class="heading-icon">
          <el-icon><Tickets /></el-icon>
        </div>
        <div>
          <h1>工单管理</h1>
          <p>管理工单分类、处理进度与服务闭环</p>
        </div>
      </div>
      <div class="heading-stat">
        <span class="live-dot"></span>
        <span>当前结果</span>
        <strong>{{ total }}</strong>
        <span>条</span>
      </div>
    </header>

    <section class="query-panel">
      <el-form :model="query" ref="queryRef" inline class="query-form">
        <el-form-item label="工单标题" prop="ticketTitle" class="field-lg">
          <el-input v-model="query.ticketTitle" placeholder="请输入工单标题" clearable
                    @keyup.enter="handleQuery"/>
        </el-form-item>
        <el-form-item label="类型" prop="ticketType" class="field-md">
          <el-select v-model="query.ticketType" placeholder="全部" clearable :value-on-clear="null">
            <el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status" class="field-md">
          <el-select v-model="query.status" placeholder="全部" clearable :value-on-clear="null">
            <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value"/>
          </el-select>
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
          <h2>工单台账</h2>
          <span class="record-count">共 {{ total }} 条记录</span>
        </div>
        <div class="table-actions">
          <el-button v-has-perm="['business:ticket:add']" type="primary" icon="Plus"
                     plain @click="handleInsert">新增</el-button>
        </div>
      </div>

      <el-table :data="ticketList" class="data-table" border row-key="ticketId">
        <el-table-column prop="ticketNo" label="工单编号" width="160" align="center">
          <template #default="scope"><span class="cell-code">{{ scope.row.ticketNo }}</span></template>
        </el-table-column>
        <el-table-column prop="ticketTitle" label="工单标题" min-width="160" align="center" show-overflow-tooltip>
          <template #default="scope"><span class="cell-main">{{ scope.row.ticketTitle }}</span></template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="typeTagType(scope.row.ticketType)" effect="plain" class="status-tag">
              <span class="status-dot"></span>
              {{ typeText(scope.row.ticketType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归属部门" width="110" align="center">
          <template #default="scope"><span class="cell-muted">{{ deptMap[scope.row.deptId] || scope.row.deptId }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" effect="plain" class="status-tag">
              <span class="status-dot"></span>
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center"/>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <div class="row-actions">
              <el-button v-has-perm="['business:ticket:edit']" link type="primary" icon="Edit"
                         @click="handleUpdate(scope.row)">修改</el-button>
              <el-button v-has-perm="['business:ticket:remove']" link type="danger" icon="Delete"
                         @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <span class="empty-icon"><el-icon><Search /></el-icon></span>
            <p>没有找到匹配的工单记录</p>
            <span>请调整筛选条件或新增工单</span>
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

    <vxe-modal :title="title" v-model="Open" class-name="form-modal" width="520px" showFooter show-maximize resize>
      <template #default>
        <el-form ref="ticketRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="工单编号" prop="ticketNo">
            <el-input v-model="form.ticketNo" placeholder="请输入唯一工单编号"/>
          </el-form-item>
          <el-form-item label="工单标题" prop="ticketTitle">
            <el-input v-model="form.ticketTitle" placeholder="请输入工单标题"/>
          </el-form-item>
          <el-form-item label="类型" prop="ticketType">
            <el-select v-model="form.ticketType" class="full-width">
              <el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="归属部门" prop="deptId">
            <el-select v-model="form.deptId" placeholder="不选则归入当前登录部门" class="full-width">
              <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" class="full-width">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可不填"/>
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <div>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="Open = false">取消</el-button>
        </div>
      </template>
    </vxe-modal>
  </div>
</template>

<script setup>
import Pagination from "@/components/Pagination/index.vue";
import { onMounted, ref } from "vue";
import { VxeModal } from "vxe-pc-ui";
import { ElMessage, ElMessageBox } from "element-plus";
import { selectTicketList, insertTicket, updateTicket, deleteTicketById } from "~/API/business/ticket.js";
import { selectDeptList } from "~/API/system/dept.js";

// 类型字典
const typeOptions = [
  { value: 0, label: "故障" },
  { value: 1, label: "需求" },
  { value: 2, label: "咨询" }
]
// 状态字典
const statusOptions = [
  { value: 0, label: "待处理" },
  { value: 1, label: "处理中" },
  { value: 2, label: "已关闭" }
]
const typeText = (v) => (typeOptions.find(d => d.value === v) || {}).label || "—"
const typeTagType = (v) => ({ 0: "danger", 1: "warning", 2: "info" }[v] || "info")
const statusText = (v) => (statusOptions.find(d => d.value === v) || {}).label || "—"
const statusTagType = (v) => ({ 0: "warning", 1: "primary", 2: "info" }[v] || "info")

// 查询参数
const query = ref({
  pageNum: 1,
  pageSize: 10,
  ticketTitle: null,
  ticketType: null,
  status: null
})
const ticketList = ref([])
const total = ref(0)
const queryRef = ref()

// 弹窗
const Open = ref(false)
const title = ref("")
const form = ref({})
const ticketRef = ref()
const rules = ref({
  ticketNo: [{ required: true, message: "请输入工单编号", trigger: "blur" }],
  ticketTitle: [{ required: true, message: "请输入工单标题", trigger: "blur" }]
})

// 部门
const deptMap = ref({})
const deptOptions = ref([])

const getList = () => {
  selectTicketList(query.value).then(res => {
    ticketList.value = res.data.rows;
    total.value = res.data.total;
  })
}

const handleQuery = () => {
  query.value.pageNum = 1;
  getList();
}

const resetQuery = () => {
  queryRef.value.resetFields();
  query.value.pageNum = 1;
  getList();
}

const handleInsert = () => {
  form.value = {
    ticketId: null, ticketNo: null, ticketTitle: null,
    ticketType: 0, deptId: null, status: 0, remark: null
  }
  title.value = "新增工单"
  Open.value = true
  ticketRef.value && ticketRef.value.clearValidate()
}

const handleUpdate = (row) => {
  form.value = { ...row }
  title.value = "修改工单"
  Open.value = true
  ticketRef.value && ticketRef.value.clearValidate()
}

const submitForm = () => {
  ticketRef.value.validate(valid => {
    if (!valid) return
    const isEdit = form.value.ticketId != null
    const request = isEdit ? updateTicket : insertTicket
    request(form.value).then(() => {
      ElMessage.success(isEdit ? "修改成功" : "新增成功")
      Open.value = false
      getList()
    })
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm("是否确认删除该工单？", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    return deleteTicketById(row.ticketId)
  }).then(() => {
    ElMessage.success("删除成功")
    getList()
  }).catch(() => {})
}

const loadDept = () => {
  selectDeptList({}).then(res => {
    const map = {}
    const flat = []
    const walk = (nodes) => (nodes || []).forEach(n => {
      map[n.deptId] = n.deptName
      flat.push({ deptId: n.deptId, deptName: n.deptName })
      if (n.children && n.children.length) walk(n.children)
    })
    walk(res.data)
    deptMap.value = map
    deptOptions.value = flat
  })
}

onMounted(() => {
  getList();
  loadDept();
})
</script>

<style scoped>
</style>
