import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { fileURLToPath, URL } from 'node:url' // 新增这行

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 配置 @ 代表 src 目录
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081', // 确保是你后端的端口
        changeOrigin: true
      }
    }
  }
})