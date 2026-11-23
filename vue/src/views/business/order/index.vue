<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="query" ref="queryRef" label-width="80px" inline>
      <el-form-item label="订单编号" prop="orderNo">
        <el-input v-model="query.orderNo" placeholder="请输入订单编号" clearable
                  style="width: 200px" @keyup.enter="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="query.status" placeholder="全部" clearable
                   :value-on-clear="null" style="width: 140px">
          <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 顶部按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"/>
      <el-button v-has-perm="['business:order:add']" type="primary" icon="Plus"
                 plain @click="handleInsert">新增</el-button>
    </el-row>

    <!-- 列表 -->
    <el-table :data="orderList" style="width: 100%" border>
      <el-table-column prop="orderNo" label="订单编号" width="160" align="center"/>
      <el-table-column prop="orderName" label="订单名称" min-width="150" align="center" show-overflow-tooltip/>
      <el-table-column prop="customerName" label="客户" width="130" align="center" show-overflow-tooltip/>
      <el-table-column label="金额" width="120" align="center">
        <template #default="scope">{{ formatMoney(scope.row.amount) }}</template>
      </el-table-column>
      <el-table-column label="归属部门" width="110" align="center">
        <template #default="scope">{{ deptMap[scope.row.deptId] || scope.row.deptId }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" align="center"/>
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button v-has-perm="['business:order:edit']" link type="primary" icon="Edit"
                     @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-has-perm="['business:order:remove']" link type="danger" icon="Delete"
                     @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination :total="total"
                v-model:page="query.pageNum"
                v-model:limit="query.pageSize"
                @pagination="getList"/>

    <!-- 新增 / 修改 -->
    <vxe-modal :title="title" v-model="Open" width="520px" showFooter show-maximize resize>
      <template #default>
        <el-form ref="orderRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="订单编号" prop="orderNo">
            <el-input v-model="form.orderNo" placeholder="请输入唯一订单编号"/>
          </el-form-item>
          <el-form-item label="订单名称" prop="orderName">
            <el-input v-model="form.orderName" placeholder="请输入订单名称 / 摘要"/>
          </el-form-item>
          <el-form-item label="客户名称" prop="customerName">
            <el-input v-model="form.customerName" placeholder="可不填"/>
          </el-form-item>
          <el-form-item label="金额" prop="amount">
            <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1000"
                             controls-position="right" style="width: 100%"/>
          </el-form-item>
          <el-form-item label="归属部门" prop="deptId">
            <el-select v-model="form.deptId" placeholder="不选则归入当前登录部门" style="width: 100%">
              <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" style="width: 100%">
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
import { selectOrderList, insertOrder, updateOrder, deleteOrderById } from "~/API/business/order.js";
import { selectDeptList } from "~/API/system/dept.js";

// 状态字典
const statusOptions = [
  { value: 0, label: "待确认" },
  { value: 1, label: "已确认" },
  { value: 2, label: "已完成" }
]
const statusText = (v) => (statusOptions.find(d => d.value === v) || {}).label || "—"
const statusTagType = (v) => ({ 0: "warning", 1: "success", 2: "info" }[v] || "info")
const formatMoney = (v) => (v === null || v === undefined || v === "") ? "—" : Number(v).toFixed(2)

// 查询参数
const query = ref({
  pageNum: 1,
  pageSize: 10,
  orderNo: null,
  status: null
})
const orderList = ref([])
const total = ref(0)
const queryRef = ref()

// 弹窗
const Open = ref(false)
const title = ref("")
const form = ref({})
const orderRef = ref()
const rules = ref({
  orderNo: [{ required: true, message: "请输入订单编号", trigger: "blur" }],
  orderName: [{ required: true, message: "请输入订单名称", trigger: "blur" }]
})

// 部门
const deptMap = ref({})
const deptOptions = ref([])

const getList = () => {
  selectOrderList(query.value).then(res => {
    orderList.value = res.data.rows;
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
    orderId: null, orderNo: null, orderName: null, customerName: null,
    deptId: null, amount: null, status: 0, remark: null
  }
  title.value = "新增订单"
  Open.value = true
  orderRef.value && orderRef.value.clearValidate()
}

const handleUpdate = (row) => {
  form.value = { ...row }
  title.value = "修改订单"
  Open.value = true
  orderRef.value && orderRef.value.clearValidate()
}

const submitForm = () => {
  orderRef.value.validate(valid => {
    if (!valid) return
    const isEdit = form.value.orderId != null
    const request = isEdit ? updateOrder : insertOrder
    request(form.value).then(() => {
      ElMessage.success(isEdit ? "修改成功" : "新增成功")
      Open.value = false
      getList()
    })
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm("是否确认删除该订单？", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    return deleteOrderById(row.orderId)
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
