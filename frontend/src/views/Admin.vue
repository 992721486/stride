<template>
  <div class="admin">
    <h2>用户管理（仅管理员可见）</h2>

    <el-table :data="userStore.users" v-loading="userStore.loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="email" label="邮箱"></el-table-column>
      <el-table-column label="角色" width="110">
        <template #default="scope">
          <el-tag :type="scope.row.role === 1 ? 'danger' : 'info'">
            {{ scope.row.role === 1 ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">改邮箱</el-button>
          <el-button size="small" type="danger" @click="confirmDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

/**
 * 【本次修复】
 * 原来写的是 `users.value = await userStore.fetchUsers()`，
 * 但 fetchUsers() 没有 return，所以拿到 undefined，表格永远是空的。
 * 现在 store 已改成显式 return，并且这里直接绑定 store 的响应式 state
 * （Pinia 的 state 本身是响应式的，不需要再拷到本地 ref）。
 */
onMounted(() => {
  userStore.fetchUsers()
})

const handleEdit = (row) => {
  ElMessageBox.prompt('请输入新的邮箱', `修改 ${row.username} 的邮箱`, {
    inputValue: row.email || '',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(async ({ value }) => {
      await userStore.updateUser(row.id, { email: value })
      ElMessage.success('修改成功')
    })
    .catch(() => {
      // 用户取消，无需处理
    })
}

const confirmDelete = (row) => {
  ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await userStore.deleteUser(row.id)
      ElMessage.success('删除成功')
    })
    .catch(() => {
      // 用户取消
    })
}
</script>

<style scoped>
.admin {
  max-width: 1000px;
  margin: 40px auto;
  padding: 0 20px;
}
</style>
