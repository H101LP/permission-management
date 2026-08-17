<template>
  <div class="container">
    <!-- 分页组件    -->
    <el-pagination :layout="layout"
                   :total="total"
                   v-model:current-page="currentPage"
                   v-model:page-size="pageSize"
                   :page-sizes="pageSizes"
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"


    />
  </div>
</template>
<script setup>
//定义组件接收的参数
const props = defineProps({
  total: {
    type: Number,
    required: true
  },
  pageSizes:{
    type: Array,
    default: () => [10,20,30,40,50]
  },
  layout:{
    type: String,
    default: 'total, prev, pager, next, jumper , sizes'
  }
})
//当前页码
const currentPage = defineModel('page',{default: 1})
//每页显示多少条
const pageSize = defineModel('limit',{default: 10})
//定义组件可以发出的事件
const emit = defineEmits(['pagination'])



//处理每页显示条数变化
const handleSizeChange = (val) => {
 if(currentPage.value * val > props.total){
   currentPage.value = 1
 }
 //向父组件发出事件
  emit('pagination',{page: currentPage.value, limit: val})
}
//页码变化时的函数
const handleCurrentChange = (val) => {
  //向父组件发出事件
  emit('pagination',{page: val, limit: pageSize.value})
}


</script>


<style scoped>
.container{
  padding: 30px 15px;
  display: flex;
  justify-content: flex-end;
}
</style>