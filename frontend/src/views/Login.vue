<template>
  <div class="login">
    <h2>登录</h2>

    <el-form :model="loginForm" @submit.prevent="handleLogin">
      <el-form-item label="用户名">
        <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input type="password" v-model="loginForm.password" placeholder="请输入密码"></el-input>
      </el-form-item>
      <el-button type="primary" native-type="submit" :loading="loading">登录</el-button>
      <el-button @click="$router.push('/register')">去注册</el-button>
    </el-form>

    <div class="tips">
      <p>测试账号：admin / admin123（管理员）</p>
      <p>tom / 123456（普通用户）</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const loginForm = ref({ username: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  try {
    await authStore.login(loginForm.value)
    ElMessage.success('登录成功')
    // 登录后按角色跳转：管理员进管理页，普通用户进个人主页
    router.push(authStore.isAdmin ? '/admin' : '/profile')
  } catch (error) {
    // 错误提示已由 utils/request.js 的响应拦截器统一处理，
    // 这里不再弹一次，否则会出现两个错误提示。
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  width: 340px;
  margin: 80px auto;
  padding: 24px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
}

.login h2 {
  text-align: center;
  margin-bottom: 20px;
}

.login .el-button {
  width: 100%;
  margin: 6px 0 0 0;
}

.tips {
  margin-top: 16px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
</style>
