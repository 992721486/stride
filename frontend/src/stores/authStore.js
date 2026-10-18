import { defineStore } from 'pinia'
import request from '@/utils/request'

/**
 * 认证状态管理。
 *
 * 【本次修复】
 *   ① 不再用 axios.defaults 全局设 header，统一交给 utils/request.js 的拦截器；
 *   ② 登录只需一次请求（后端现在返回 { token, user }），
 *      原来登录后还要再发一次 GET /api/users/{username} 才能拿到用户信息；
 *   ③ role 一起存下来，路由守卫才能判断"是不是管理员"。
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    /** 1 = 管理员，2 = 普通用户 */
    isAdmin: (state) => state.user?.role === 1,
    username: (state) => state.user?.username || ''
  },

  actions: {
    async login(credentials) {
      // 后端返回 { token, user }
      const data = await request.post('/users/login', credentials)

      this.token = data.token
      this.user = data.user

      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(data.user))

      return data
    },

    /** 进入页面时用 Token 拉一次最新的用户信息（防止本地缓存的 user 过期） */
    async fetchCurrentUser() {
      const data = await request.get('/users/me')
      this.user = data
      localStorage.setItem('user', JSON.stringify(data))
      return data
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
