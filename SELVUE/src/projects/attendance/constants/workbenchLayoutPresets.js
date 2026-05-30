// attendance 工程自己的工作台宽度预设统一放在这里，避免把业务宽度写死到 shared 组件里。

// 外层壳层分栏负责“左导航 + 主工作区”的基础宽度关系，其他工程可继续使用 shared 默认值。
export const attendanceShellLayoutPreset = {
  outerDefaultLeftPercent: 13,
  outerMinLeftPercent: 9,
  outerMaxLeftPercent: 22
}

// 初始化概览区是考勤工程自己的双栏概览布局，左侧摘要比右侧表单略宽。
export const attendanceOverviewLayoutPreset = {
  outerDefaultLeftPercent: 64,
  outerMinLeftPercent: 42,
  outerMaxLeftPercent: 76
}

// 基础资料四个频道统一走同一套两栏宽度，让列表区和编辑区观感保持一致。
export const attendanceMasterDataLayoutPreset = {
  outerDefaultLeftPercent: 64,
  outerMinLeftPercent: 42,
  outerMaxLeftPercent: 78
}

// 打卡记录列表需要比基础资料更宽一些，保证筛选栏和表格列在首屏更稳定。
export const attendancePunchLayoutPreset = {
  outerDefaultLeftPercent: 65,
  outerMinLeftPercent: 48,
  outerMaxLeftPercent: 78
}

// 日次结果列表要同时容纳状态、班次和打卡时间，所以左侧列表区保持更宽默认值。
export const attendanceDailyLayoutPreset = {
  outerDefaultLeftPercent: 67,
  outerMinLeftPercent: 48,
  outerMaxLeftPercent: 78
}

// 异常处理列表要比日次略宽，同时右侧审批区仍需保留稳定操作宽度。
export const attendanceCaseLayoutPreset = {
  outerDefaultLeftPercent: 68,
  outerMinLeftPercent: 50,
  outerMaxLeftPercent: 80
}

// 排班页中间是超宽月历主区，因此左侧主区默认占比和允许范围都要比其他频道更大。
export const attendanceScheduleLayoutPreset = {
  outerDefaultLeftPercent: 72,
  outerMinLeftPercent: 56,
  outerMaxLeftPercent: 80
}
