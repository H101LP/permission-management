<template>
  <div class="management-page">
    <header class="page-heading">
      <div class="heading-main">
        <div class="heading-icon">
          <el-icon><Box /></el-icon>
        </div>
        <div>
          <h1>资产管理</h1>
          <p>维护资产台账、归属部门与使用状态</p>
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
        <el-form-item label="资产名称" prop="assetName" class="field-lg">
          <el-input v-model="query.assetName" placeholder="请输入资产名称" clearable
                    @keyup.enter="handleQuery"/>
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
          <h2>资产台账</h2>
          <span class="record-count">共 {{ total }} 条记录</span>
        </div>
        <div class="table-actions">
          <el-button v-has-perm="['business:asset:add']" type="primary" icon="Plus"
                     plain @click="handleInsert">新增</el-button>
        </div>
      </div>

      <el-table :data="assetList" class="data-table" border row-key="assetId">
        <el-table-column prop="assetId" label="编号" width="90" align="center">
          <template #default="scope"><span class="cell-code">{{ scope.row.assetId }}</span></template>
        </el-table-column>
        <el-table-column prop="assetName" label="资产名称" min-width="150" align="center" show-overflow-tooltip>
          <template #default="scope"><span class="cell-main">{{ scope.row.assetName }}</span></template>
        </el-table-column>
        <el-table-column prop="assetCode" label="资产编号" width="150" align="center">
          <template #default="scope"><span class="cell-code">{{ scope.row.assetCode }}</span></template>
        </el-table-column>
        <el-table-column label="归属部门" width="120" align="center">
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
        <el-table-column label="原值" width="120" align="center">
          <template #default="scope"><span class="cell-money">{{ formatMoney(scope.row.price) }}</span></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center"/>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <div class="row-actions">
              <el-button v-has-perm="['business:asset:edit']" link type="primary" icon="Edit"
                         @click="handleUpdate(scope.row)">修改</el-button>
              <el-button v-has-perm="['business:asset:remove']" link type="danger" icon="Delete"
                         @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <span class="empty-icon"><el-icon><Search /></el-icon></span>
            <p>没有找到匹配的资产记录</p>
            <span>请调整筛选条件或新增资产</span>
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
        <el-form ref="assetRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="资产名称" prop="assetName">
            <el-input v-model="form.assetName" placeholder="请输入资产名称"/>
          </el-form-item>
          <el-form-item label="资产编号" prop="assetCode">
            <el-input v-model="form.assetCode" placeholder="可不填"/>
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
          <el-form-item label="原值" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="100"
                             controls-position="right" class="full-width"/>
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
import { selectAssetList, insertAsset, updateAsset, deleteAssetById } from "~/API/business/asset.js";
import { selectDeptList } from "~/API/system/dept.js";

// 状态字典
const statusOptions = [
  { value: 0, label: "闲置" },
  { value: 1, label: "在用" },
  { value: 2, label: "报废" }
]
const statusText = (v) => (statusOptions.find(d => d.value === v) || {}).label || "—"
const statusTagType = (v) => ({ 0: "info", 1: "success", 2: "danger" }[v] || "info")
const formatMoney = (v) => (v === null || v === undefined || v === "") ? "—" : Number(v).toFixed(2)

// 查询参数
const query = ref({
  pageNum: 1,
  pageSize: 10,
  assetName: null,
  status: null
})
const assetList = ref([])
const total = ref(0)
const queryRef = ref()

// 弹窗
const Open = ref(false)
const title = ref("")
const form = ref({})
const assetRef = ref()
const rules = ref({
  assetName: [{ required: true, message: "请输入资产名称", trigger: "blur" }]
})

// 部门
const deptMap = ref({})
const deptOptions = ref([])

const getList = () => {
  selectAssetList(query.value).then(res => {
    assetList.value = res.data.rows;
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
    assetId: null, assetName: null, assetCode: null,
    deptId: null, status: 0, price: null, remark: null
  }
  title.value = "新增资产"
  Open.value = true
  assetRef.value && assetRef.value.clearValidate()
}

const handleUpdate = (row) => {
  // 列表行字段完整，直接拷贝，省一次详情请求
  form.value = { ...row }
  title.value = "修改资产"
  Open.value = true
  assetRef.value && assetRef.value.clearValidate()
}

const submitForm = () => {
  assetRef.value.validate(valid => {
    if (!valid) return
    const isEdit = form.value.assetId != null
    const request = isEdit ? updateAsset : insertAsset
    request(form.value).then(() => {
      ElMessage.success(isEdit ? "修改成功" : "新增成功")
      Open.value = false
      getList()
    })
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm("是否确认删除该资产？", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    return deleteAssetById(row.assetId)
  }).then(() => {
    ElMessage.success("删除成功")
    getList()
  }).catch(() => {})
}

// 部门树摊平成 下拉列表 + id→名称 映射
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
