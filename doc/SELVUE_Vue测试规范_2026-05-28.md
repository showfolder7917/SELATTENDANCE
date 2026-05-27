# SELVUE Vue 测试规范

## 0. 当前实现现状与规范目标

### 0.1 当前实现现状
- `SELVUE` 已建立 `attendance / host / memory` 三组前端测试目录。
- `attendance` 已按 `unit / integration / e2e` 分层，并在 `unit / integration` 下继续按 `bootstrap / department / employee / schedule / shift / tenant / workplace` 细分。
- `SELVUE` 已建立独立测试入口：
  - `tests/attendance/index.js`
  - `tests/host/index.js`
  - `tests/memory/index.js`
- `SELVUE` 已接入：
  - `Vitest + jsdom`
  - `Playwright`
  - `coverage`
- 当前覆盖率门槛已在 `vite.config.js` 中设置为：
  - `lines 80`
  - `functions 80`
  - `statements 80`
  - `branches 80`
- `attendance` 已补入首批 `integration`、`e2e` 和独立 `*-regression.test.js` 回归测试文件。

### 0.2 规范目标
- 统一 `SELVUE` 前端 Vue 测试的分层结构、目录方式、命名方式、入口方式和注释风格。
- 保证后续新增测试时，不再把多个模块职责混写到同一个通用 `js` 测试文件中。
- 保证测试可按模块执行、可按层级执行、可长期沉淀历史回归点。
- 保证测试入口与业务源码目录解耦，不再把测试适配层混入 `src`。

## 1. 测试分层原则

### 1.1 unit 层
- 目标：保护单模块本地状态、表单默认值、筛选修复、轻量辅助逻辑。
- 关注内容：
  - 状态工厂默认值
  - 表单初始值
  - 筛选字段修复
  - 本地计数同步
  - 轻量导出桥接
  - toast、URL、默认引用等前端本地行为
- `unit` 测试必须贴模块职责，不允许再建立“通用状态中心”。

### 1.2 integration 层
- 目标：保护前端模块之间和服务边界之间的接线契约。
- 关注内容：
  - `services/*.js` 请求路径
  - HTTP 方法
  - 查询参数
  - 提交体
  - 宿主壳和模块注册表协同
  - 组件视图与 composable 的组合边界
- `integration` 不是回归测试同义词，而是“接线层测试”。

### 1.3 e2e 层
- 目标：保护真实浏览器里的关键业务链。
- 关注内容：
  - 页面加载
  - 模块切换
  - 真实表单交互
  - 业务链路贯通
  - 导出、批量操作、向导、确认框等浏览器级动作
- `e2e` 必须走 Playwright，不允许用 `jsdom` 冒充真实浏览器测试。

### 1.4 regression 规则
- 目标：保护历史 bug 与高风险回归点。
- 关注内容：
  - 已修复问题
  - 容易反复破坏的筛选逻辑
  - 查询串拼接
  - 状态同步
  - 复杂交互链路
- `SELVUE` 当前不单独建立 `regression/` 目录。
- 回归测试必须使用独立文件表达，例如：
  - `employee-query-regression.test.js`
  - `schedule-query-regression.test.js`
  - `department-filters-regression.test.js`

### 1.5 test adapter 层
- 目标：统一测试对模块公开面的依赖边界。
- 关注内容：
  - 模块测试入口 `tests/*/index.js`
  - 允许测试依赖的状态工厂、服务边界、项目注册信息、主 composable 入口
- 测试应优先依赖 `tests` 层 adapter，不应直接依赖长相对路径的源码物理位置。

## 2. 标准目录结构

```text
SELVUE/tests/
  attendance/
    index.js
    unit/
      bootstrap/
        attendance-project.test.js
      department/
        department-filters-regression.test.js
      employee/
        employee-filters-regression.test.js
      schedule/
        schedule-state.test.js
      shift/
        shift-form-defaults.test.js
      tenant/
        tenant-shell.test.js
      workplace/
        workplace-form-defaults.test.js
    integration/
      bootstrap/
        bootstrap-api.test.js
      department/
        department-api.test.js
      employee/
        employee-api.test.js
        employee-query-regression.test.js
      schedule/
        schedule-api.test.js
        schedule-query-regression.test.js
      shift/
        shift-template-api.test.js
      tenant/
        tenant-api.test.js
      workplace/
        workplace-api.test.js
    e2e/
      README.md
      attendance-workbench.e2e.test.js
      attendance-full-flow.e2e.test.js
      mockAttendanceApi.js

  host/
    index.js
    unit/
      projects/
        projects-registry.test.js
    integration/
      shell/
        project-host-app.test.js
    e2e/
      README.md
      project-switcher.e2e.test.js

  memory/
    index.js
    unit/
      workspace/
        memory-project.test.js
    integration/
      workspace/
        memory-workbench-view.test.js
    e2e/
      README.md
```

## 3. 测试文件命名规范

- `*-project.test.js`
  - 用于模块注册信息、插件契约、宿主发现入口测试。
- `*-state.test.js`
  - 用于状态工厂、筛选默认值、看板默认值、批量向导默认值测试。
- `*-form-defaults.test.js`
  - 用于模块表单初始值与联动默认引用测试。
- `*-shell.test.js`
  - 用于首页壳、租户摘要壳、模块摘要壳测试。
- `*-api.test.js`
  - 用于服务层请求契约测试。
- `*-regression.test.js`
  - 用于历史 bug 与高风险回归点测试。
- `*.e2e.test.js`
  - 用于真实浏览器链路测试。
- `index.js`
  - 用于测试 adapter 公开入口，不承载具体业务断言。

## 4. 测试入口与 alias 规范

### 4.1 测试入口位置
- 测试公开入口统一放在：
  - `tests/attendance/index.js`
  - `tests/host/index.js`
  - `tests/memory/index.js`
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
- 测试文件应优先通过测试 adapter 或稳定 alias 引用，不要使用长相对路径直穿 `src`。

### 4.3 adapter 公开面规则
- adapter 只暴露允许测试依赖的稳定边界。
- adapter 可以暴露：
  - 状态工厂
  - 主 composable
  - 项目注册入口
  - 服务边界函数
- adapter 不应暴露：
  - 任意内部私有实现
  - 与当前测试无关的深层目录细节

## 5. 测试配置规范

### 5.1 Vitest 配置
- `unit / integration` 统一使用 `Vitest`。
- `environment` 必须显式为：
  - `jsdom`
- 当前标准脚本：
  - `npm test`
  - `npm run test:watch`
  - `npm run test:coverage`

### 5.2 Playwright 配置
- `e2e` 统一使用 `Playwright`。
- 当前标准脚本：
  - `npm run test:e2e`
- 当前标准浏览器项目：
  - `chromium`
- 当前标准本地地址：
  - `http://127.0.0.1:5180`

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

### 5.4 e2e 服务托管
- Playwright `webServer` 应统一托管前端开发服务生命周期。
- 当前标准命令：
  - `npm run dev:local`
- 不要求手工先起服务。

## 6. 注释规范

### 6.1 Vue 测试文件注释规则
- `import` 上方允许写业务目的注释。
- 测试文件必须使用中文业务语义注释。
- 注释必须说明：
  - 该测试保护哪条业务链
  - 为什么这条默认值或行为重要
  - 它对应哪个模块职责
- 不允许只写语法级空注释，例如：
  - “定义变量”
  - “调用函数”

### 6.2 `it` 用例注释规则
- 每个测试用例前应有中文业务注释。
- 注释应说明：
  - 测试目的
  - 保护的业务场景
  - 避免的回退风险

### 6.3 e2e 注释规则
- `e2e` 用例必须按业务链分段注释。
- 每一段真实操作前应说明：
  - 当前业务步骤
  - 为什么要这样串联
  - 这一段要验证的闭环是什么

## 7. unit 测试规范

- `unit` 测试必须按模块归属放置。
- 不允许再建立一个文件同时承载多个模块的默认状态断言。
- `unit` 应优先覆盖：
  - 表单默认值
  - 空状态
  - 本地筛选修复
  - URL/导出/提示等轻量行为
- `unit` 不负责证明整条浏览器链路。

## 8. integration 测试规范

- `integration` 测试必须直接保护模块接线关系。
- `integration` 应优先覆盖：
  - 请求 URL
  - HTTP 方法
  - 查询参数
  - body 内容
  - 组合入口导出
  - 模块和宿主壳协同
- 复杂查询串与筛选拼接问题应优先补独立 `*-regression.test.js`。

## 9. e2e 测试规范

### 9.1 放置规则
- `e2e` 统一放在各模块目录下的 `e2e/`。
- `attendance`、`host`、`memory` 各自维护自己的浏览器测试。

### 9.2 使用规则
- `e2e` 必须保护真实页面主链。
- `attendance` 的 `e2e` 至少应覆盖：
  - workbench 首屏壳
  - 关键业务链
  - 导出或批量动作
- 当前全链路示例：
  - 租户保存
  - 场所创建
  - 部门创建
  - 员工创建
  - 外部映射
  - CSV 导入导出
  - 班次模板创建
  - 未排班检查
  - 批量排班
  - 排班导出

### 9.3 证据规则
- `e2e` 失败时必须保留：
  - `screenshot`
  - `trace`
- 复杂全链路测试应优先保留 `trace.zip` 作为回看证据。

## 10. regression 测试规范

### 10.1 放置规则
- 回归测试不单独建 `regression/` 目录。
- 回归测试必须使用独立文件表达，并放在所属层级下。
- 例如：
  - `unit/department/department-filters-regression.test.js`
  - `integration/schedule/schedule-query-regression.test.js`

### 10.2 使用规则
- 每修复一个明确 bug，应新增至少一个最小回归测试。
- 回归测试命名应直接反映问题场景。
- 回归测试可以出现在：
  - `unit`
  - `integration`
  - `e2e`
- 但文件名必须明确带 `regression`。

## 11. 编写优先级

新增业务或修 bug 时，测试补充优先级如下：

1. 先补最贴业务风险的 `unit` 或 `integration`。
2. 若属于真实页面主链，再补 `e2e`。
3. 若属于已修历史问题，再补独立 `*-regression.test.js`。
4. 若出现重复导出或重复引用路径，再回收进 `tests/*/index.js` adapter。

## 12. 执行与验证规范

- 只要本轮改了前端业务代码、测试代码或测试配置，必须执行测试验证。
- 至少要跑与改动层级直接相关的测试。
- 完成后应补一轮相邻回归。
- 若涉及真实页面或交互链路，必须补 `e2e` 或真实页面验证。

推荐命令示例：

```bash
npm test
npm run test:coverage
npm run test:e2e
npx vitest run tests/attendance/unit/**
npx vitest run tests/attendance/integration/**
npx playwright test tests/attendance/e2e/**
```

## 13. 后续补强建议

- 为 `memory` 补首批真实 `e2e` 用例，而不是只保留 `README.md`
- 为 `attendance` 更多高风险交互链继续补 `*-regression.test.js`
- 对更多大型 Vue 组件继续补覆盖率统计范围
- 在后续条件允许时，为公共测试夹具补独立 `tests/support` 层，但不能破坏现有模块边界
