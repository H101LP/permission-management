<template>
  <div class="login-container">
    <!-- 注册卡片 500px宽度，开启flex垂直排列 -->
    <div style="width: 500px;background: rgba(255,255,255,0.75);display: flex;flex-direction: column;padding:40px;">
      <!-- 标题区域 -->
      <div style="text-align: center;margin-bottom:30px;">
        <div style="text-align: center">
          <h3 style="font-size: 28px;font-weight: bold;color:#333">注册</h3>
          <p style="font-size: 14px;color:#444">请输入您的注册信息</p>
        </div>

        <el-form :model="registerFrom" ref="registerRef" :rules="rules" >
          <el-form-item prop="userName">
            <el-input v-model="registerFrom.userName" size="large" placeholder="请输入账号"/>
          </el-form-item>
          <el-form-item prop="password">
            <el-input show-password v-model="registerFrom.password" size="large" placeholder="请输入密码"/>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input show-password v-model="registerFrom.confirmPassword" size="large" placeholder="请确认密码"/>
          </el-form-item>
        </el-form>
        <el-form style="width: 100% ">
          <el-form-item style="text-align: center;width: 100%" >
            <el-button
                type="primary"
                style="width: 100%"
                :loading="loading"
                @click = "handleRegister"

            >注册</el-button>
          </el-form-item>



        </el-form>



      </div>
    </div>
  </div>
</template>


<script setup>
import { ref } from 'vue'
import {login} from "~/API/login.js";
import {useRouter} from "vue-router";
import useUserStore from "@/stores/modules/userStore.js";
import {register} from "~/API/register.js";
import {ElMessage} from "element-plus";
//路由实例
const router = useRouter()


//表单实例
const registerRef = ref()
//表单参数
const registerFrom = ref({
  userName: null,
  password: null,
  confirmPassword: null
})
//加载状态
const loading = ref(false)
//注册方法
const handleRegister = () => {
  registerRef.value.validate(valid =>{
    if(valid){
      //打开加载状态
      loading.value = true;
      //调用注册接口
      register(registerFrom.value).then(res => {
        ElMessage.success('恭喜您，你的账号'+registerFrom.value.userName+'注册成功！')
        router.push({ path: '/login' })
      }).catch(err => {
        loading.value = false;
        console.log(err)
      })


    }
  })
}
//验证是否相同
const equalToPassword = (rule, value, callback) => {
  if (value === registerFrom.value.password) {
    callback();
  } else {
    callback(new Error('两次输入的密码不一致'));
  }
};






//表单校验
const rules = ref({
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请输入确认密码', trigger: 'blur' },
    {required: true,validator:equalToPassword,trigger: 'blur'}
  ],
})

</script>


<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: url("@/assets/images/background.jpg") center center fixed;
  background-size: cover;
  position: relative;
}
</style>