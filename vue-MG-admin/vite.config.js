import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // 加载当前环境变量
  const env = loadEnv(mode, process.cwd());

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      proxy: {
        [env.VITE_APP_BASE_API]: { // 动态匹配前缀，例如 '/api'
          target: env.VITE_APP_URL, // 代理目标地址，例如 'http://localhost:8080/admin'
          secure: false,
          changeOrigin: true, // 开启跨域代理
          rewrite: (path) => path.replace(new RegExp('^' + env.VITE_APP_BASE_API), '') // 路径重写：移除 '/api' 前缀
        }
      }
    }
  }
})
