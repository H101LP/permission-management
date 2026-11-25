<template>
  <div class="auth-page">
    <main class="auth-card">
      <div class="auth-header">
        <img class="auth-logo" src="@/assets/logo/logo.jpg" alt="系统标识" />
        <p class="auth-eyebrow">RBAC 权限管理系统</p>
        <h1 class="auth-title">登录</h1>
        <p class="auth-description">请输入您的登录信息</p>
      </div>

      <el-form :model="loginForm" ref="loginRef" :rules="rules" class="auth-form">
        <el-form-item prop="userName">
          <el-input v-model="loginForm.userName" size="large" placeholder="请输入账号"/>
        </el-form-item>
        <el-form-item prop="password">
          <el-input show-password v-model="loginForm.password" size="large" placeholder="请输入密码"/>
        </el-form-item>
      </el-form>
      <el-form class="auth-actions">
        <el-form-item class="submit-item">
          <el-button
              class="submit-button"
              type="primary"
              :loading="loading"
              @click="handleLogin"
          >登录</el-button>
        </el-form-item>

        <div class="auth-switch">
          还没有账户？
          <router-link to="/register" class="auth-link">立即注册</router-link>
        </div>
      </el-form>

      <footer class="auth-footer">仅限授权用户访问</footer>
    </main>
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
.auth-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  min-height: 100svh;
  box-sizing: border-box;
  padding: 32px 16px;
  background: var(--bg-page);
}

.auth-page::before {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  height: 4px;
  background: var(--brand-500);
  content: "";
}

.auth-card {
  width: 100%;
  max-width: 420px;
  box-sizing: border-box;
  padding: 32px;
  border: 1px solid var(--border-default);
  border-radius: var(--radius-card);
  background: var(--bg-surface);
  box-shadow: var(--shadow-card);
}

.auth-header {
  margin-bottom: 24px;
  text-align: center;
}

.auth-logo {
  display: block;
  width: 44px;
  height: 44px;
  box-sizing: border-box;
  margin: 0 auto 16px;
  border: 1px solid var(--border-default);
  border-radius: 10px;
  object-fit: cover;
}

.auth-eyebrow {
  margin: 0 0 6px;
  color: var(--brand-600);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.auth-title {
  margin: 0;
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 650;
  line-height: 1.35;
}

.auth-description {
  margin: 8px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.auth-form :deep(.el-input__wrapper) {
  min-height: 42px;
  box-sizing: border-box;
  border-radius: var(--radius-control);
  background: var(--bg-surface);
  box-shadow: 0 0 0 1px var(--border-strong) inset;
  transition: box-shadow 0.18s ease;
}

.auth-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--brand-500) inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--brand-500) inset, 0 0 0 3px var(--brand-50);
}

.auth-form :deep(.el-input__inner) {
  color: var(--text-primary);
  font-size: 14px;
}

.auth-actions {
  margin-top: 22px;
}

.auth-actions :deep(.el-form-item) {
  width: 100%;
  margin: 0;
}

.submit-button {
  width: 100%;
  height: 42px;
  margin: 0;
  border-radius: var(--radius-control);
  font-size: 14px;
  font-weight: 600;
}

.auth-switch {
  margin-top: 18px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
  text-align: center;
}

.auth-link {
  margin-left: 4px;
  color: var(--brand-600);
  font-weight: 500;
  text-decoration: none;
}

.auth-link:hover {
  color: var(--brand-700);
  text-decoration: underline;
}

.auth-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-default);
  color: var(--text-tertiary);
  font-size: 11px;
  text-align: center;
}

@media (max-width: 480px) {
  .auth-page {
    padding: 24px 16px;
  }

  .auth-card {
    max-width: calc(100vw - 32px);
    padding: 24px 20px;
  }

  .auth-logo {
    width: 40px;
    height: 40px;
  }

  .auth-title {
    font-size: 22px;
  }
}
</style>