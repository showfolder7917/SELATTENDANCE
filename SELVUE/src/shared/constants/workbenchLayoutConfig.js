// shared 工作台配置入口统一收口页面壳宽度、宽屏断点和分栏比例，避免这些数字继续散落在各工程页面里。

// 工作台相关断点统一定义在 shared 层，后续所有工程都应直接复用这里而不是再手写 1180、1660、3000 之类的阈值。
export const workbenchViewportBreakpoints = {
  // 超窄屏下页面壳左右留白需要进一步压缩，避免首屏卡片在手机上被过度挤压。
  shellCompactMaxWidth: 720,
  // 三分栏在这个阈值以下统一退回单列堆叠，避免在平板和小屏上继续强行拖拽分栏。
  splitStackedMaxWidth: 1180,
  // 宽工作台进入大桌面时开始放大页面最大宽度，给中栏表格和右栏表单更多实际空间。
  wideWorkbenchMinWidth: 1660,
  // 超宽显示器下继续抬高页面上限，避免已经进入超宽屏却仍停留在中等桌面的壳宽。
  ultraWideWorkbenchMinWidth: 3000
}

// 工作台视口间距统一定义在 shared 层，避免 hero 下间距和底部呼吸空间继续在各页面手写不同数字。
export const workbenchViewportMetrics = {
  // hero 和 workbench 之间统一保留 20px 顶部间距，让三栏主体不会直接贴住英雄栏。
  topGapPx: 20,
  // 工作台底部统一保留 16px 呼吸空间，避免滚动区紧贴浏览器底边。
  bottomBreathingPx: 16
}

// 分栏 preset 统一通过这个工厂生成，保证不同工程拿到的字段名始终一致。
export function createWorkbenchSplitPreset({ defaultLeftPercent, minLeftPercent, maxLeftPercent }) {
  // 默认比例决定首次进入页面时左栏或主表格区的视觉主次关系。
  const normalizedDefaultLeftPercent = defaultLeftPercent
  // 最小比例用于防止拖拽时把左栏压到不可读。
  const normalizedMinLeftPercent = minLeftPercent
  // 最大比例用于防止拖拽时右栏完全失去可用空间。
  const normalizedMaxLeftPercent = maxLeftPercent

  // 返回值同时保留 shared 通用字段名和现有 attendance 兼容字段名，避免迁移期间打断旧页面接线。
  return {
    // shared 通用字段直接服务新页面和新组件。
    defaultLeftPercent: normalizedDefaultLeftPercent,
    // shared 通用字段直接服务新页面和新组件。
    minLeftPercent: normalizedMinLeftPercent,
    // shared 通用字段直接服务新页面和新组件。
    maxLeftPercent: normalizedMaxLeftPercent,
    // 兼容字段继续服务当前已经大量存在的 attendance 旧接线方式。
    outerDefaultLeftPercent: normalizedDefaultLeftPercent,
    // 兼容字段继续服务当前已经大量存在的 attendance 旧接线方式。
    outerMinLeftPercent: normalizedMinLeftPercent,
    // 兼容字段继续服务当前已经大量存在的 attendance 旧接线方式。
    outerMaxLeftPercent: normalizedMaxLeftPercent
  }
}

// 入口页壳默认宽度配置服务普通 hero/空态页面，不主动进入超宽工作台模式。
const defaultEntryShellProfile = {
  // 常规桌面下默认保留 32px 总边距，兼顾框内呼吸感和内容宽度。
  baseEdgeGapPx: 32,
  // 普通入口页保持 2140px 上限，避免首屏卡片在大屏里显得过散。
  baseMaxWidthPx: 2140,
  // 手机宽度下入口页壳缩小到 20px 总边距，降低横向挤压。
  compactEdgeGapPx: 20,
  // 手机宽度下仍沿用 2140 的理论上限，真正起作用的是更小的视口减边距表达式。
  compactMaxWidthPx: 2140
}

// 宽工作台配置服务 attendance、布局1号这类三栏页，让它们在大屏和超宽屏下继续展开。
const wideWorkbenchShellProfile = {
  // 常规桌面先沿用 shared 入口页的基线宽度。
  baseEdgeGapPx: 32,
  // 常规桌面先沿用 shared 入口页的基线宽度。
  baseMaxWidthPx: 2140,
  // 手机宽度仍要压缩边距，避免 hero 和工作台容器横向溢出。
  compactEdgeGapPx: 20,
  // 手机宽度仍沿用基线上限，真正变窄的是视口减边距结果。
  compactMaxWidthPx: 2140,
  // 大桌面开始把左右留白略微放大，让整体框体在超大视口里依然靠中稳住。
  wideEdgeGapPx: 36,
  // 大桌面时把内容上限提升到 2280，给三栏工作台更多展开空间。
  wideMaxWidthPx: 2280,
  // 超宽屏再略微放大边距，保持框体与浏览器边缘的呼吸感。
  ultraWideEdgeGapPx: 40,
  // 超宽屏把内容上限提升到 3400，避免在超宽显示器里仍像固定中屏布局。
  ultraWideMaxWidthPx: 3400
}

// 页面壳配置统一暴露给 shared composable 和后续所有工程入口页消费。
export const workbenchShellProfiles = {
  // 普通入口页继续走不主动拉宽的默认 profile。
  defaultEntry: defaultEntryShellProfile,
  // 三栏工作台统一走宽工作台 profile。
  wideWorkbench: wideWorkbenchShellProfile
}

// 统一按 viewport 解析当前页面壳应该采用哪一组边距和最大宽度。
export function resolveWorkbenchShellFrame(shellProfileKey, viewportWidth) {
  // 未显式传 profile 时兜底走普通入口页配置，避免页面壳样式完全丢失。
  const shellProfile = workbenchShellProfiles[shellProfileKey] || workbenchShellProfiles.defaultEntry
  // 先把未知环境下的宽度兜底成常规桌面值，避免 SSR 或早期挂载阶段拿到 NaN。
  const normalizedViewportWidth = Number(viewportWidth) > 0 ? Number(viewportWidth) : 1440

  // 手机宽度优先走 compact 档位，让首屏和工作台都先确保横向可读。
  if (normalizedViewportWidth <= workbenchViewportBreakpoints.shellCompactMaxWidth) {
    return {
      // 返回当前档位的边距供页面壳 style 和后续诊断复用。
      edgeGapPx: shellProfile.compactEdgeGapPx,
      // 返回当前档位的最大宽度供页面壳 style 和后续诊断复用。
      maxWidthPx: shellProfile.compactMaxWidthPx
    }
  }

  // 宽工作台 profile 在超宽显示器下优先进入 ultra 档位。
  if (
    shellProfileKey === 'wideWorkbench' &&
    normalizedViewportWidth >= workbenchViewportBreakpoints.ultraWideWorkbenchMinWidth
  ) {
    return {
      // 超宽屏边距继续略放大，避免页面框体贴边。
      edgeGapPx: shellProfile.ultraWideEdgeGapPx,
      // 超宽屏上限抬高到 3400，允许真正展开三栏工作台。
      maxWidthPx: shellProfile.ultraWideMaxWidthPx
    }
  }

  // 宽工作台 profile 在大桌面进入第二档，让宽屏页和普通首屏区分开。
  if (
    shellProfileKey === 'wideWorkbench' &&
    normalizedViewportWidth >= workbenchViewportBreakpoints.wideWorkbenchMinWidth
  ) {
    return {
      // 大桌面边距使用 36px 档位，保持框体稳定居中。
      edgeGapPx: shellProfile.wideEdgeGapPx,
      // 大桌面上限提升到 2280，让表格和右栏有更多横向空间。
      maxWidthPx: shellProfile.wideMaxWidthPx
    }
  }

  // 其他情况全部回到基线档位，保证普通桌面和未声明 profile 的页面口径一致。
  return {
    // 常规桌面返回 32px 基线边距。
    edgeGapPx: shellProfile.baseEdgeGapPx,
    // 常规桌面返回 2140 基线上限。
    maxWidthPx: shellProfile.baseMaxWidthPx
  }
}

// 页面根壳统一通过这个 helper 生成 style，后续项目不再自己拼接 width/max-width 表达式。
export function buildWorkbenchShellStyle(shellProfileKey, viewportWidth) {
  // 先解析当前 viewport 对应的边距与最大宽度档位。
  const shellFrame = resolveWorkbenchShellFrame(shellProfileKey, viewportWidth)
  // 共享页面壳直接使用同一条 CSS 表达式，保证 inline style 和 CSS 变量口径完全一致。
  const widthExpression = `min(calc(100vw - ${shellFrame.edgeGapPx}px), ${shellFrame.maxWidthPx}px)`

  // 返回值同时写入 width、maxWidth 和调试用 CSS 变量，方便页面壳和后续排查都走同一份结果。
  return {
    // width 直接驱动当前页面壳的实际宽度。
    width: widthExpression,
    // maxWidth 和 width 保持一致，避免其他样式层再把壳宽度改回去。
    maxWidth: widthExpression,
    // CSS 变量保留给 shared 样式层和浏览器调试工具直接读取当前档位。
    '--selshared-shell-edge-gap': `${shellFrame.edgeGapPx}px`,
    // CSS 变量保留给 shared 样式层和浏览器调试工具直接读取当前档位。
    '--selshared-shell-max-width': `${shellFrame.maxWidthPx}px`
  }
}

// attendance 左导航加主工作区比例继续保留为一个独立 preset，但源头已经迁到 shared。
export const attendanceShellLayoutPreset = createWorkbenchSplitPreset({
  // attendance 壳层左导航默认较窄，让中间工作区尽可能吃到更多首屏宽度。
  defaultLeftPercent: 13,
  // attendance 壳层左导航最小仍要保留可读导航标题和 badge。
  minLeftPercent: 9,
  // attendance 壳层左导航最大限制在 22，避免导航区反过来挤压主工作台。
  maxLeftPercent: 22
})

// attendance 初始化概览区是左摘要右表单的两栏页面，需要稍微偏左主区。
export const attendanceOverviewLayoutPreset = createWorkbenchSplitPreset({
  // 左侧摘要区默认更宽，便于容纳初始化步骤卡和说明块。
  defaultLeftPercent: 64,
  // 最小值防止摘要卡被拖到过窄导致断行严重。
  minLeftPercent: 42,
  // 最大值防止右侧表单区被压扁到不可操作。
  maxLeftPercent: 76
})

// attendance 基础资料频道统一复用同一套两栏比例，减少多频道间的编辑观感漂移。
export const attendanceMasterDataLayoutPreset = createWorkbenchSplitPreset({
  // 列表区默认略宽，保证首屏能先看到更多数据行。
  defaultLeftPercent: 64,
  // 最小值保证左侧数据表和右侧表单都还可读。
  minLeftPercent: 42,
  // 最大值避免右侧编辑区失去稳定宽度。
  maxLeftPercent: 78
})

// attendance 打卡记录区中间表格和右侧补录表单采用更偏表格的比例。
export const attendancePunchLayoutPreset = createWorkbenchSplitPreset({
  // 打卡记录默认让左侧主表格吃到更多宽度，方便表头和筛选项稳定铺开。
  defaultLeftPercent: 65,
  // 最小值仍保留中栏基础可用宽度，避免筛选栏和表格列挤压。
  minLeftPercent: 48,
  // 最大值防止右侧手工补录和 CSV 导入卡片被压到不可用。
  maxLeftPercent: 78
})

// attendance 日次结果要同时容纳状态、班次和时间字段，主列表比打卡记录再略宽一些。
export const attendanceDailyLayoutPreset = createWorkbenchSplitPreset({
  // 日次主列表默认略宽，减少关键字段换行。
  defaultLeftPercent: 67,
  // 最小值仍保证右侧详情和审批操作区可读。
  minLeftPercent: 48,
  // 最大值避免右栏被拖到只剩窄条。
  maxLeftPercent: 78
})

// attendance 异常处理需要更强调主列表和审批流程，所以再把左侧主区略微放大。
export const attendanceCaseLayoutPreset = createWorkbenchSplitPreset({
  // 异常处理默认让列表区承接更多案例上下文。
  defaultLeftPercent: 68,
  // 最小值保证右侧审批动作区仍保留稳定按钮和表单宽度。
  minLeftPercent: 50,
  // 最大值防止右侧审批区失去可操作性。
  maxLeftPercent: 80
})

// attendance 月次汇总沿用异常处理同等级的主区宽度，让统计列和详情面板都稳定。
export const attendanceMonthlyLayoutPreset = createWorkbenchSplitPreset({
  // 月次主区默认和异常处理保持同级，减少频道切换时的宽度跳变。
  defaultLeftPercent: 68,
  // 最小值保证右侧月次详情和月结操作区可读。
  minLeftPercent: 50,
  // 最大值避免右栏完全缩掉。
  maxLeftPercent: 80
})

// attendance 排班页中间月历本身超宽，所以主区比例需要比其他频道再大一档。
export const attendanceScheduleLayoutPreset = createWorkbenchSplitPreset({
  // 排班页默认最大程度优先中间月历主区。
  defaultLeftPercent: 72,
  // 最小值保证左侧员工和班次过滤仍然可用。
  minLeftPercent: 56,
  // 最大值限制在 80，给右侧详情和批量动作留出稳定空间。
  maxLeftPercent: 80
})

// uniauth 壳层也迁到 shared 配置源，后续其他工程要复用时直接在这里追加 preset。
export const uniauthShellLayoutPreset = createWorkbenchSplitPreset({
  // 权限中心左导航默认比 attendance 略宽，方便容纳模块说明和全局动作。
  defaultLeftPercent: 18,
  // 最小值保证导航和按钮不会过度挤压。
  minLeftPercent: 12,
  // 最大值避免左侧导航反向压缩主内容区。
  maxLeftPercent: 24
})

// uniauth 主内容区默认让中间列表区更宽，右侧详情区仍保留稳定编辑空间。
export const uniauthContentLayoutPreset = createWorkbenchSplitPreset({
  // 权限中心列表默认更宽，保证权限点和菜单字段首屏更稳定。
  defaultLeftPercent: 70,
  // 最小值防止中栏列表被拖得太窄。
  minLeftPercent: 56,
  // 最大值避免右侧详情区失去表单可用宽度。
  maxLeftPercent: 82
})
