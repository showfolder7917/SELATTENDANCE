// 主题选项沿用 attendance 已稳定的三套主题，保证两个工程共享同一视觉基底。
export const uniauthThemeOptions = [
  // 第一套主题是最稳的通用后台深色主题。
  { value: 'admin-workbench-dark', labelKey: 'themeAdminWorkbenchDark', short: 'A' },
  // 第二套主题复用考勤玻璃深色，方便对齐宿主工作台氛围。
  { value: 'attendance-glass-dark', labelKey: 'themeAttendanceGlassDark', short: 'B' },
  // 第三套主题保留液态玻璃浅色，供截图和演示场景切换。
  { value: 'liquid-glass', labelKey: 'themeLiquidGlass', short: 'C' }
]

// 默认主题继续使用通用后台深色，减少权限中心和 attendance 首页之间的视觉落差。
export const defaultUniauthTheme = 'admin-workbench-dark'
