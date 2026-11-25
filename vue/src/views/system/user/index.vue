<template>
  <div class="management-page">
    <header class="page-heading">
      <div class="heading-main">
        <div class="heading-icon">
          <el-icon><User /></el-icon>
        </div>
        <div>
          <h1>用户管理</h1>
          <p>维护用户账号、角色分配与启用状态</p>
        </div>
      </div>
      <div class="heading-stat">
        <span class="live-dot"></span>
        <span>用户总数</span>
        <strong>{{ total }}</strong>
        <span>个</span>
      </div>
    </header>

    <section class="query-panel">
      <el-form :model="query" ref="queryRef" label-width="80px" inline class="query-form">
        <el-form-item label="用户名称" prop="userName" class="field-lg">
          <el-input v-model="query.userName" placeholder="请输入用户名称" clearable/>
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
          <h2>用户列表</h2>
          <span class="record-count">共 {{ total }} 条记录</span>
        </div>
        <div class="table-actions">
          <el-button v-has-perm="['system:user:add']" type="primary" icon="Plus" @click="handleInsert" plain>新增</el-button>
          <el-button v-has-perm="['system:user:edit']" :disabled="single" type="success" icon="Edit" @click="handleUpdate" plain>修改</el-button>
          <el-button v-has-perm="['system:user:remove']" :disabled="multiple" type="danger" icon="Delete" @click="handleDelete" plain>删除</el-button>
        </div>
      </div>

      <el-table :data="userList" class="data-table" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="用户编号" width="180" align="center">
          <template #default="scope">
            <span class="cell-code">{{ scope.row.userId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户名" width="180" align="center">
          <template #default="scope">
            <span class="cell-main">{{ scope.row.userName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="性别" align="center">
          <template #default="scope">
            <span v-if="scope.row.sex === 0">男</span>
            <span v-else-if="scope.row.sex === 1">女</span>
            <span v-else>未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="头像" width="90" align="center">
          <template #default="scope">
            <el-avatar :size="34" :src="scope.row.avatar? baseUrl+scope.row.avatar : defaultAvatar" class="user-avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="roleName" label="对应角色" align="center" />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-switch
                v-model="scope.row.status"
                active-value="0"
                inactive-value="1"
                :before-change="() => handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <div class="row-actions">
              <el-button v-has-perm="['system:user:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
              <el-button v-has-perm="['system:user:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <span class="empty-icon"><el-icon><Search /></el-icon></span>
            <p>没有找到匹配的用户</p>
            <span>请调整查询条件后再试</span>
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

    <!--添加或修改用户对话框-->
    <vxe-modal class-name="form-modal" :title="title" v-model="Open" width="500px" showFooter show-maximize resize>
      <template #default>
        <el-form ref="userRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="用户名" prop="userName">
            <el-input v-model="form.userName" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="form.sex">
              <el-radio :value="0">男</el-radio>
              <el-radio :value="1">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" placeholder="请输入密码"></el-input>
          </el-form-item>
          <el-form-item label="角色" prop="roleId">
            <el-select v-model="form.roleId"  placeholder="请选择角色">
              <el-option
                  v-for="role in roleList"
                  :key="role.roleId"
                  :label="role.roleName"
                  :value="role.roleId"
              />
            </el-select>
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
import {onMounted, ref} from "vue";
import { changeUserStatus} from "~/API/system/user.js"
import {deleteUserByUserIds, insertUser, selectUserById, selectUserList, updateUser} from "~/API/system/user.js";
//引入默认头像
import defaultAvatar from "@/assets/images/profile.jpg";
import {VxeModal} from "vxe-pc-ui";
import {ElMessage, ElMessageBox} from "element-plus";
import {selectAllRole} from "~/API/system/role.js";

//拿后端路径
const baseUrl = import.meta.env.VITE_APP_BASE_API
//表单实例
const userRef = ref()

//对话框表单title
const title = ref('')
//对话框是否打开
const Open = ref(false)
//表单参数
const form = ref({
  userId: null,
  userName: null,
  sex: null,
  password: null,
  roleId: null
})
//表单校验
const rules = ref({
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
})
//新增按钮
const handleInsert = () => {
  form.value = {
    userId: null,
    userName: null,
    sex: null,
    password: null,
    roleId: null
  }
  Open.value = true;
  title.value = '新增用户'
}
//保存按钮
const submitForm = () => {
  userRef.value.validate(valid =>{
    if(valid){
      if(form.value.userId !=null){
        //修改
        updateUser(form.value).then(res=>{
          ElMessage.success('修改成功')
          Open.value = false;
          getList();
        })
      }else {
        //新增
        insertUser(form.value).then(res=>{
          ElMessage.success('新增成功')
          Open.value = false;
          getList();
        })
      }
    }
  })
  Open.value = false;
}





//查询参数
const query = ref({
  pageNum: 1,
  pageSize: 10,
  userName: null
})
//用户列表数据
const userList = ref([])
//已勾选的ID数组
const ids = ref([])
//当前是否  未选中单行
const single =ref(true)
//当前是否   未选中多行
const multiple = ref(true)
//顶部表单查询实例
const queryRef = ref()






//查询数据
const getList = () => {
  selectUserList(query.value).then(res=>{
   userList.value = res.data.rows; //赋值给userList
    total.value = res.data.total;
  })
}
//多选时的触发方法
const handleSelectionChange = (selection) => {
 ids.value = selection.map(item => item.userId)
 single.value = selection.length !=1;
 multiple.value = !selection.length;
}
//搜索按钮
const handleQuery = () => {
  query.value.pageNum = 1;
  getList();
}
//重置按钮
const resetQuery = () => {
  queryRef.value.resetFields();
  getList();
}
//修改按钮
const handleUpdate = (row) => {
  const userId = row.userId || ids.value
  selectUserById(userId).then(res=>{
    form.value = res.data;
    Open.value = true;
    title.value = '修改用户'

  })
}
//删除按钮
const handleDelete = (row) => {
  const userIds = row.userId || ids.value
  ElMessageBox.confirm(
      '是否确认删除用户',
      '系统提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
       //调用删除API
       deleteUserByUserIds(userIds).then(res =>{
         ElMessage.success('删除成功')
         getList();
       })
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: '已取消删除',
        })
      })

}


//数据总数
const total = ref(0); //数据总数


//角色列表数据
const roleList = ref([])
// 状态开关：先发请求，成功才让开关切换（失败保持原样）
const handleStatusChange = (row) => {
  const target = row.status === '0' ? '1' : '0'
  return new Promise((resolve) => {
    changeUserStatus({ userId: row.userId, status: target })
        .then(() => {
          ElMessage.success(target === '0' ? '启用成功' : '停用成功')
          resolve(true)          // 返回 true → 开关切换
        })
        .catch(() => {
          ElMessage.error('状态修改失败')
          resolve(false)         // 返回 false → 开关弹回原位
        })
  })
}




onMounted(() => {
  getList();
  //查询所有角色列表
  selectAllRole().then(res => {
    roleList.value = res.data
  })
})
</script>


<style scoped>
.user-avatar {
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 1px var(--border-default);
}
</style>