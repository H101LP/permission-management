<template>
<div style="text-align: center;margin: 20px auto;max-width: 500px;">
  <div >

    <el-upload
        class="avatar-uploader"
        :action="uploadUrl"
        :headers="headers"
        :show-file-list="false"
        :on-success="handleAvatarSuccess"
        :on-error="handleAvatarError"
        :before-upload="beforeUpload"
    >
      <img v-if="userStore.avatar" :src="userStore.avatar" class="avatar" />
      <div v-else class="avatar-uploader-icon">点击上传头像</div>
    </el-upload>

  </div>
  <ul class="list-group" style="display: inline-block; text-align: left;width: 100%;max-width: 300px;">
    <li class="list-group-item">
      用户名称
    <div class="pull-right">{{userStore.name}}</div>
    </li>
    <li class="list-group-item">
      性别
      <div class="pull-right" v-if="state.user.sex=== 0 ">男</div>
      <div class="pull-right" v-else-if="state.user.sex=== 1 ">女</div>
    </li>
  </ul>
   <div style=" margin-top: 20px">
     <el-button type="primary" @click ="">修改基本资料</el-button>
     <el-button type="primary" @click ="">修改密码</el-button>

   </div>

</div>
</template>

<script setup>

import useUserStore from "@/stores/modules/userStore.js";
import {onMounted, reactive} from "vue";
import {getInfo} from "~/API/login.js";
import {getToken} from "@/utils/auth.js";
import {ElMessage} from "element-plus";

//头像上传接口地址
const uploadUrl = import.meta.env.VITE_APP_BASE_API + "/system/user/profile/avatar"
//请求头部
const headers = {
  Authorization:"Bearer " + getToken()
}

//用户状态
const userStore = useUserStore()
//数据状态
const  state = reactive({
  user:{}
})
//获取用户信息
const getUser = () =>{
  getInfo().then(res =>{
    state.user = res.data;
  })
}
//上传前处理
const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG 格式!');
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!');
  }
  return isJPG && isLt2M;
}
//上传成功处理
const handleAvatarSuccess = (res) => {
 if(res.code === 200){
   userStore.avatar = import.meta.env.VITE_APP_BASE_API + res.imgUrl
 }else {
   ElMessage.error(res.msg ||修改失败);
 }
}

//上传错误处理
const handleAvatarError = () => {
  ElMessage.error('上传头像图片失败!');
}


//组件挂载后执行
onMounted(()=>{
  getUser();
})

</script>


<style scoped>
.list-group{
  padding-left: 0;
  list-style: none;
}
.pull-right{
  float: right;
}
.list-group-item{
  margin-bottom: -1px;
  padding: 11px 0;
  font-size: 14px;
}

.avatar-uploader .avatar{
  width: 120px;
  height: 120px;
  display: block;
  border-radius: 50%;

}

</style>