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
      <el-button type="primary" icon="Plus" @click="" plain>新增</el-button>
      <el-button :disabled="single" type="success" icon="Edit" @click="" plain>修改</el-button>
      <el-button :disabled="multiple" type="danger" icon="Delete" @click="" plain>删除</el-button>

    </el-row>
    <!--列表    -->
    <el-table :data="roleList" style="width: 100%" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="roleId" label="角色编号" :width="200" align="center" />
      <el-table-column prop="roleName" label="角色名" min-width="180" align="center" />
      <el-table-column label="操作" align="center" :width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页-->
    <pagination :total="total"
                v-model:page="query.pageNum"
                v-model:limit="query.pageSize"
                @pagination="getList"/>
  </div>
</template>


<script setup>


import {onMounted, ref} from "vue";
import {selectUserList} from "~/API/system/user.js";
import {selectRoleById, selectRoleList} from "~/API/system/role.js";
import defaultAvatar from "@/assets/images/profile.jpg";
import Pagination from "@/components/Pagination/index.vue";
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

onMounted(() => {
  getList();

})
</script>


<style scoped>

</style>