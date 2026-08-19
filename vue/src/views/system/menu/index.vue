<template class="app-container">
  <div>
    <!--顶部查询区域    -->
    <el-form :model="query" ref="queryRef" label-width="80px" inline>
      <el-form-item label="菜单名称" prop="menuName">
        <el-input v-model="query.menuName" placeholder="请输入菜单名称"/>
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
    </el-row>
    <!--列表    -->
    <el-table :data="menuList" style="width: 100%" border row-key="menuId"
              :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
      <el-table-column prop="menuName" label="菜单名称" min-width="100" align="center" />
      <el-table-column prop="icon" label="图标" align="center" >
        <template #default="scope">
          <svg-icon :icon-class="scope.row.icon" />
        </template>
      </el-table-column>
      <el-table-column prop="menuSort" label="排序" align="center" width="60" />
      <el-table-column prop="component" label="组件路径" align="center" width="160" />
      <el-table-column label="操作" align="center" :width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="">删除</el-button>
        </template>
      </el-table-column>
    </el-table>


  </div>
</template>
<script setup>


import {onMounted, ref} from "vue";
import {selectMenuList} from "~/API/system/menu.js";
import SvgIcon from "@/components/SvgIcon/index.vue";


//顶部查询表单实例
const queryRef = ref();
//搜索按钮
const handleQuery = () => {
  getList();
}
//重置按钮
const resetQuery = () => {
  queryRef.value.resetFields();
  getList();
}
//查询数据
const getList = () => {
  selectMenuList(query.value).then(res=>{
    if (query.value.menuName != null){
      //如果有查询参数 根据菜单名称查询
      menuList.value = res.data;
    }else {
      //如果没有查询参数 显示所有菜单
      menuList.value = buildTree(res.data,0)
    }
  })
}


//菜单列表
const menuList = ref([]);
//查询参数
const query = ref({
  menuName: null

});




onMounted(()=>{
  selectMenuList(query.value).then(res=>{
    menuList.value = buildTree(res.data,0);
  }
  )
})
//构建菜单树
const buildTree = (data, parentId) => {
 //存放当前层级的所有菜单项
  const result = [];
  for (const item of data){
    if(item.parentId === parentId){
      //如果当前菜单项的父级ID等于传入的父级ID 说明它是当前层级的菜单项
      //递归查找
      const children = buildTree(data, item.menuId);
      //如果找到了子菜单
      if (children.length > 0){
        item.children = children;
      }
      result.push(item);
    }
  }
  //返回当前层级的所有菜单项
  return result;
};



</script>


<style scoped>

</style>