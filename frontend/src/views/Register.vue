<template>
  <div class="register">
    <h2>注册</h2>

    <el-form :model="registerForm" @submit.prevent="handleRegister">
      <el-form-item label="用户名">
        <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input type="password" v-model="registerForm.password" placeholder="至少 6 位"></el-input>
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input type="password" v-model="registerForm.confirmPassword"></el-input>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="registerForm.email"></el-input>
      </el-form-item>
      <el-button type="primary" native-type="submit" :loading="loading">注册</el-button>
      <el-button @click="$router.push('/login')">返回登录</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()

const registerForm = ref({ username: '', password: '', confirmPassword: '', email: '' })
const loading = ref(false)

const handleRegister = async () => {
  // 前端校验只是为了体验，后端仍会独立校验一遍
  // （前端校验可以被绕过，永远不能作为安全依赖）
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (registerForm.value.password.length < 6) {
    ElMessage.error('密码长度不能少于 6 位')
    return
  }

  loading.value = true
  try {
    // 【安全修复】不再向前端传 role。
    // 原来这里硬编码 role: 2，但任何人都能用 Postman 改成 role: 1 把自己注册成管理员。
    // 现在角色由后端强制设为普通用户（见 UserService.register）。
    await request.post('/users/register', {
      username: registerForm.value.username,
      password: registerForm.value.password,
      email: registerForm.value.email
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    // 错误提示由 request.js 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register {
  width: 340px;
  margin: 60px auto;
  padding: 24px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
}

.register h2 {
  text-align: center;
  margin-bottom: 20px;
}

.register .el-button {
  width: 100%;
  margin: 6px 0 0 0;
}
</style>
