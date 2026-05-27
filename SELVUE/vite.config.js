// 引入 fileURLToPath，供 alias 把 import.meta URL 转成 Vitest/Vite 都可识别的本机绝对路径。
import { fileURLToPath } from 'node:url'
// 引入 defineConfig，便于用标准 Vite 方式声明开发代理和插件配置。
import { defineConfig } from 'vite'
// 引入 Vue 官方插件，保证单文件组件能被 Vite 正常解析。
import vue from '@vitejs/plugin-vue'

// 开发配置里显式接管 /api 代理，让 SELVUE 双击启动后能直接联到 SELSP 而不触发跨域。
export default defineConfig({
  // 先注册 Vue 插件，保持现有页面编译链不变。
  plugins: [vue()],
  // 为源码主入口和测试侧公开入口定义稳定 alias，让测试依赖停留在 tests 层而不是混进业务源码目录。
  resolve: {
    alias: {
      // 通用源码 alias 供测试和业务代码共享，避免反复书写长相对路径。
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      // 考勤测试侧入口只暴露允许测试依赖的公开面，后续 src 内部目录调整时只需维护 tests 侧出口。
      '@tests-attendance': fileURLToPath(new URL('./tests/attendance/index.js', import.meta.url)),
      // host 测试侧入口统一收口宿主壳和工程注册表的公开测试面。
      '@tests-host': fileURLToPath(new URL('./tests/host/index.js', import.meta.url)),
      // memory 测试侧入口统一收口最小插件工程的公开测试面。
      '@tests-memory': fileURLToPath(new URL('./tests/memory/index.js', import.meta.url)),
      // 共享基础服务 alias 供 API 封装测试 mock request 桥接时直接引用统一入口。
      '@shared': fileURLToPath(new URL('./src/shared', import.meta.url))
    }
  },
  // 测试配置统一挂在 Vite 配置里，让前端构建和测试复用同一套模块解析链路。
  test: {
    // 前端单元与集成测试都运行在 jsdom，保证 window、document 和组件挂载能力可用。
    environment: 'jsdom',
    // 统一打开全局断言 API，减少每个测试文件重复导入 describe、it、expect。
    globals: true,
    // 让 Vue 单文件组件测试在每个用例后自动清理挂载痕迹，避免跨用例污染。
    clearMocks: true,
    // 把前端测试根目录固定到 tests，避免误扫 src 下业务文件。
    include: ['tests/**/*.test.js'],
    // 真实浏览器 e2e 统一交给 Playwright 执行，避免被 Vitest 误扫后把浏览器用例当成 jsdom 测试。
    exclude: ['tests/**/*.e2e.test.js'],
    // 覆盖率统计只聚焦当前已经纳入自动化验证的宿主、memory 和 attendance 核心状态/服务边界。
    coverage: {
      // 使用 V8 原生覆盖率，兼容当前 Vite + Vue 编译链而不再引入额外 babel 管道。
      provider: 'v8',
      // 输出终端摘要、JSON 汇总和 HTML 报告，便于收口时同时留文字结论和可浏览证据。
      reporter: ['text', 'json-summary', 'html'],
      // 把报告统一落在 coverage 目录，后续执行文档和记账都可以直接引用固定路径。
      reportsDirectory: './coverage',
      // 当前统计范围聚焦已经按模块分层补齐测试的核心工程入口、宿主入口、状态层和服务层。
      include: [
        'src/App.vue',
        'src/projects/index.js',
        'src/projects/memory/index.js',
        'src/projects/memory/views/MemoryWorkbenchView.vue',
        'src/projects/attendance/index.js',
        'src/projects/attendance/composables/useAttendanceWorkbench.js',
        'src/projects/attendance/composables/workbench/state.js',
        'src/projects/attendance/composables/workbench/helpers.js',
        'src/projects/attendance/services/*.js'
      ],
      // 用 80% 作为当前自动化覆盖门槛，保证这轮重排后的核心边界不是只靠个别冒烟用例撑住。
      thresholds: {
        lines: 80,
        functions: 80,
        statements: 80,
        branches: 80
      }
    }
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
