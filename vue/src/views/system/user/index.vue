<template>
  <div>
    <h1>这是用户管理</h1>
  </div>
</template>
<script setup>


import {onMounted, ref} from "vue";
import {selectUserList} from "~/API/system/user.js";

//查询参数
const query = ref({
  pageNum: 1,
  pageSize: 10,
  userName: '张三'
})

onMounted(() => {
  selectUserList(query.value).then(res=>{

  })
})
</script>


<style scoped>

</style>