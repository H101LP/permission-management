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
    <!--添加或修改用户对话框-->
    <vxe-modal :title="title" v-model="Open"  width="40%" showFooter show-maximize resize>
      <template #default>
        <el-form ref="menuRef" :model="form" :rules="rules" label-width="80px">
          <el-row >
            <el-col :span="12">
            <el-form-item label="上级菜单">
              <el-tree-select
                  v-model="form.parentId"
                  :data="menuOptions"
                  :props="{value: 'menuId', label: 'menuName', children: 'children'}"
                  check-strictly
                  value-key="menuId"
                  placeholder="请选择上级菜单" />
            </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="菜单类型" prop="menuType">
                  <el-radio-group v-model="form.menuType">
                    <el-radio value="M">目录</el-radio>
                    <el-radio value="C">菜单</el-radio>
                  </el-radio-group>
                </el-form-item>
            </el-col>
          </el-row>
<!--          -->
          <el-row >
            <el-col :span="12">
              <el-form-item label="菜单图标" prop="icon">
              <el-popover
                  placement="bottom-start"
                  width="550"
                  trigger="click"
              >
                <template #reference>
                 <el-input v-model="form.icon" placeholder="点击选择图标" @blur="">
                   <template #prefix>
                    <svg-icon v-if="form.icon" :icon-class="form.icon" style="height: 32px;width: 16px" />
                     <el-icon v-else>
                       <Search/>
                     </el-icon>
                   </template>
                 </el-input>
                </template>
                <icon-select ref="iconSelectRef" @selected="selectedIcon"/>
              </el-popover>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="显示排序" prop="menuSort">
                <el-input-number v-model="form.menuSort" min="0" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row >
            <el-col :span="12">
              <el-form-item label="菜单名称" prop="menuName">
                <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="form.menuType === 'C'">
              <el-form-item label="路由地址" prop="path">
                <el-input v-model="form.path" placeholder="请输入路由地址" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row  >
            <el-col :span="12" v-if="form.menuType === 'C'">
              <el-form-item label="组件路径" prop="component">
                <el-input v-model="form.component" placeholder="请输入组件路径" />
              </el-form-item>
            </el-col>
          </el-row>

        </el-form>
      </template>
      <template #footer>
        <div>
          <el-button type="primary" @click="">保存</el-button>
          <el-button @click="Open = false">取消</el-button>
        </div>
      </template>
    </vxe-modal>


  </div>
</template>
<script setup>


import {onMounted, ref} from "vue";
import {selectMenuList} from "~/API/system/menu.js";
import SvgIcon from "@/components/SvgIcon/index.vue";
import {VxeModal} from "vxe-pc-ui";
import {ElMessage} from "element-plus";
import {ElTreeSelect} from "element-plus";
import {Search} from "@element-plus/icons-vue";
import IconSelect from "@/components/IconSelect/index.vue";


//图标选择组件实例
const iconSelectRef = ref();
//选择图标回调事件
const selectedIcon = (name) => {
  form.value.icon = name;
};







//title
const title = ref('');
//对话框
const Open = ref(false);
//表单实例
const menuRef = ref();
//表单数据
const form = ref({
  menuId:null,
  parentId:null,
  menuName: null,
  icon: null,
  menuType: 'M',
  menuSort: null,
  path: null,
  component: null,
});
//表单校验
const rules = ref({
  menuName: [
    { required: true, message: '请输入菜单名', trigger: 'blur' }
  ],
  menuSort: [
    { required: true, message: '请输入菜单顺序', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '请输入路由地址', trigger: 'blur' }
  ],
  component: [
    { required: true, message: '请输入组件路径', trigger: 'blur' }
  ]
})
//新增按钮
const handleInsert = () => {
  form.value = {
    menuId:null,
    parentId:0,
    menuName: null,
    icon: null,
    menuType: 'M',
    menuSort: null,
    path: null,
    component: null,
  };
  getTreeSelect()
  Open.value = true;
  title.value = '新增菜单';
};

//菜单下拉树数据
const menuOptions = ref([]);

//查询菜单下拉树结构
const getTreeSelect = () => {
  selectMenuList().then(
      res => {
        const menu = {menuId : 0,menuName:'主层级',children:[]}
        menu.children = buildTree(res.data,0)
        menuOptions.value.push(menu)
      }
  )

};
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