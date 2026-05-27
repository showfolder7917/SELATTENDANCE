// 引入 defineConfig，便于用标准 Vite 方式声明开发代理和插件配置。
import { defineConfig } from 'vite'
// 引入 Vue 官方插件，保证单文件组件能被 Vite 正常解析。
import vue from '@vitejs/plugin-vue'

// 开发配置里显式接管 /api 代理，让 SELVUE 双击启动后能直接联到 SELSP 而不触发跨域。
export default defineConfig({
  // 先注册 Vue 插件，保持现有页面编译链不变。
  plugins: [vue()],
  // 测试配置统一挂在 Vite 配置里，让前端构建和测试复用同一套模块解析链路。
  test: {
    // 前端单元与集成测试都运行在 jsdom，保证 window、document 和组件挂载能力可用。
    environment: 'jsdom',
    // 统一打开全局断言 API，减少每个测试文件重复导入 describe、it、expect。
    globals: true,
    // 让 Vue 单文件组件测试在每个用例后自动清理挂载痕迹，避免跨用例污染。
    clearMocks: true,
    // 把前端测试根目录固定到 tests，避免误扫 src 下业务文件。
    include: ['tests/**/*.test.js']
  },
  // 仅在本地开发服务中代理后端接口，请求保持同源体验，后端实际仍由 8090 提供。
  server: {
    // 所有 /api 请求都转发到 SELSP，避免前端在 5180 端口直接跨域访问 8090。
    proxy: {
      // 当前考勤第一阶段的所有接口都以 /api 开头，统一走这一条代理规则。
      '/api': {
        // 目标后端就是 SELATTENDANCE 本地服务。
        target: 'http://127.0.0.1:8090',
        // 允许代理层改写 origin，避免后端把请求视为来自错误来源。
        changeOrigin: true
      }
    }
  }
})
