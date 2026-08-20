<template>
  <div class="app-container">
    <!--顶部查询区域    -->
    <el-form :model="query" ref="queryRef" label-width="80px" inline>
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="query.roleName" placeholder="请输入角色名称"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button  icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <!--顶部按钮    -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"/>
      <el-button type="primary" icon="Plus" @click="handleInsert" plain>新增</el-button>
      <el-button :disabled="single" type="success" icon="Edit" @click="handleUpdate" plain>修改</el-button>
      <el-button :disabled="multiple" type="danger" icon="Delete" @click="handleDelete" plain>删除</el-button>

    </el-row>
    <!--列表    -->
    <el-table :data="roleList" style="width: 100%" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="roleId" label="角色编号" :width="200" align="center" />
      <el-table-column prop="roleName" label="角色名" min-width="180" align="center" />
      <el-table-column label="操作" align="center" :width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页-->
    <pagination :total="total"
                v-model:page="query.pageNum"
                v-model:limit="query.pageSize"
                @pagination="getList"/>
    <!--添加或修改角色对话框-->
    <vxe-modal :title="title" v-model="Open"  width="500px" showFooter show-maximize resize>
      <template #default>
        <el-form ref="roleRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="角色名" prop="roleName">
            <el-input v-model="form.roleName" placeholder="请输入角色名"></el-input>
          </el-form-item>
          <el-form-item label="角色顺序" prop="roleSort">
            <el-input v-model="form.roleSort" placeholder="请输入角色顺序"></el-input>
          </el-form-item>
          <el-form-item label="菜单权限" >
            <el-tree
                style="width:100%"
                :data="menuOptions"
                show-checkbox
                default-expand-all
                ref="menuRef"
                node-key="id"
                :props="{ label: 'label', children: 'children' }"
            />
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


import {nextTick, onMounted, ref} from "vue";
import {selectUserList} from "~/API/system/user.js";
import {deleteRoleByRoleIds, insertRole, selectRoleById, selectRoleList, updateRole} from "~/API/system/role.js";
import defaultAvatar from "@/assets/images/profile.jpg";
import Pagination from "@/components/Pagination/index.vue";
import {VxeModal} from "vxe-pc-ui";
import {ElMessage, ElMessageBox} from "element-plus";
import {selectRoleMenuTree} from "~/API/system/menu.js";


//菜单权限表单实例
const menuRef = ref()


//定义数据
const query = ref({
  pageNum: 1,
  pageSize: 10,
  roleName: null
})

//角色列表数据
const roleList = ref([])
//数据总数
const total = ref(0)
//顶部表单实例
const queryRef = ref()

//表单实例
const roleRef = ref()

//对话框表单title
const title = ref('')
//对话框是否打开
const Open = ref(false)
//表单参数
const form = ref({
  roleId: null,
  roleName: null,
  roleSort: null,
  menuIds: []
})
//表单校验
const rules = ref({
  roleName: [
    { required: true, message: '请输入角色名', trigger: 'blur' }
  ],
  roleSort: [
    { required: true, message: '请输入角色排序', trigger: 'blur' }
  ]
})
//新增按钮
const handleInsert = () => {
  form.value = {
    roleId: null,
    roleName: null,
    roleSort: null,
    menuIds: []
  }
  Open.value = true;
  title.value = '新增角色'
}
//修改按钮
const handleUpdate = (row) => {
  if(menuRef.value){
    menuRef.value.setCheckedKeys([])
  }
  const roleId = row.roleId || ids.value
  form.value = {
    roleId: null,
    roleName: null,
    roleSort: null,
    menuIds: []
  }
  //根据角色ID查询对应的菜单树
  const roleMenu = getRoleMenuTreeSelect(roleId);
  selectRoleById(roleId).then(res=>{
    form.value = res.data;
    Open.value = true;
    title.value = '修改角色'
    //等待DOM更新完成后执行菜单选中操作
    nextTick(() => {
      //等待菜单树渲染完成
      roleMenu.then((res) =>{
        //获取已选中的菜单ID
        let checkedKeys = res.checkedKeys
        //遍历已选中的菜单ID
        checkedKeys.forEach((key) => {
          //再次使用nextTick等待菜单树更新
          nextTick(() => {
            menuRef.value.setChecked(key, true, false)
          })
        })
      })
    })
  })
}
//菜单树 数据
const menuOptions = ref([])


//根据角色ID查询对应的菜单树
const getRoleMenuTreeSelect = (roleId) => {
   return selectRoleMenuTree(roleId).then(res => {
    // 重置菜单选项前先清空数组
     menuOptions.value = []
     // 将查询到的菜单数据赋值给菜单选项
     menuOptions.value = res.menus
     return res;
  })
}




//删除按钮
const handleDelete = (row) => {
  const roleIds = row.roleId || ids.value
  ElMessageBox.confirm(
      '是否确认删除角色',
      '系统提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        //调用删除API
        deleteRoleByRoleIds(roleIds).then(res=>{
          ElMessage.success('删除成功')
          getList()
        })
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: '已取消删除',
        })
      })

}



//保存按钮
const submitForm = () => {
  roleRef.value.validate((valid) => {
    if (valid) {
      if (form.value.roleId != null) {
        form.value.menuIds = getMenuAllCheckedKeys()
        // 修改角色
        updateRole(form.value).then((res) => {
          ElMessage.success('修改成功')
          Open.value = false
          getList()
        })
      } else {
        // 新增角色
        insertRole(form.value).then((res) => {
          ElMessage.success('新增成功')
          Open.value = false
          getList()
        })
      }
    }
  })
}
//获取菜单组件中所有被选中的节点
const getMenuAllCheckedKeys = () => {
  //获取所有被选中的节点
  let checkedKeys = menuRef.value.getCheckedKeys()
  //获取所有半选中的节点
  let halfCheckedKeys = menuRef.value.getHalfCheckedKeys()
  //拼接成最终的菜单ID数组
  return [...halfCheckedKeys, ...checkedKeys]
}






//查询数据
const getList = () => {
  selectRoleList(query.value).then(res=>{
    roleList.value = res.rows; //赋值给roleList
    total.value = res.total;
  })
}
//已勾选的ID数组
const ids = ref([])
//当前是否  未选中单行
const single =ref(true)
//当前是否   未选中多行
const multiple = ref(true)

//多选时的触发方法
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.roleId)
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

onMounted(() => {
  getList();

})
</script>


<style scoped>

</style>