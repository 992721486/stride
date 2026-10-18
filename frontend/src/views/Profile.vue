<template>
  <div class="profile">
    <h2>个人主页</h2>

    <el-descriptions :column="1" border>
      <el-descriptions-item label="ID">{{ user?.id }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user?.email || '未填写' }}</el-descriptions-item>
      <el-descriptions-item label="角色">
        <el-tag :type="user?.role === 1 ? 'danger' : 'info'">
          {{ user?.role === 1 ? '管理员' : '普通用户' }}
        </el-tag>
      </el-descriptions-item>
    </el-descriptions>

    <div class="actions">
      <el-button v-if="authStore.isAdmin" type="primary" @click="$router.push('/admin')">
        进入用户管理
      </el-button>
      <el-button type="danger" @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/authStore'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

/**
 * 【响应式修复】
 * 原来写的是 `const user = authStore.user`，这是"解构"。
 * Pinia 的 store 本身是响应式对象，但直接取出来的引用会失去响应性
 * —— user 变化时页面不会更新。
 * 正确做法是用 storeToRefs 保留响应性。
 */
const { user } = storeToRefs(authStore)

onMounted(async () => {
  // 进入页面时拉一次最新用户信息，同时验证 Token 是否仍然有效
  try {
    await authStore.fetchCurrentUser()
  } catch (e) {
    // 401 会被拦截器处理并跳转登录页
  }
})

const handleLogout = () => {
  authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.profile {
  width: 520px;
  margin: 60px auto;
  padding: 24px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
}

.profile h2 {
  text-align: center;
  margin-bottom: 20px;
}

.actions {
  margin-top: 20px;
  text-align: center;
}
</style>
