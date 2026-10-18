import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 统一的 axios 实例。
 *
 * 【为什么需要这个文件】
 * 原来的代码把 axios 用得很乱：
 *   - authStore.js 用 `axios.defaults.headers.common['Authorization']` 全局设 token
 *   - userStore.js 每个请求手动传 `headers: { Authorization: ... }`
 * 同一件事两种写法，而且全局默认头只存在内存里，刷新页面就丢了。
 *
 * 正确的做法是用 axios 的拦截器，在一处统一处理：
 *   - 请求拦截器：自动给每个请求加上 Authorization 头
 *   - 响应拦截器：统一拆解 Result 结构、统一提示错误、401 时跳登录页
 * 业务代码里就再也不用关心 token 和错误处理了。
 */
const request = axios.create({
  // 开发环境走 Vite 代理（vite.config.js 里 /api 代理到 8081），所以只需写相对路径
  baseURL: '/api',
  timeout: 10000
})

// ==================== 请求拦截器 ====================
request.interceptors.request.use(
  (config) => {
    // 每次请求前从 localStorage 读 token，这样刷新页面后也能带上
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ==================== 响应拦截器 ====================
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 后端统一返回 { code, msg, data }
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }

    // 直接把 data 层返回给调用方，业务代码里不用再写 res.data.data
    return res.data
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.msg

    if (status === 401) {
      // 未认证：清掉本地登录态并跳登录页
      ElMessage.error(msg || '登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // 用 hash 路由，直接改 location 最稳妥（这里不 import router，避免循环依赖）
      if (!location.hash.startsWith('#/login')) {
        location.hash = '#/login'
      }
    } else if (status === 403) {
      // 已认证但无权限
      ElMessage.error(msg || '无权限访问该资源')
    } else if (status === 404) {
      ElMessage.error(msg || '请求的资源不存在')
    } else if (status === 400 || status === 409) {
      ElMessage.error(msg || '请求参数有误')
    } else {
      ElMessage.error(msg || '网络异常，请稍后重试')
    }

    return Promise.reject(error)
  }
)

export default request
