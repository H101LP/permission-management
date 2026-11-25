<template>
  <div class="management-page">
    <header class="page-heading">
      <div class="heading-main">
        <div class="heading-icon">
          <el-icon><OfficeBuilding /></el-icon>
        </div>
        <div>
          <h1>部门管理</h1>
          <p>维护组织层级、负责人与启用状态</p>
        </div>
      </div>
      <div class="heading-stat">
        <span class="meta-dot"></span>
        <span>组织架构</span>
      </div>
    </header>

    <section class="query-panel">
      <el-form :model="query" ref="queryRef" label-width="80px" inline class="query-form">
        <el-form-item label="部门名称" prop="deptName" class="field-lg">
          <el-input v-model="query.deptName" placeholder="请输入部门名称" clearable/>
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
          <h2>部门列表</h2>
          <span class="record-count">当前 {{ deptList.length }} 个根部门</span>
        </div>
        <div class="table-actions">
          <el-button v-has-perm="['system:dept:add']" type="primary" icon="Plus"
                     @click="handleInsert" plain>新增</el-button>
        </div>
      </div>

      <el-table :data="deptList" class="data-table" border row-key="deptId"
                :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
        <el-table-column label="部门名称" min-width="180" align="center">
          <template #default="scope">
            <span class="cell-main">{{ scope.row.deptName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="80" align="center"/>
        <el-table-column label="负责人" width="120" align="center">
          <template #default="scope">
            <span class="cell-main">{{ scope.row.leader || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" class="status-tag" effect="plain">
              <span class="status-dot"></span>
              {{ scope.row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center"/>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <div class="row-actions">
              <el-button v-has-perm="['system:dept:edit']" link type="primary" icon="Edit"
                         @click="handleUpdate(scope.row)">修改</el-button>
              <el-button v-has-perm="['system:dept:remove']" link type="danger" icon="Delete"
                         @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <span class="empty-icon"><el-icon><Search /></el-icon></span>
            <p>没有找到匹配的部门</p>
            <span>请调整查询条件后再试</span>
          </div>
        </template>
      </el-table>
    </section>

    <!-- 新增/修改弹窗 -->
    <vxe-modal class-name="form-modal" :title="title" v-model="Open" width="40%" showFooter show-maximize resize>
      <template #default>
        <el-form ref="deptRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="上级部门">
            <el-tree-select
                v-model="form.parentId"
                :data="deptTree"
                :props="{value: 'deptId', label: 'deptName', children: 'children'}"
                check-strictly
                value-key="deptId"
                :disabled="form.deptId != null"
                placeholder="请选择上级部门"/>
          </el-form-item>
          <el-form-item label="部门名称" prop="deptName">
            <el-input v-model="form.deptName" placeholder="请输入部门名称"/>
          </el-form-item>
          <el-form-item label="显示排序" prop="orderNum">
            <el-input-number v-model="form.orderNum" min="0"/>
          </el-form-item>
          <el-form-item label="负责人">
            <el-input v-model="form.leader" placeholder="请输入负责人"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio value="0">正常</el-radio>
              <el-radio value="1">停用</el-radio>
            </el-radio-group>
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
import {onMounted, ref} from "vue";
import {selectDeptList, selectDeptById, selectDeptTree,
  insertDept, updateDept, deleteDeptById} from "~/API/system/dept.js";
import {VxeModal} from "vxe-pc-ui";
import {ElMessage, ElMessageBox} from "element-plus";

// 查询参数
const query = ref({ deptName: null });
// 查询表单实例
const queryRef = ref();
// 弹窗表单实例
const deptRef = ref();
// 部门树列表
const deptList = ref([]);
// 上级部门下拉数据
const deptTree = ref([]);
// 弹窗开关
const Open = ref(false);
// 弹窗标题
const title = ref('');

// 弹窗表单
const form = ref({
  deptId: null,
  parentId: 0,
  deptName: null,
  orderNum: 0,
  leader: '',
  status: '0'
});

// 表单校验
const rules = ref({
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
});

/**
 * 加载上级部门下拉数据
 * 关键：包一层虚拟根节点 deptId=0，
 * 让 form.parentId=0（顶级部门）在下拉里有对应项可以显示和选择
 */
const loadDeptTree = () => {
  return selectDeptTree().then(res => {
    deptTree.value = [{ deptId: 0, deptName: '主层级', children: res.data }];
  });
};

// 查询部门列表（后端已返回树，直接赋值）
const getList = () => {
  selectDeptList(query.value).then(res => {
    deptList.value = res.data;
  });
};

// 搜索
const handleQuery = () => getList();

// 重置
const resetQuery = () => {
  queryRef.value.resetFields();
  getList();
};

// 新增
const handleInsert = () => {
  form.value = { deptId: null, parentId: 0, deptName: null, orderNum: 0, leader: '', status: '0' };
  loadDeptTree();
  title.value = '新增部门';
  Open.value = true;
};

// 修改
const handleUpdate = (row) => {
  loadDeptTree();
  selectDeptById(row.deptId).then(res => {
    form.value = res.data;
    title.value = '修改部门';
    Open.value = true;
  });
};

// 保存
const submitForm = () => {
  deptRef.value.validate(valid => {
    if (!valid) return;
    if (form.value.deptId != null) {
      // 修改
      updateDept(form.value).then(() => {
        ElMessage.success('修改成功');
        Open.value = false;
        getList();
      });
    } else {
      // 新增
      insertDept(form.value).then(() => {
        ElMessage.success('新增成功');
        Open.value = false;
        getList();
      });
    }
  });
};

// 删除（有子部门时后端返回"存在子部门，不允许删除"，拦截器已统一提示，这里不再重复弹）
const handleDelete = (row) => {
  ElMessageBox.confirm('是否确认删除该部门', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteDeptById(row.deptId).then(() => {
      ElMessage.success('删除成功');
      getList();
    }).catch(() => {});
  }).catch(() => {});
};

onMounted(() => getList());
</script>
