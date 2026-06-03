// 权限中心工作台现在有五个正式管理域，先收口成稳定常量，避免导航和区块判断散落。
// 当前导航顺序保持模块在菜单前，方便管理员先看模块注册，再看模块下的菜单节点。
export const uniauthSectionKeys = ['tenant', 'user', 'role', 'module', 'menu']

// 根据当前文案和摘要数据生成左侧导航项，结构对齐 attendance 的 navItems 口径。
export function buildWorkbenchSectionItems(t, summary) {
  // 返回稳定数组，供导航、当前模块头部和统计角标统一复用。
  return [
    {
      key: 'tenant',
      label: t('navTenant'),
      caption: t('sectionTenantHint'),
      badge: String(summary?.tenantCount || 0)
    },
    {
      key: 'user',
      label: t('navUser'),
      caption: t('sectionUserHint'),
      badge: String(summary?.userCount || 0)
    },
    {
      key: 'role',
      label: t('navRole'),
      caption: t('sectionRoleHint'),
      badge: String(summary?.roleCount || 0)
    },
    {
      key: 'module',
      label: t('navModule'),
      caption: t('sectionModuleHint'),
      badge: String(summary?.moduleCount || 0)
    },
    {
      key: 'menu',
      label: t('navMenu'),
      caption: t('sectionMenuHint'),
      badge: String(summary?.menuCount || 0)
    }
  ]
}
