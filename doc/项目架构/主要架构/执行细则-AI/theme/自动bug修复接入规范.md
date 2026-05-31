# SELTHEME 自动bug修复接入规范

## 1. 文档定位

本文档用于规定主题工程接入自动 bug 修复能力时，必须满足的基础条件和实施规范。

自动修复链路中的主题包定位、preview 目录判断和主题层元数据回放，必须优先基于：

- `/Users/showfolder/Documents/workSpace/SELF/SELTEMPLATE/SELTHEME`

## 2. 接入目标

接入自动修复后，至少要支持：

- 自动定位问题主题包
- 自动定位问题层级是 token、component、asset 还是 preview
- 自动关联截图、引用路径和预览入口
- 自动执行主题回归验证

## 3. 工程前置条件

必须具备：

1. 主题集合层结构清晰
2. 单主题包边界清晰
3. token、component、asset、preview 分层清晰
4. 可执行预览验证
5. 文档可检索

## 4. 测试接入要求

- 视觉问题必须保留真实预览截图
- 目录重构问题必须补相对引用验证
- 修复后必须补目标 preview 回归验证
- 相邻主题包若受影响，至少做一轮最小回归
- 互动 demo 修复必须补浏览器级点击、输入、切换或浮层验证
- 若存在主题 demo 打开脚本，还应验证启动地址正确且退出后端口被回收

## 5. 缺陷定位输入要求

自动修复入口应优先接受：

- 失败截图
- 失败 preview URL
- 失败 CSS 引用路径
- 失败资产路径
- 失败目录结构描述

## 6. 自动修复执行边界

自动修复默认只应修改：

- 明确命中的主题包
- 明确命中的 `tokens.css`
- 明确命中的 `components.css`
- 明确命中的 `assets/`
- 明确命中的 `preview/`
- 与当前问题直接相关的根 README 或主题说明

若问题命中可互动 demo 结构，还应继续细分到 preview 子层，而不是把所有问题都归为单个 html：

- `preview_shell_html`
- `preview_fragment_html`
- `preview_interaction_script`
- `preview_local_style`
- `preview_demo_launcher`
- `preview_runtime_feedback`

若问题属于主题工程缺口，应优先明确缺口类型：

- `theme_package_structure`
- `token_layer`
- `component_style_layer`
- `component_buttons_layer`
- `component_forms_layer`
- `component_tabs_layer`
- `component_tables_layer`
- `component_trees_layer`
- `component_overlays_layer`
- `theme_script_layer`
- `asset_reference_layer`
- `preview_entry_layer`
- `interactive_demo_layer`
- `theme_output_verifier`
- `theme_prefix_replacer`
- `workspace_stretch_layout_layer`
- `single_sample_grid_layer`
- `workspace_meta_wrap_layer`

## 7. 验证闭环

自动修复完成后至少形成三类验证：

1. 复现验证
2. 修复验证
3. 相邻回归验证

若问题命中主题目录或预览链，还必须额外形成：

4. 目录结构验证
5. 相对引用验证
6. 预览截图验证
7. 互动反馈验证
8. 启动脚本生命周期验证
9. 主题层元数据一致性验证
10. 旧前缀残留清零验证
11. 工作区纵向拉伸验证
12. 单样例层宽度收敛验证

## 8. 执行要求

以后凡是声称“已接入自动 bug 修复能力”的主题工程，至少应满足：

1. 有清晰主题包目录结构
2. 有可执行 preview 验证
3. 有主题工程文档
4. 有稳定的 token、component、asset、preview 边界
5. 若采用左侧目录 + 右侧样例区结构，必须能把“目录过高导致样例区被拉伸”“单样例层残留空白列”“顶部胶囊竖向堆叠拉高首屏”判定为正式可修复缺口
