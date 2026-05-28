# SELVUE Vue 测试规范

## 0. 当前实现现状与规范目标

### 0.1 当前实现现状
- `SELVUE` 当前测试根目录已经统一为 `test/`，不再使用 `tests/`。
- `SELVUE` 当前按工程分为：
  - `test/attendance`
  - `test/memory`
  - `test/host`
  - `test/diagnostics`
- `attendance / host / memory` 三组测试继续按 `unit / integration / e2e` 分层。
- `attendance` 的 `unit / integration` 当前继续按：
  - `bootstrap`
  - `department`
  - `employee`
  - `schedule`
  - `shift`
  - `tenant`
  - `workplace`
  细分。
- `SELVUE` 当前测试适配入口已经统一为：
  - `test/attendance/index.js`
  - `test/host/index.js`
  - `test/memory/index.js`
- `SELVUE` 当前已经接入：
  - `Vitest + jsdom`
  - `Playwright`
  - `coverage`
- `SELVUE` 当前正式 `e2e` 已改为“项目自管服务链路”：
  - 项目脚本自己启动本地 `Vite`
  - 探活 `http://127.0.0.1:5180`
  - 运行 `Playwright`
  - 显式关闭本地服务
- `Playwright` 默认正式套件当前只跑业务 `e2e`，不混跑诊断用例。

### 0.2 规范目标
- 统一 `SELVUE` 前端测试的目录边界、执行方式、命名方式和适配入口。
- 保证测试目录与业务源码目录解耦，不把测试实现塞回 `src`。
- 保证 `Vitest`、`Playwright`、诊断用例三类职责边界明确。
- 保证 `Windows` 环境下 `npm run test:e2e` 能稳定执行并正常退出。

## 1. 测试分层原则

### 1.1 unit 层
- 目标：保护单模块本地状态、默认值、筛选逻辑和轻量辅助行为。
- 关注内容：
  - 状态工厂默认值
  - 表单初始值
  - 筛选字段修复
  - 本地计数同步
  - 轻量导出桥接
  - toast、URL、默认引用等本地行为
- `unit` 不负责证明整条浏览器链路。

### 1.2 integration 层
- 目标：保护模块之间、组件与 composable 之间、服务与请求封装之间的接线契约。
- 关注内容：
  - `services/*.js` 请求路径
  - HTTP 方法
  - 查询参数
  - 提交体
  - 宿主壳和模块注册表协同
  - 组件视图与 composable 组合边界
- `integration` 是接线层测试，不等于浏览器链路测试。

### 1.3 e2e 层
- 目标：保护真实浏览器中的关键业务主链。
- 关注内容：
  - 页面加载
  - 模块切换
  - 表单交互
  - 批量动作
  - 导出动作
  - 浏览器级确认与路由行为
- `e2e` 必须使用 `Playwright`，不能用 `jsdom` 冒充。

### 1.4 regression 规则
- 目标：保护历史 bug 和高风险回归点。
- 当前不单独建立 `regression/` 总目录。
- 回归用例通过独立文件表达，例如：
  - `employee-query-regression.test.js`
  - `schedule-query-regression.test.js`
  - `department-filters-regression.test.js`
  - `employee-table-layout-regression.e2e.test.js`

### 1.5 adapter 层
- 目标：统一测试对模块公开面的依赖边界。
- 测试适配入口统一放在：
  - `test/attendance/index.js`
  - `test/host/index.js`
  - `test/memory/index.js`
- 测试应优先依赖 adapter 和稳定 alias，不直接穿透长相对路径访问 `src` 深层目录。

### 1.6 diagnostics 层
- 目标：保护测试运行时本身，而不是业务功能。
- 关注内容：
  - `Playwright runner` 是否能正常退出
  - `Chromium` 本身是否可运行
  - `e2e` 服务托管链路是否可回收
- 诊断用例统一放在：
  - `test/diagnostics/e2e`
- 诊断用例默认不混入正式业务 `e2e` 套件。

## 2. 标准目录结构

```text
SELVUE/test/
  attendance/
    index.js
    unit/
      bootstrap/
      department/
      employee/
      schedule/
      shift/
      tenant/
      workplace/
    integration/
      bootstrap/
      department/
      employee/
      schedule/
      shift/
      tenant/
      workplace/
    e2e/
      README.md
      attendance-workbench.e2e.test.js
      attendance-full-flow.e2e.test.js
      employee-table-layout-regression.e2e.test.js
      mockAttendanceApi.js

  host/
    index.js
    unit/
      projects/
    integration/
      shell/
    e2e/
      README.md
      project-switcher.e2e.test.js

  memory/
    index.js
    unit/
      workspace/
    integration/
      workspace/
    e2e/
      README.md

  diagnostics/
    e2e/
      playwright-runner-exit.e2e.test.js
      playwright-webserver-exit.e2e.test.js
```

## 3. 测试文件命名规范

- `*-project.test.js`
  - 模块注册信息、插件契约、宿主发现入口测试
- `*-state.test.js`
  - 状态工厂、看板默认值、本地状态桥接测试
- `*-form-defaults.test.js`
  - 表单初始值和默认联动测试
- `*-shell.test.js`
  - 首页壳、摘要壳、首屏壳测试
- `*-api.test.js`
  - 服务层请求契约测试
- `*-regression.test.js`
  - 历史 bug 与高风险回归点测试
- `*.e2e.test.js`
  - 真实浏览器链路测试
- `index.js`
  - 测试 adapter 公开入口，不承载具体业务断言

## 4. 测试入口与 alias 规范

### 4.1 测试入口位置
- 测试公开入口统一放在：
  - `test/attendance/index.js`
  - `test/host/index.js`
  - `test/memory/index.js`
- 不允许再把测试入口混入：
  - `src/projects/**/testing`
  - `src/testing`

### 4.2 alias 规范
- 当前标准 alias：
  - `@`
  - `@tests-attendance`
  - `@tests-host`
  - `@tests-memory`
  - `@shared`
- 测试文件应优先通过 adapter 或稳定 alias 引用。

### 4.3 adapter 公开面规则
- adapter 可以暴露：
  - 状态工厂
  - 主 composable
  - 项目注册入口
  - 服务边界函数
- adapter 不应暴露：
  - 与当前测试无关的深层私有实现
  - 仅为了单个测试临时绕开的内部细节

## 5. 测试配置规范

### 5.1 Vitest 配置
- `unit / integration` 统一使用 `Vitest`。
- 当前标准脚本：
  - `npm test`
  - `npm run test:watch`
  - `npm run test:coverage`
- 当前标准环境：
  - `jsdom`
- `Vitest` 当前扫描：
  - `test/**/*.test.js`
- `Vitest` 当前排除：
  - `test/**/*.e2e.test.js`

### 5.2 Playwright 配置
- `e2e` 统一使用 `Playwright`。
- 当前标准浏览器项目：
  - `chromium`
- 当前标准访问地址：
  - `http://127.0.0.1:5180`
- 当前默认正式套件会忽略：
  - `test/diagnostics/**`

### 5.3 coverage 配置
- 当前 coverage 由 `vite.config.js` 统一声明。
- 当前报告输出：
  - `text`
  - `json-summary`
  - `html`
- 当前标准报告目录：
  - `coverage/`
- 当前门槛：
  - `lines >= 80`
  - `functions >= 80`
  - `statements >= 80`
  - `branches >= 80`

## 6. e2e 服务执行规范

### 6.1 默认正式链路
- 默认正式 `e2e` 不再使用 `Playwright webServer` 托管模式。
- 当前标准脚本为：
  - `npm run test:e2e`
- 当前脚本入口为：
  - `scripts/run-playwright-e2e.mjs`

### 6.2 自管链路职责
- `scripts/run-playwright-e2e.mjs` 负责：
  - 启动本地 `Vite` 服务
  - 探活 `http://127.0.0.1:5180`
  - 直接调用本地 `Playwright` CLI
  - 测试完成后显式关闭服务
  - 必要时强制清理服务进程树

### 6.3 保留原因
- 保留项目自管服务链路的原因是：
  - 已经确认 `Windows` 当前环境下 `Playwright webServer` 托管会导致命令收尾不退出
  - 改为项目自管后，正式 `e2e` 已能通过并正常退出
- 后续除非验证新的托管方式稳定，否则不恢复 `webServer` 模式。

## 7. 诊断配置规范

### 7.1 诊断配置文件
- 当前专项诊断配置为：
  - `playwright.diagnostic.runner.config.js`
  - `playwright.diagnostic.webserver.config.js`

### 7.2 诊断用途
- `playwright.diagnostic.runner.config.js`
  - 只验证 `Playwright runner + Chromium` 是否能正常运行和退出
- `playwright.diagnostic.webserver.config.js`
  - 验证托管服务链路是否会引发启动或回收问题

### 7.3 诊断原则
- 诊断配置只用于排障，不混入正式回归链。
- 诊断用例应尽量最小化，不依赖复杂业务页面与后端状态。

## 8. 注释规范

### 8.1 Vue 测试文件注释规则
- `js` 测试文件使用中文业务语义注释。
- 注释应说明：
  - 当前测试保护哪条业务链
  - 为什么该默认值或接线重要
  - 当前断言对应哪个模块职责

### 8.2 用例注释规则
- 每个测试用例前应有中文业务注释。
- 注释应说明：
  - 测试目的
  - 保护的业务场景
  - 避免的回退风险

### 8.3 e2e 注释规则
- `e2e` 用例应按业务链分段注释。
- 每段操作前应说明：
  - 当前业务步骤
  - 为什么要这样串联
  - 这一段验证的闭环是什么

## 9. unit、integration、e2e 使用边界

### 9.1 unit 规范
- `unit` 按模块归属放置。
- 优先覆盖：
  - 表单默认值
  - 空状态
  - 本地筛选修复
  - URL、导出、提示等轻量行为

### 9.2 integration 规范
- `integration` 直接保护模块接线关系。
- 优先覆盖：
  - URL
  - HTTP 方法
  - 查询参数
  - body 内容
  - 宿主壳与工程注册表接线

### 9.3 e2e 规范
- `e2e` 保护真实浏览器主链。
- `attendance` 当前至少应覆盖：
  - 首屏壳
  - 关键业务链
  - 导出或批量动作
- `host` 当前至少应覆盖：
  - 工程切换
- `memory` 后续应逐步补真实 `e2e`，不长期停留在只有 `README` 的状态。

## 10. 执行与验证规范

- 只要改了前端业务代码、测试代码、测试配置或测试执行脚本，就必须做匹配验证。
- 至少要跑与改动层级直接相关的测试。
- 若涉及真实浏览器链路，必须执行 `npm run test:e2e`。

推荐命令：

```bash
npm test
npm run test:coverage
npm run test:e2e
npx vitest run test/attendance/unit/**
npx vitest run test/attendance/integration/**
npx playwright test --config playwright.diagnostic.runner.config.js
npx playwright test --config playwright.diagnostic.webserver.config.js
```

## 11. 后续补强建议

- 为 `memory` 补首批真实业务 `e2e`
- 为 `attendance` 继续补更多高风险 `regression` 用例
- 继续把大型视图组件纳入覆盖率统计范围
- 若后续需要抽公共测试夹具，可单独考虑 `test/support`，但不能破坏现有模块边界
