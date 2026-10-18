import { defineStore } from 'pinia'
import request from '@/utils/request'

/**
 * 用户数据管理。
 *
 * 【本次修复的核心 bug】
 * 原来 fetchUsers() 内部只写了 `this.users = response.data` 就结束了，
 * 但 Admin.vue 里写的是 `users.value = await userStore.fetchUsers()`。
 * 因为 action 没有 return，await 拿到的是 undefined，
 * 所以管理页表格永远是空的 —— 典型的"看起来对了但没返回值"的 bug。
 *
 * 现在改成显式 return，调用方两种写法都能用。
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    users: [],
    loading: false
  }),

  actions: {
    /** 查询全部用户（仅管理员可调用，后端会校验角色） */
    async fetchUsers() {
      this.loading = true
      try {
        const list = await request.get('/users')
        this.users = list || []
        return this.users          // ← 关键修复：把数据返回给调用方
      } finally {
        this.loading = false
      }
    },

    /** 修改用户信息（仅管理员） */
    async updateUser(id, payload) {
      const updated = await request.put(`/users/${id}`, payload)
      // 同步更新本地列表，避免整页重新加载
      const index = this.users.findIndex((u) => u.id === id)
      if (index !== -1) {
        this.users[index] = updated
      }
      return updated
    },

    /** 删除用户（仅管理员） */
    async deleteUser(id) {
      await request.delete(`/users/${id}`)
      this.users = this.users.filter((u) => u.id !== id)
    }
  }
})
