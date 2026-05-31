# uniauth + attendance 改造成 Spring Security 方案的落地改造清单

## 1. 文档定位

本文档只服务当前真实项目：

- 项目根目录：`/Users/showfolder/Documents/workSpace/SELF/SELATTENDANCE`
- 后端工程：`SELSP`
- 前端工程：`SELVUE`

本文档不是 Spring Security 通用教程。  
本文档只回答一件事：

**如何把当前 `uniauth + attendance` 的自定义认证鉴权接法，收口为 `Spring Security` 正式方案。**

## 2. 当前现状

当前系统已经具备以下基础：

- `uniauth` 已独立成权限业务域
- `attendance` 已作为资源业务域独立存在
- 双数据库已经拆开：
  - `attendance_db`
  - `uniauth_db`
- `uniauth` 已具备用户、角色、菜单、租户等基础能力
- `attendance` 已开始消费宿主上下文和权限中心信息

当前仍不够正式的部分：

- token 生成与校验仍偏手工
- 安全上下文注入仍偏自定义
- 接口级鉴权没有完全收口到统一安全框架
- 模块 / 按钮 / 数据范围权限还没有完整沉到标准授权链

## 3. 改造目标

改造后的正式目标不是“删掉 `uniauth`，只保留 Spring Security”。

正式目标应是：

- `Spring Security` 负责安全底座
- `uniauth` 负责权限业务主数据
- `attendance` 负责资源接口和业务数据

角色分工必须固定为：

### 3.1 Spring Security 负责

- 登录认证过滤链
- JWT 校验
- SecurityContext 注入
- 401 / 403 统一处理
- 权限注解拦截
- 资源接口访问控制

### 3.2 uniauth 负责

- 用户管理
- 角色管理
- 菜单管理
- 租户管理
- 权限码管理
- 数据范围定义
- token 签发时的 claim 组装

### 3.3 attendance 负责

- 消费 token 中的用户上下文
- 消费 token 中的租户上下文
- 消费 token 中的权限码
- 消费 token 中的数据范围
- 在业务查询里叠加数据权限过滤

## 4. 正式架构形态

推荐正式形态如下：

```text
前端 SELVUE
   ↓ Bearer Token
attendance 资源系统（Spring Security Resource Server / JWT）
   ↑ 校验 Token / 提取 Claim
uniauth 权限中心（Spring Security Auth Server 风格 or 自建登录签发层）
```

当前项目更现实的落地方式不是一步上完整 OAuth2 平台，而是分两步：

### 4.1 第一阶段

先把当前 `uniauth` 改造成：

- 统一登录入口
- 统一 JWT 签发
- 统一用户上下文 claim 输出
- 统一权限码 claim 输出
- 统一租户与数据范围 claim 输出

### 4.2 第二阶段

再把 `attendance` 改造成：

- 基于 Spring Security 的 JWT 资源服务器
- 统一认证过滤器链
- 统一权限注解
- 统一 401 / 403 返回
- 统一数据范围桥接

## 5. 后端改造清单

### 5.1 uniauth 侧改造

必须新增或收口以下能力：

1. 安全配置
- 新增 `SecurityFilterChain`
- 统一登录接口白名单
- 统一管理员接口鉴权规则

2. 登录认证服务
- 使用 `AuthenticationManager` 或等价认证入口
- 不再让 controller 直接拼认证逻辑

3. UserDetails 映射
- 把 `uniauth` 用户对象映射成 Spring Security 身份对象
- 映射内容至少包含：
  - userId
  - username
  - displayName
  - tenantCode
  - roleCodes
  - permissionCodes
  - dataScopes

4. JWT 签发
- token 里必须写入正式 claim：
  - `sub`
  - `user_id`
  - `tenant_code`
  - `role_codes`
  - `permission_codes`
  - `data_scopes`
  - `menu_codes`

5. 401 / 403 处理
- 登录失败统一返回 401
- 权限不足统一返回 403
- 返回结构必须保持项目统一响应风格

### 5.2 attendance 侧改造

必须新增或收口以下能力：

1. 资源服务器配置
- 新增 `SecurityFilterChain`
- 接入 JWT 解析器
- 从 token 中恢复当前用户上下文

2. CurrentUser 统一工厂
- 把 token claim 映射成业务可消费对象
- 统一生成：
  - 当前用户
  - 当前租户
  - 当前权限码集合
  - 当前数据范围集合

3. 控制器权限注解
- 对模块级接口增加 `@PreAuthorize`
- 禁止再把权限判断散落在 controller 里手写 `if`

4. 服务层数据范围桥接
- 对员工、排班、打卡、月次、规则、接入配置等查询补 `dataScope`
- 至少支持：
  - `ALL`
  - `TENANT`
  - `DEPARTMENT`
  - `SELF`

5. 审计与操作人
- 从 Spring Security 上下文统一读取操作者
- 禁止继续让业务层手填固定审批人 / 操作人

## 6. 前端改造清单

### 6.1 登录与会话

- 登录成功后统一保存 access token
- request 层统一自动带 Bearer Token
- token 失效统一跳转登录或弹统一提示

### 6.2 菜单权限

- 菜单显示必须按 `menu_codes`
- `attendance` 和 `uniauth` 的 project 入口都应支持动态显隐

### 6.3 按钮权限

- 创建
- 修改
- 删除
- 审批
- 月结
- 反结
- 重算
- 导出
- 规则编辑
- connector 重试

这些动作都必须按 `permission_codes` 控制显隐或禁用。

### 6.4 数据权限提示

前端要能识别当前数据范围：

- 全量
- 本租户
- 本部门
- 仅本人

必要时在页面上给出范围提示，避免用户误以为“系统没数据”。

## 7. 建议落地顺序

### 第一步

先改 `uniauth`

- 统一登录认证
- 统一 JWT 签发
- 统一 claim 模型

### 第二步

再改 `attendance`

- 接 Spring Security
- 接 JWT 解析
- 接当前用户上下文恢复

### 第三步

补模块级权限注解

- 先覆盖 `attendance`
- 再覆盖 `uniauth`

### 第四步

补数据范围过滤

- 先从员工、部门、排班开始
- 再扩到打卡、月次、规则、connector

### 第五步

补前端菜单与按钮权限控制

## 8. 本次不建议做的事

当前不建议一上来就做：

- 完整 OAuth2 授权服务器大而全平台化
- SSO 联邦登录
- 第三方身份源接入
- 复杂 ABAC 引擎

当前项目最重要的是：

**先把 `Spring Security` 接成稳定底座，把现有 `uniauth` 和 `attendance` 的安全链路正规化。**

## 9. 验收标准

达到以下条件，才算改造完成：

1. 登录、登出、token 校验都走 Spring Security 链
2. `attendance` 接口可从 token 恢复当前用户上下文
3. `attendance` 接口已按权限码控制访问
4. 前端菜单已按菜单权限动态显示
5. 前端关键按钮已按权限码动态显隐
6. 至少一条主数据查询已叠加数据范围过滤
7. 401 / 403 已统一返回
8. 中日双语提示已补齐

## 10. 当前结论

当前项目最合理的正式方向不是：

- 放弃 `uniauth`
- 只用 Spring Security 直接硬写角色判断

当前项目最合理的正式方向是：

**保留 `uniauth` 作为权限业务中心，用 Spring Security 做安全底座。**
