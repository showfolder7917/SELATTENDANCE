# SELVUE

用户管理台前端工程。

## 运行

```bash
npm install
npm run dev:local
```

## 启动器

- `打开SELVUE用户管理台.command`：只启动前端并打开页面。
- `一键启动SELVUE用户管理全套.command`：先启动 `SELSP` 后端，再启动前端，关闭脚本窗口后两者一并停止。

## 构建

```bash
npm run build
```

## 目录说明

- `src/views/`：页面级视图
- `src/components/`：可复用界面块
- `src/composables/`：状态、动作和副作用封装
- `src/services/`：接口与本地回退服务
- `src/constants/`：导航、枚举和主题样例常量
- `doc/`：项目文档
