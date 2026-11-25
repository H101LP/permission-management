<template>
<div class="management-page profile-page">
  <header class="page-heading">
    <div class="heading-main">
      <span class="heading-icon"><el-icon><User /></el-icon></span>
      <div>
        <h1>个人中心</h1>
        <p>维护头像、基本资料与登录密码</p>
      </div>
    </div>
  </header>

  <section class="profile-card">
    <div class="profile-header">
      <div class="avatar-section">
        <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="headers"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :on-error="handleAvatarError"
            :before-upload="beforeUpload"
        >
          <img v-if="userStore.avatar" :src="userStore.avatar" class="avatar" alt="用户头像" />
          <div v-else class="avatar-uploader-icon">点击上传头像</div>
        </el-upload>
        <div class="avatar-caption">
          <h2>个人头像</h2>
          <p>支持 JPG 格式，文件大小不超过 2MB</p>
        </div>
      </div>
    </div>

    <div class="profile-info">
      <h2>基本信息</h2>
      <dl class="profile-list">
        <div class="profile-row">
          <dt>用户名称</dt>
          <dd class="profile-value">{{ userStore.name }}</dd>
        </div>
        <div class="profile-row">
          <dt>性别</dt>
          <dd class="profile-value">
            <span v-if="state.user.sex === 0">男</span>
            <span v-else-if="state.user.sex === 1">女</span>
          </dd>
        </div>
      </dl>
    </div>

    <div class="profile-actions">
      <el-button type="primary" icon="Edit" @click="editUserInfo">修改基本资料</el-button>
      <el-button type="primary" icon="Lock" @click="editPassword">修改密码</el-button>
    </div>
  </section>

  <!--基本资料修改对话框-->
  <vxe-modal class-name="form-modal" title="修改基本资料" v-model="userInfoOpen" width="500px" showFooter show-maximize resize>
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
      </el-form>
    </template>
    <template #footer>
      <div>
        <el-button type="primary" @click="submitUserInfo">保存</el-button>
        <el-button @click="userInfoOpen = false">取消</el-button>
      </div>
    </template>
  </vxe-modal>

  <!--基本资料密码对话框-->
  <vxe-modal class-name="form-modal" title="修改密码" v-model="pwdOpen" width="500px" showFooter show-maximize resize>
    <template #default>
      <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" placeholder="请输入旧密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" placeholder="请输入确认密码"></el-input>
        </el-form-item>
      </el-form>
    </template>
    <template #footer>
      <div>
        <el-button type="primary" @click="submitPwd">保存</el-button>
        <el-button @click="pwdOpen = false">取消</el-button>
      </div>
    </template>
  </vxe-modal>
</div>
</template>

<script setup>
import {VxeModal} from "vxe-pc-ui"
import useUserStore from "@/stores/modules/userStore.js";
import {ref, watch} from "vue";
import {onMounted, reactive} from "vue";
import {getInfo} from "~/API/login.js";
import {getToken} from "@/utils/auth.js";
import {ElMessage} from "element-plus";
import {updateProfile, updatePwd} from "~/API/system/user.js";
//修改密码按钮
const editPassword = () => {
  pwdOpen.value = true;
}
//保存密码
const submitPwd = () => {
  if(pwdForm.value.newPassword !== pwdForm.value.confirmPassword){
    ElMessage.error('新密码和确认密码不一致');
    return
  }
  pwdRef.value.validate(valid => {
  if(valid){
    updatePwd(pwdForm.value).then(res=>{
      ElMessage.success('修改成功');
      pwdOpen.value = false;
    })

  }
  })
}

//修改密码对话框是否打开
const pwdOpen = ref(false);
//修改密码表单实例
const pwdRef = ref();
//密码表单参数
const pwdForm = ref({
})
//密码表单校验
const pwdRules = ref({
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请输入确认密码', trigger: 'blur' },
  ],
})





//修改基本资料对话框是否打开
const userInfoOpen = ref(false);
//修改基本资料表单实例
const userRef = ref(null);
//用户资料表单参数
const form = ref({
})
//表单校验
const rules = ref({
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
})
//提交用户资料
const submitUserInfo = () => {
  userRef.value.validate(valid =>{
    if(valid){
      updateProfile(form.value).then(res=>{
        userInfoOpen.value = false;
        ElMessage.success('修改成功');
        getUser();
        userStore.name = form.value.userName;
      })

    }
  })
}



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

//修改基本资料按钮
const editUserInfo = () => {
  userInfoOpen.value = true;
}





//获取用户信息
const getUser = () =>{
  getInfo().then(res =>{
    // /getInfo 返回的是 { user: {...}, permissions: [...] }
    // 之前直接取 res.data，导致 userName/sex 取到 undefined（性别空白、弹窗用户名为空）
    state.user = res.data.user || {};
    //初始化表单
    form.value={
      userName: state.user.userName,
      sex: state.user.sex
    }
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
   userStore.avatar = import.meta.env.VITE_APP_BASE_API + res.data.imgUrl
 }else {
   ElMessage.error(res.msg || '修改失败');
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
//监听用户信息变化时
watch(()=>state.user, //要监听的数据源
    user=>{ //回调函数，接收监听的数据
      if(user){
        form.value = {userName:user.userName, sex:user.sex}
      }
    },
    {immediate: true}
)
</script>


<style scoped>
.profile-card {
  max-width: 640px;
  margin: 0 auto;
  overflow: hidden;
  border: 1px solid var(--border-default);
  border-radius: 12px;
  background: var(--bg-surface);
  box-shadow: var(--shadow-card);
}

.profile-header {
  padding: 32px 24px 28px;
  border-bottom: 1px solid var(--border-default);
  background: linear-gradient(180deg, var(--bg-subtle), var(--bg-surface));
  text-align: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.avatar-uploader {
  display: grid;
  place-items: center;
  width: 112px;
  height: 112px;
  overflow: hidden;
  border: 3px solid var(--bg-surface);
  border-radius: 50%;
  background: var(--brand-50);
  box-shadow: 0 0 0 1px var(--border-default), 0 8px 20px rgba(23, 32, 51, 0.08);
  cursor: pointer;
  transition: box-shadow 0.18s ease, transform 0.18s ease;
}

.avatar-uploader:hover {
  box-shadow: 0 0 0 1px var(--brand-500), 0 10px 24px rgba(49, 87, 213, 0.14);
  transform: translateY(-1px);
}

.avatar-uploader .avatar {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-uploader-icon {
  color: var(--brand-500);
  font-size: 13px;
  font-weight: 500;
}

.avatar-caption h2 {
  margin: 0;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
}

.avatar-caption p {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.5;
}

.profile-info {
  padding: 26px 32px 10px;
}

.profile-info h2 {
  margin: 0 0 10px;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 600;
}

.profile-list {
  margin: 0;
}

.profile-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 54px;
  border-bottom: 1px solid var(--border-default);
}

.profile-row:last-child {
  border-bottom: 0;
}

.profile-row dt {
  flex: 0 0 auto;
  color: var(--text-secondary);
  font-size: 13px;
}

.profile-value {
  min-width: 0;
  margin: 0;
  overflow-wrap: anywhere;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
  text-align: right;
}

.profile-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 22px 32px 30px;
}

.profile-actions :deep(.el-button) {
  min-width: 132px;
  min-height: 36px;
  border-radius: 7px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .profile-card {
    border-radius: 10px;
  }

  .profile-header {
    padding: 26px 18px 24px;
  }

  .avatar-uploader {
    width: 100px;
    height: 100px;
  }

  .profile-info {
    padding: 22px 20px 8px;
  }

  .profile-actions {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .profile-actions {
    flex-direction: column;
  }

  .profile-actions :deep(.el-button) {
    width: 100%;
  }
}
</style>