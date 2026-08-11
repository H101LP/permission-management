<template>
  <div class="login-container">
    <!-- 登录卡片 500px宽度，开启flex垂直排列 -->
    <div style="width: 500px;background: rgba(255,255,255,0.75);display: flex;flex-direction: column;padding:40px;">
      <!-- 标题区域 -->
      <div style="text-align: center;margin-bottom:30px;">
        <div style="text-align: center">
          <h3 style="font-size: 28px;font-weight: bold;color:#333">登录</h3>
          <p style="font-size: 14px;color:#444">请输入您的登录信息</p>
        </div>

        <el-form :model="loginForm" ref="loginRef" :rules="rules" >
          <el-form-item prop="userName">
            <el-input v-model="loginForm.userName" size="large" placeholder="请输入账号"/>
          </el-form-item>
          <el-form-item prop="password">
            <el-input show-password v-model="loginForm.password" size="large" placeholder="请输入密码"/>
          </el-form-item>
        </el-form>
        <el-form style="width: 100% ">
          <el-form-item style="text-align: center;width: 100%" >
            <el-button
                type="primary"
                style="width: 100%"
                :loading="loading"
                @click = "handleLogin"

            >登录</el-button>
          </el-form-item>


          <div>还没有账户？
          <router-link to="/register" style="color: #409EFF;margin-left: 5px;text-decoration: none">立即注册</router-link>
          </div>
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


    //表单实例
    const loginRef = ref()
    //表单参数
    const loginForm = ref({
      userName: null,
      password: null
    })
    //加载状态
    const loading = ref(false)
//用户状态管理
const userStore = useUserStore()
//拿到路由实例
const router = useRouter()




//登录方法
const handleLogin = () => {
loginRef.value.validate(valid =>{
  if(valid){
    //打开加载状态
   loading.value = true;
   //调用登录方法 TODO
  userStore.login(loginForm.value).then(res =>{
    //登录成功后让路由守卫处理跳转逻辑
    const redirectPath = '/';
    router.push(redirectPath);
  }).catch(()=>{
    loading.value = false;

  })

  }
})
}
//表单校验
const rules = ref({
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ]
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