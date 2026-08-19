<template>
  <div>



  </div>
</template>
<script setup>


import {onMounted, ref} from "vue";
import {selectMenuList} from "~/API/system/menu.js";

//菜单列表
const menuList = ref([]);
//查询参数
const query = ref({
  menuName: null

});




onMounted(()=>{
  selectMenuList(query.value).then(res=>{
    menuList.value = buildTree(res.data,0);
    console.log(menuList.value,'看看菜单列表')
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