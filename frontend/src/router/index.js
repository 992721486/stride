import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

// 路由懒加载：用 () => import(...) 的形式，
// 打包时每个页面会被切成独立 chunk，首屏只加载当前页面需要的代码。
const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { public: true } },
  { path: '/profile', name: 'Profile', component: () => import('@/views/Profile.vue') },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/Admin.vue'),
    // meta 是路由的自定义元信息，守卫里读取它来决定是否放行
    meta: { requiresAdmin: true }
  },
  // 兜底：访问不存在的地址时回首页
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

/**
 * 全局前置守卫（router.beforeEach）。
 *
 * 【为什么必须有】
 * 原来完全没有守卫，在地址栏直接敲 #/admin 就能进管理页 —— 前端没有任何拦截。
 * 但要注意：前端守卫只是"体验层"保护（让用户看不到不该看的页面），
 * 真正的安全必须由后端做（拦截器校验 role，见 JwtRequestFilter）。
 * 两者都要有：前端防误入，后端防恶意调用。
 */
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 公开页面（登录、注册）
  if (to.meta.public) {
    // 已登录用户再访问登录页，直接送去个人主页
    if (authStore.isLoggedIn) {
      return next('/profile')
    }
    return next()
  }

  // 未登录 → 跳登录页
  if (!authStore.isLoggedIn) {
    return next({ path: '/login' })
  }

  // 需要管理员权限但不是管理员 → 回个人主页
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return next('/profile')
  }

  next()
})

export default router
