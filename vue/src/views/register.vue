<template>
  <div class="auth-page">
    <main class="auth-card">
      <div class="auth-header">
        <img class="auth-logo" src="@/assets/logo/logo.jpg" alt="系统标识" />
        <p class="auth-eyebrow">RBAC 权限管理系统</p>
        <h1 class="auth-title">注册</h1>
        <p class="auth-description">请输入您的注册信息</p>
      </div>

      <el-form :model="registerFrom" ref="registerRef" :rules="rules" class="auth-form">
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
      <el-form class="auth-actions">
        <el-form-item class="submit-item">
          <el-button
              class="submit-button"
              type="primary"
              :loading="loading"
              @click="handleRegister"
          >注册</el-button>
        </el-form-item>

        <div class="auth-switch">
          已有账户？
          <router-link to="/login" class="auth-link">立即登录</router-link>
        </div>
      </el-form>

      <footer class="auth-footer">注册后请使用新账户登录</footer>
    </main>
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