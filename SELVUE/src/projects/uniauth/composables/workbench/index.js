import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import {
  clearAuthSession,
  readAuthSession,
  subscribeAuthSession,
  writeAuthSession
} from '../../../../shared/services/authSession'
import { resolveLocalizedRequestError } from '../../../../shared/services/request'
import { localeOptions, messages } from '../../constants/i18nMessages'
import { buildWorkbenchSectionItems, uniauthSectionKeys } from '../../constants/workbenchSections'
import {
  fetchAttendanceHostContext,
  fetchCurrentUser,
  fetchUniauthWorkbench,
  loginUniauth,
  saveModule,
  saveMenu,
  saveRole,
  saveTenant,
  saveUser
} from '../../services'

// 登录表单默认值统一收口，保证重置登录输入时不会出现字段漏清理。
const LOGIN_FORM_DEFAULTS = {
  loginName: '',
  password: ''
}

// 模块表单默认值统一收口，保证模块主数据新增和编辑之间可以稳定复位。
const MODULE_FORM_DEFAULTS = {
  id: null,
  moduleCode: '',
  moduleName: '',
  moduleType: 'business',
  moduleDesc: '',
  entryProject: '',
  ownerSystem: '',
  routeKey: '',
  enabledFlag: true
}

// 租户表单默认值收口成稳定对象，方便新增和编辑之间复位。
const TENANT_FORM_DEFAULTS = {
  id: null,
  tenantCode: '',
  tenantName: '',
  tenantStatus: 'enabled',
  contactName: '',
  contactEmail: '',
  contactPhone: ''
}

// 用户表单默认值包含角色文本字段，保证账号编辑区可以直接回填角色绑定表达。
const USER_FORM_DEFAULTS = {
  id: null,
  tenantId: '',
  loginName: '',
  password: '',
  displayName: '',
  displayNameKana: '',
  locale: 'zh-CN',
  email: '',
  phone: '',
  userStatus: 'enabled',
  roleIdsText: ''
}

// 角色表单默认值统一收口，避免授权表达字段在重置时残留旧值。
const ROLE_FORM_DEFAULTS = {
  id: null,
  tenantId: '',
  roleCode: '',
  roleName: '',
  roleDesc: '',
  roleStatus: 'enabled',
  permissionCodesText: '',
  menuCodesText: '',
  dataScopeType: 'tenant',
  dataScopeValue: ''
}

// 菜单表单默认值覆盖动态导航节点维护的全部核心字段。
const MENU_FORM_DEFAULTS = {
  id: null,
  moduleCode: 'attendance',
  menuCode: '',
  parentId: '',
  menuType: 'page',
  routePath: '',
  componentName: '',
  iconName: '',
  sortOrder: 10,
  titleZh: '',
  titleJa: '',
  enabledFlag: true
}

// 工作台摘要默认值确保未加载完成前共享指标卡也能安全渲染。
const SUMMARY_DEFAULTS = {
  moduleCount: 0,
  tenantCount: 0,
  userCount: 0,
  roleCount: 0,
  menuCount: 0
}

// 权限中心工作台总入口负责组装登录态、区块切换、工作台聚合数据和四类保存动作。
export function useUniauthWorkbench() {
  // 当前本地登录态快照直接从统一会话服务读取，保证宿主和权限中心消费同一份 token。
  const authSession = ref(readAuthSession())
  // 当前页面语言优先继承登录用户 locale，未登录时回退到中文默认。
  const locale = ref(resolveInitialLocale(authSession.value?.currentUser?.locale))
  // 当前激活区块默认仍落在模块管理，避免仅因左侧导航顺序调整就改变管理员进入工作台后的首屏落点。
  const activeSection = ref('module')
  // 页面级消息文本统一放在 view 外层，避免每个区块各自维护提示条。
  const messageText = ref('')
  // 页面级消息语气决定提示条颜色和强调层级。
  const messageTone = ref('info')
  // 错误弹出框单独承接失败反馈，避免错误消息再挤占 hero 下方可视高度。
  const errorDialog = reactive({
    open: false,
    title: '',
    message: ''
  })
  // 登录动作等待态单独维护，避免保存动作和登录动作互相污染按钮状态。
  const loginPending = ref(false)
  // 保存等待态统一覆盖四类写操作，避免同一时间重复提交多个管理动作。
  const savePending = ref(false)
  // 工作台刷新等待态单独维护，便于 view 在刷新时控制禁用动作按钮。
  const reloadPending = ref(false)
  // 当前宿主桥接快照直接展示 attendance 宿主已消费到的统一上下文。
  const hostContext = ref(null)
  // 当前用户快照单独缓存，供侧栏、页头和桥接卡重复复用。
  const currentUser = ref(authSession.value?.currentUser || null)
  // 摘要对象承接 bootstrap 直接返回的头部统计值。
  const summary = ref({ ...SUMMARY_DEFAULTS })
  // 模块表格数据直接承接后端工作台聚合。
  const moduleRows = ref([])
  // 租户表格数据直接承接后端工作台聚合。
  const tenantRows = ref([])
  // 用户表格数据直接承接后端工作台聚合。
  const userRows = ref([])
  // 角色表格数据直接承接后端工作台聚合。
  const roleRows = ref([])
  // 菜单表格数据直接承接后端工作台聚合。
  const menuRows = ref([])
  // 权限定义元数据单独保留，供角色区块查看授权参考。
  const permissionRows = ref([])
  // 登录表单负责承接账号密码输入。
  const loginForm = ref(createLoginForm())
  // 模块表单负责承接模块主数据编辑。
  const moduleForm = ref(createModuleForm())
  // 租户表单负责承接租户主资料编辑。
  const tenantForm = ref(createTenantForm())
  // 用户表单负责承接账号主资料和角色绑定表达。
  const userForm = ref(createUserForm())
  // 角色表单负责承接角色主资料和授权表达。
  const roleForm = ref(createRoleForm())
  // 菜单表单负责承接导航节点和双语标题编辑。
  const menuForm = ref(createMenuForm())
  // 会话订阅清理函数单独缓存，避免页面卸载后残留广播监听。
  let unsubscribeAuthSession = () => {}

  // 翻译函数统一从 messages 读取，保证 view、section 组件和主题切换都走同一套双语字典。
  function t(key) {
    // 当前语言命中的文案优先返回，缺失时再回退到中文或 key 本身。
    return messages[locale.value]?.[key] ?? messages['zh-CN']?.[key] ?? key
  }

  // 左侧导航项直接按 attendance 的 badge/caption 结构生成，保证共享导航视觉节奏一致。
  const navItems = computed(() => buildWorkbenchSectionItems(t, summary.value))

  // 当前激活区块元数据同时承接标题、说明和 badge，供头部和导航高亮复用。
  const activeSectionMeta = computed(() => {
    // 先从导航项里找出当前激活项，保证标题和 badge 永远和左侧导航一致。
    const currentNavItem = navItems.value.find((item) => item.key === activeSection.value) || navItems.value[0]
    // 根据激活区块补齐当前主标题和引导文案，避免 view 再写一层 if/else 文案映射。
    const titleMap = {
      module: t('moduleTitle'),
      tenant: t('tenantTitle'),
      user: t('userTitle'),
      role: t('roleTitle'),
      menu: t('menuTitle')
    }
    // lead 文案直接承接当前 section 的业务解释，和 attendance 每个模块头部的职责说明保持一致。
    const leadMap = {
      module: t('moduleLead'),
      tenant: t('tenantLead'),
      user: t('userLead'),
      role: t('roleLead'),
      menu: t('menuLead')
    }
    // 返回合并后的元数据，让 view 只做装配，不自己计算业务展示文案。
    return {
      ...currentNavItem,
      title: titleMap[activeSection.value] || currentNavItem?.label || '',
      lead: leadMap[activeSection.value] || currentNavItem?.caption || ''
    }
  })

  // 顶部摘要卡对齐 attendance 的 SharedMetricCards 结构，根据当前区块输出更有针对性的统计。
  const metricItems = computed(() => {
    // 模块管理优先展示托管模块总量、启用量和 attendance 工程接入量，帮助管理员先看平台治理范围。
    if (activeSection.value === 'module') {
      return [
        { key: 'moduleCount', value: moduleRows.value.length, label: t('summaryModule'), tone: 'default' },
        {
          key: 'enabledModuleCount',
          value: moduleRows.value.filter((row) => row.enabledFlag).length,
          label: t('enabledFlag'),
          tone: 'warm'
        },
        {
          key: 'attendanceModuleCount',
          value: moduleRows.value.filter((row) => row.entryProject === 'attendance').length,
          label: t('entryProject'),
          tone: 'muted'
        }
      ]
    }
    // 租户模块优先展示租户总量、启用量和有联系方式的配置完整度。
    if (activeSection.value === 'tenant') {
      return [
        { key: 'tenantCount', value: tenantRows.value.length, label: t('summaryTenant'), tone: 'default' },
        {
          key: 'enabledTenantCount',
          value: tenantRows.value.filter((row) => String(row.tenantStatus).toLowerCase() === 'enabled').length,
          label: t('enabledFlag'),
          tone: 'warm'
        },
        {
          key: 'contactTenantCount',
          value: tenantRows.value.filter((row) => row.contactName || row.contactEmail || row.contactPhone).length,
          label: t('contactName'),
          tone: 'muted'
        }
      ]
    }
    // 用户模块优先展示用户总量、日语账号量和锁定账号量，方便管理员先看账号分布和风险。
    if (activeSection.value === 'user') {
      return [
        { key: 'userCount', value: userRows.value.length, label: t('summaryUser'), tone: 'default' },
        {
          key: 'japaneseLocaleCount',
          value: userRows.value.filter((row) => row.locale === 'ja-JP').length,
          label: t('locale'),
          tone: 'warm'
        },
        {
          key: 'lockedCount',
          value: userRows.value.filter((row) => row.lockedFlag).length,
          label: t('userStatus'),
          tone: 'danger'
        }
      ]
    }
    // 角色模块优先展示角色总量、内置角色量和权限定义量，帮助管理员判断授权模型复杂度。
    if (activeSection.value === 'role') {
      return [
        { key: 'roleCount', value: roleRows.value.length, label: t('summaryRole'), tone: 'default' },
        {
          key: 'builtinRoleCount',
          value: roleRows.value.filter((row) => row.builtinFlag).length,
          label: t('roleStatus'),
          tone: 'warm'
        },
        {
          key: 'permissionCount',
          value: permissionRows.value.length,
          label: t('permissionReference'),
          tone: 'muted'
        }
      ]
    }
    // 菜单模块优先展示菜单总量、启用量和 attendance 入口量，便于排查宿主菜单显隐问题。
    return [
      { key: 'menuCount', value: menuRows.value.length, label: t('summaryMenu'), tone: 'default' },
      {
        key: 'enabledMenuCount',
        value: menuRows.value.filter((row) => row.enabledFlag).length,
        label: t('enabledFlag'),
        tone: 'warm'
      },
      {
        key: 'attendanceMenuCount',
        value: menuRows.value.filter((row) => row.moduleCode === 'attendance').length,
        label: t('moduleCode'),
        tone: 'muted'
      }
    ]
  })

  // 当前用户信息面板统一输出键值对，让 side 区既能展示给管理员看，也能方便截图校验。
  const currentUserEntries = computed(() => {
    // 当前没有登录用户时直接回空数组，避免 side 面板渲染无意义占位。
    if (!currentUser.value) {
      return []
    }
    // 返回稳定的展示字段集合，重点覆盖租户、语言和权限规模。
    return [
      { key: 'username', label: t('loginName'), value: currentUser.value.username || '-' },
      { key: 'displayName', label: t('loginDisplayName'), value: currentUser.value.displayName || '-' },
      { key: 'tenantCode', label: t('tenantCode'), value: currentUser.value.tenantCode || '-' },
      { key: 'locale', label: t('locale'), value: currentUser.value.locale || '-' },
      {
        key: 'permissionCodes',
        label: t('permissionCodes'),
        value: String(currentUser.value.permissionCodes?.length || 0)
      },
      {
        key: 'menuCodes',
        label: t('menuCodes'),
        value: String(currentUser.value.menuCodes?.length || 0)
      }
    ]
  })

  // 宿主桥接区统一把 JSON 快照格式化成文本，方便页面直接显示和截图留证。
  const hostContextPreview = computed(() => {
    // 没取到宿主上下文时用可读提示代替空白区域。
    if (!hostContext.value) {
      return t('hostContextMissing')
    }
    // 正常情况下把宿主上下文格式化成多行 JSON，便于校验菜单码和数据范围是否已贯通。
    return JSON.stringify(hostContext.value, null, 2)
  })

  // 角色区权限参考区直接把权限定义列表格式化成行文本，避免表单旁边只有原始 JSON 可读性差。
  const permissionReferenceRows = computed(() =>
    permissionRows.value.map((row) => ({
      id: row.id,
      label: row.permissionName || row.permissionCode,
      hint: [row.permissionCode, row.permissionType, row.scopeType].filter(Boolean).join(' / ')
    }))
  )

  // 登录后或刷新工作台时统一覆盖当前数据区块，保证列表、摘要和当前用户快照始终同源。
  function applyWorkbench(workbench) {
    // 聚合结果不存在时直接回退，不对当前页面状态做无意义写入。
    if (!workbench) {
      return
    }
    // 当前用户优先使用 bootstrap 的统一快照，让宿主和权限中心都消费同一口径的身份数据。
    currentUser.value = workbench.currentUser || currentUser.value || authSession.value?.currentUser || null
    // 模块列表直接替换成后端聚合输出，保证模块管理区块和摘要统计同源。
    moduleRows.value = Array.isArray(workbench.modules) ? workbench.modules : []
    // 租户列表直接替换成后端聚合输出，避免前端自己拼装列表。
    tenantRows.value = Array.isArray(workbench.tenants) ? workbench.tenants : []
    // 用户列表直接替换成后端聚合输出，保证当前页面和写接口回刷后状态一致。
    userRows.value = Array.isArray(workbench.users) ? workbench.users : []
    // 角色列表直接替换成后端聚合输出。
    roleRows.value = Array.isArray(workbench.roles) ? workbench.roles : []
    // 菜单列表直接替换成后端聚合输出。
    menuRows.value = Array.isArray(workbench.menus) ? workbench.menus : []
    // 权限元数据列表直接替换成后端聚合输出，供角色授权参考区消费。
    permissionRows.value = Array.isArray(workbench.permissions) ? workbench.permissions : []
    // 摘要优先采用后端直接计算结果，缺失时再由前端根据各列表长度兜底。
    summary.value = {
      moduleCount: workbench.summary?.moduleCount ?? moduleRows.value.length,
      tenantCount: workbench.summary?.tenantCount ?? tenantRows.value.length,
      userCount: workbench.summary?.userCount ?? userRows.value.length,
      roleCount: workbench.summary?.roleCount ?? roleRows.value.length,
      menuCount: workbench.summary?.menuCount ?? menuRows.value.length
    }
    // 如果当前登录态已经存在 accessToken，就把最新 currentUser 快照回写到统一会话，供宿主菜单过滤立即刷新。
    if (authSession.value?.accessToken && currentUser.value) {
      const nextSession = {
        ...authSession.value,
        currentUser: currentUser.value
      }
      authSession.value = nextSession
      writeAuthSession(nextSession)
    }
  }

  // 刷新工作台统一走聚合接口和宿主桥接接口，避免四个区块各自重复取数。
  async function reloadWorkbench(options = {}) {
    // 显式等待态帮助页面区分“正在刷新”和“普通空列表”。
    reloadPending.value = true
    try {
      // 先读取聚合工作台，保证主列表和摘要一次性同步更新。
      const workbench = await fetchUniauthWorkbench()
      applyWorkbench(workbench)
      // 再读取宿主上下文，确认 attendance 是否已经消费当前统一登录态。
      hostContext.value = await fetchAttendanceHostContext()
      // 当前登录态已经存在时，用 me 接口再拉一次最新用户快照，保证单独刷新后仍能恢复完整身份信息。
      if (authSession.value?.accessToken) {
        const nextCurrentUser = await fetchCurrentUser()
        currentUser.value = nextCurrentUser
        const nextSession = {
          ...authSession.value,
          currentUser: nextCurrentUser
        }
        authSession.value = nextSession
        writeAuthSession(nextSession)
      }
      // 非静默刷新才提示成功，避免首次加载一进页就刷提示条。
      if (!options.silent) {
        setMessage('success', `${t('successPrefix')}${t('reload')}`)
      }
    } catch (error) {
      // 读取失败时回显错误，帮助用户区分是 token 失效还是工作台空数据。
      setMessage('error', resolveErrorMessage(error))
    } finally {
      // 刷新结束后立刻关闭等待态，允许用户再次操作。
      reloadPending.value = false
    }
  }

  // 登录动作负责建立统一会话、同步 locale，并直接拿首屏聚合数据进入工作台。
  async function submitLogin() {
    // 登录过程中禁止重复点击，避免生成多份并发会话。
    loginPending.value = true
    try {
      // 调用登录接口换取 token、当前用户快照和首屏工作台聚合。
      const loginOut = await loginUniauth({
        loginName: loginForm.value.loginName,
        password: loginForm.value.password
      })
      // 把登录结果里的 token 和当前用户快照写入统一会话，供宿主和请求层共同消费。
      const nextSession = {
        accessToken: loginOut.accessToken,
        currentUser: loginOut.currentUser
      }
      authSession.value = nextSession
      writeAuthSession(nextSession)
      // 登录后页面语言优先切到当前用户偏好，保证权限中心和宿主语言口径一致。
      locale.value = resolveInitialLocale(loginOut.currentUser?.locale)
      // 直接消费登录返回的首屏工作台数据，避免再额外等一轮 bootstrap 请求。
      applyWorkbench(loginOut.workbench)
      // 登录成功后立即校验 attendance 宿主桥接，确保第九阶段最小闭环一进页就可验证。
      hostContext.value = await fetchAttendanceHostContext()
      // 登录表单在成功后重置密码等输入，避免敏感信息继续停留在浏览器里。
      loginForm.value = createLoginForm()
      // 登录完成后统一提示成功，让管理员知道后续可继续编辑各区块数据。
      setMessage('success', `${t('successPrefix')}${t('signIn')}`)
    } catch (error) {
      // 登录失败时直接回显接口错误，帮助用户排查账号、密码或后端状态。
      setMessage('error', resolveErrorMessage(error))
    } finally {
      // 登录流程结束后立即关闭等待态。
      loginPending.value = false
    }
  }

  // 退出登录必须同时清理统一会话和当前页面数据，避免宿主还残留旧权限。
  function signOut() {
    // 先清理浏览器统一会话，让所有工程同步感知退出。
    clearAuthSession()
    // 再把当前工作台局部状态全部回到未登录初始值。
    resetWorkbenchState()
    // 退出提示单独保留，告诉用户当前已经回到未登录状态。
    setMessage('info', t('signOut'))
  }

  // 重新登录动作和普通退出区分开来，重点是把管理员直接带回登录页继续输入账号密码。
  function restartLogin() {
    // 先清理统一会话，确保旧 token、当前用户快照和宿主侧权限消费一起失效。
    clearAuthSession()
    // 再把当前工作台局部状态整体重置为未登录初始态，立即切回登录区块。
    resetWorkbenchState()
    // 最后用当前语言提示已经回到登录页，避免用户误以为只是普通刷新。
    setMessage('info', t('reloginReady'))
  }

  // 模块保存负责把当前模块表单转换成稳定 payload，并在成功后回刷工作台。
  async function submitModule() {
    savePending.value = true
    try {
      await saveModule({
        id: moduleForm.value.id || undefined,
        moduleCode: moduleForm.value.moduleCode,
        moduleName: moduleForm.value.moduleName,
        moduleType: moduleForm.value.moduleType,
        moduleDesc: moduleForm.value.moduleDesc,
        entryProject: moduleForm.value.entryProject,
        ownerSystem: moduleForm.value.ownerSystem,
        routeKey: moduleForm.value.routeKey,
        enabledFlag: Boolean(moduleForm.value.enabledFlag)
      })
      // 模块主数据保存后需要回刷整页聚合，保证菜单、宿主入口和摘要卡同步更新。
      await reloadWorkbench({ silent: true })
      // 回刷完成后清空模块表单，避免继续带着上一次模块误改其他工程。
      moduleForm.value = createModuleForm()
      setMessage('success', `${t('successPrefix')}${t('moduleTitle')}`)
    } catch (error) {
      setMessage('error', resolveErrorMessage(error))
    } finally {
      savePending.value = false
    }
  }

  // 租户保存负责把当前表单转换成后端稳定 payload，并在成功后回刷工作台。
  async function submitTenant() {
    savePending.value = true
    try {
      await saveTenant({
        id: tenantForm.value.id || undefined,
        tenantCode: tenantForm.value.tenantCode,
        tenantName: tenantForm.value.tenantName,
        tenantStatus: tenantForm.value.tenantStatus,
        contactName: tenantForm.value.contactName,
        contactEmail: tenantForm.value.contactEmail,
        contactPhone: tenantForm.value.contactPhone
      })
      // 保存成功后刷新整页聚合，保证摘要、列表和宿主上下文都拿到最新结果。
      await reloadWorkbench({ silent: true })
      // 回刷完成后清空表单，避免继续带着上一次保存对象误改别的租户。
      tenantForm.value = createTenantForm()
      setMessage('success', `${t('successPrefix')}${t('tenantTitle')}`)
    } catch (error) {
      setMessage('error', resolveErrorMessage(error))
    } finally {
      savePending.value = false
    }
  }

  // 用户保存负责把角色文本先归一化成 id 数组，再提交账号资料。
  async function submitUser() {
    savePending.value = true
    try {
      await saveUser({
        id: userForm.value.id || undefined,
        tenantId: Number(userForm.value.tenantId),
        loginName: userForm.value.loginName,
        password: userForm.value.password || undefined,
        displayName: userForm.value.displayName,
        displayNameKana: userForm.value.displayNameKana,
        locale: userForm.value.locale,
        email: userForm.value.email,
        phone: userForm.value.phone,
        userStatus: userForm.value.userStatus,
        roleIds: parseNumberList(userForm.value.roleIdsText)
      })
      await reloadWorkbench({ silent: true })
      userForm.value = createUserForm()
      setMessage('success', `${t('successPrefix')}${t('userTitle')}`)
    } catch (error) {
      setMessage('error', resolveErrorMessage(error))
    } finally {
      savePending.value = false
    }
  }

  // 角色保存负责把权限码和菜单码文本归一化成数组，并带上数据范围表达。
  async function submitRole() {
    savePending.value = true
    try {
      await saveRole({
        id: roleForm.value.id || undefined,
        tenantId: roleForm.value.tenantId === '' ? null : Number(roleForm.value.tenantId),
        roleCode: roleForm.value.roleCode,
        roleName: roleForm.value.roleName,
        roleDesc: roleForm.value.roleDesc,
        roleStatus: roleForm.value.roleStatus,
        permissionCodes: parseTextList(roleForm.value.permissionCodesText),
        menuCodes: parseTextList(roleForm.value.menuCodesText),
        dataScopeType: roleForm.value.dataScopeType,
        dataScopeValue: roleForm.value.dataScopeValue
      })
      await reloadWorkbench({ silent: true })
      roleForm.value = createRoleForm()
      setMessage('success', `${t('successPrefix')}${t('roleTitle')}`)
    } catch (error) {
      setMessage('error', resolveErrorMessage(error))
    } finally {
      savePending.value = false
    }
  }

  // 菜单保存负责把布尔、数字和父级 id 这些混合字段先归一化，再提交稳定菜单 payload。
  async function submitMenu() {
    savePending.value = true
    try {
      await saveMenu({
        id: menuForm.value.id || undefined,
        moduleCode: menuForm.value.moduleCode,
        menuCode: menuForm.value.menuCode,
        parentId: menuForm.value.parentId === '' ? null : Number(menuForm.value.parentId),
        menuType: menuForm.value.menuType,
        routePath: menuForm.value.routePath,
        componentName: menuForm.value.componentName,
        iconName: menuForm.value.iconName,
        sortOrder: Number(menuForm.value.sortOrder || 0),
        titleZh: menuForm.value.titleZh,
        titleJa: menuForm.value.titleJa,
        enabledFlag: Boolean(menuForm.value.enabledFlag)
      })
      await reloadWorkbench({ silent: true })
      menuForm.value = createMenuForm()
      setMessage('success', `${t('successPrefix')}${t('menuTitle')}`)
    } catch (error) {
      setMessage('error', resolveErrorMessage(error))
    } finally {
      savePending.value = false
    }
  }

  // 租户行点击后直接把主资料回填到表单，方便管理员在同一页继续修改。
  function editTenant(row) {
    tenantForm.value = createTenantForm(row)
  }

  // 用户行点击后先把基础字段和角色文本一起回填，保证账号编辑不需要手工重新查角色 id。
  function editUser(row) {
    userForm.value = createUserForm({
      ...row
    })
  }

  // 角色行点击后把权限码、菜单码和数据范围文本一起回填到表单。
  function editRole(row) {
    roleForm.value = createRoleForm({
      ...row,
      permissionCodesText: arrayToCommaText(row.permissionCodes),
      menuCodesText: arrayToCommaText(row.menuCodes)
    })
  }

  // 菜单行点击后把当前节点全部字段回填到表单，让管理员能直接修改双语标题或排序。
  function editMenu(row) {
    menuForm.value = createMenuForm(row)
  }

  // 模块行点击后直接把模块主数据回填到表单，方便管理员继续修正入口工程或路由键。
  function editModule(row) {
    moduleForm.value = createModuleForm(row)
  }

  // 当前会话变化时同步本地状态，确保宿主其他地方触发登录或退出后权限中心能立刻跟上。
  function handleAuthSessionChange(nextSession) {
    authSession.value = nextSession
    // 新会话里有 locale 时优先切换语言，否则保持当前页面语言不动。
    if (nextSession?.currentUser?.locale) {
      locale.value = resolveInitialLocale(nextSession.currentUser.locale)
    }
    // 外部把会话清空时，权限中心也要立即回未登录页面。
    if (!nextSession?.accessToken) {
      resetWorkbenchState({ keepMessage: true })
    }
  }

  // 当前页面挂载后立即订阅统一会话，并在已有 token 时恢复工作台。
  onMounted(async () => {
    unsubscribeAuthSession = subscribeAuthSession(handleAuthSessionChange)
    if (authSession.value?.accessToken) {
      await reloadWorkbench({ silent: true })
    }
  })

  // 页面卸载时移除统一会话监听，避免再次进入页面时重复执行回调。
  onBeforeUnmount(() => {
    unsubscribeAuthSession()
  })

  // 用户手工切换语言时只影响当前页面展示，不改后端用户资料。
  watch(locale, (nextLocale) => {
    locale.value = resolveInitialLocale(nextLocale)
  })

  // 对外只暴露 view 和 section 真正需要消费的状态与动作，保持组件层尽量薄。
  return {
    locale,
    localeOptions,
    authSession,
    activeSection,
    messageText,
    messageTone,
    errorDialog,
    loginPending,
    savePending,
    reloadPending,
    hostContext,
    currentUser,
    loginForm,
    moduleForm,
    tenantForm,
    userForm,
    roleForm,
    menuForm,
    moduleRows,
    tenantRows,
    userRows,
    roleRows,
    menuRows,
    permissionRows,
    navItems,
    activeSectionMeta,
    metricItems,
    currentUserEntries,
    hostContextPreview,
    permissionReferenceRows,
    t,
    reloadWorkbench,
    submitLogin,
    restartLogin,
    signOut,
    submitModule,
    submitTenant,
    submitUser,
    submitRole,
    submitMenu,
    editModule,
    editTenant,
    editUser,
    editRole,
    editMenu,
    closeErrorDialog
  }

  // 页面级消息设置统一收口，避免成功、失败和退出提示各自拼装 tone。
  function setMessage(tone, text) {
    // 失败提示统一切到弹出框，让用户在任何区块都能立即看到完整错误内容。
    if (tone === 'error') {
      messageTone.value = 'info'
      messageText.value = ''
      openErrorDialog(text)
      return
    }
    // 非错误提示不再渲染任何页内布局，只负责顺手关闭上一次错误弹出框残留状态。
    closeErrorDialog()
    messageTone.value = 'info'
    messageText.value = ''
  }

  // 页面级错误解析统一优先消费 shared request 写入的稳定错误码，再回退到原始消息。
  function resolveErrorMessage(error) {
    // 通过共享错误解析器把鉴权类异常翻译成当前语言，未知错误继续保留后端真实文案。
    return resolveLocalizedRequestError(error, t)
  }

  // 整个工作台重置时同时清理数据表、表单和宿主桥接快照，回到未登录初始状态。
  function resetWorkbenchState(options = {}) {
    authSession.value = null
    currentUser.value = null
    hostContext.value = null
    summary.value = { ...SUMMARY_DEFAULTS }
    moduleRows.value = []
    tenantRows.value = []
    userRows.value = []
    roleRows.value = []
    menuRows.value = []
    permissionRows.value = []
    moduleForm.value = createModuleForm()
    tenantForm.value = createTenantForm()
    userForm.value = createUserForm()
    roleForm.value = createRoleForm()
    menuForm.value = createMenuForm()
    loginForm.value = createLoginForm()
    closeErrorDialog()
    if (!options.keepMessage) {
      messageText.value = ''
      messageTone.value = 'info'
    }
  }

  // 打开错误弹出框时统一覆盖标题和正文，保证所有失败提示入口只维护一套交互。
  function openErrorDialog(message) {
    errorDialog.open = true
    errorDialog.title = t('errorDialogTitle')
    errorDialog.message = message
  }

  // 关闭错误弹出框时同时清空旧文案，避免下一次错误瞬间闪出上一轮内容。
  function closeErrorDialog() {
    errorDialog.open = false
    errorDialog.title = ''
    errorDialog.message = ''
  }
}

// 登录表单工厂保证每次重置都拿到全新对象，避免 v-model 继续指向旧引用。
function createLoginForm(source = {}) {
  return {
    ...LOGIN_FORM_DEFAULTS,
    ...source
  }
}

// 模块表单工厂统一负责把模块主数据回填成可编辑对象，并保持布尔字段稳定。
function createModuleForm(source = {}) {
  return {
    ...MODULE_FORM_DEFAULTS,
    ...source,
    enabledFlag: typeof source.enabledFlag === 'boolean' ? source.enabledFlag : MODULE_FORM_DEFAULTS.enabledFlag
  }
}

// 租户表单工厂统一负责把后端列表项转换成可编辑对象。
function createTenantForm(source = {}) {
  return {
    ...TENANT_FORM_DEFAULTS,
    ...source
  }
}

// 用户表单工厂统一负责把列表字段补全成完整编辑对象，并保留密码空值策略。
function createUserForm(source = {}) {
  return {
    ...USER_FORM_DEFAULTS,
    ...source,
    tenantId: source.tenantId ?? USER_FORM_DEFAULTS.tenantId,
    roleIdsText: source.roleIdsText ?? arrayToCommaText(source.roleIds)
  }
}

// 角色表单工厂统一负责把数组字段转换成逗号文本，便于管理员快速修改授权表达。
function createRoleForm(source = {}) {
  return {
    ...ROLE_FORM_DEFAULTS,
    ...source,
    tenantId: source.tenantId ?? ROLE_FORM_DEFAULTS.tenantId,
    permissionCodesText: source.permissionCodesText ?? arrayToCommaText(source.permissionCodes),
    menuCodesText: source.menuCodesText ?? arrayToCommaText(source.menuCodes)
  }
}

// 菜单表单工厂统一负责把数字和布尔字段回填成表单可编辑值。
function createMenuForm(source = {}) {
  return {
    ...MENU_FORM_DEFAULTS,
    ...source,
    parentId: source.parentId ?? MENU_FORM_DEFAULTS.parentId,
    sortOrder: source.sortOrder ?? MENU_FORM_DEFAULTS.sortOrder,
    enabledFlag: typeof source.enabledFlag === 'boolean' ? source.enabledFlag : MENU_FORM_DEFAULTS.enabledFlag
  }
}

// 初始语言解析统一支持 zh/ja 与完整 locale 两种写法，避免旧数据只存短码时切换失败。
function resolveInitialLocale(rawLocale) {
  if (rawLocale === 'ja' || rawLocale === 'ja-JP') {
    return 'ja-JP'
  }
  return 'zh-CN'
}

// 逗号文本转数组时统一去空格和空项，避免权限码、菜单码输入中出现无意义空字符串。
function parseTextList(rawText) {
  return String(rawText || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

// 逗号文本转数字数组时统一去空项并过滤非数字，避免脏输入直接传给后端。
function parseNumberList(rawText) {
  return parseTextList(rawText)
    .map((item) => Number(item))
    .filter((item) => Number.isFinite(item))
}

// 数组回填成逗号文本时统一兼容空数组，避免编辑表单展示 undefined。
function arrayToCommaText(value) {
  return Array.isArray(value) ? value.join(',') : ''
}
