// 主题选项只描述业务工程允许切换的正式主题，三套主题都来自模板工程的正式 themes 目录。
// 定义 考勤主题选项 处理入口，承接当前业务动作。
export const attendanceThemeOptions = [
  {
    // 维护 value 字段，供当前前端状态或配置直接使用。
    value: 'admin-workbench-dark',
    // 维护 labelKey 字段，供当前前端状态或配置直接使用。
    labelKey: 'themeAdminWorkbenchDark',
    // 维护 short 字段，供当前前端状态或配置直接使用。
    short: 'A'
  },
  {
    // 维护 value 字段，供当前前端状态或配置直接使用。
    value: 'attendance-glass-dark',
    // 维护 labelKey 字段，供当前前端状态或配置直接使用。
    labelKey: 'themeAttendanceGlassDark',
    // 维护 short 字段，供当前前端状态或配置直接使用。
    short: 'B'
  },
  {
    // 维护 value 字段，供当前前端状态或配置直接使用。
    value: 'liquid-glass',
    // 维护 labelKey 字段，供当前前端状态或配置直接使用。
    labelKey: 'themeLiquidGlass',
    // 维护 short 字段，供当前前端状态或配置直接使用。
    short: 'C'
  }
// 执行当前业务步骤，推进本行对应的 constants 处理。
]

// 默认先使用最稳重的跨项目通用后台主题，其余主题作为品牌化切换补充。
// 定义 default考勤主题 处理入口，承接当前业务动作。
export const defaultAttendanceTheme = 'admin-workbench-dark'
