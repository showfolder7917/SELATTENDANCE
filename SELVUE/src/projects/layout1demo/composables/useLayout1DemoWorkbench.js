// computed 用来把当前语言和后台数据映射成页面直接可消费的展示结构。
import { computed, onMounted, ref } from 'vue'
// useAttendanceTheme 直接复用现成主题注入逻辑，保证布局1号 demo 也能正确挂上共享深色变量。
import { useAttendanceTheme } from '../../attendance/composables/useAttendanceTheme'
// fetchLayout1DemoBootstrap 负责优先读取真实后端接口，buildLayout1DemoFallback 负责在本地后端未刷新时继续稳定展示布局。
import { buildLayout1DemoFallback, fetchLayout1DemoBootstrap } from '../services/layout1DemoApi'

// MESSAGE_CATALOG 负责承接布局1号 demo 的中日双语静态壳文案。
const MESSAGE_CATALOG = {
  // zh 负责中文界面的标题、按钮和区块文案。
  zh: {
    heroLabel: '公共布局 / Demo 工程',
    backendLink: '后端链接',
    promptDoc: '提示词文档',
    sourceBackend: '真实后端',
    sourceFallback: '本地回退',
    layoutGuide: '布局1号固定结构',
    layoutGuideLead: '上方英雄栏 + 左侧导航栏目 + 中间表格栏 + 右侧表单栏 / 其他处理项',
    navTitle: '功能导航',
    navLead: '左侧固定承接模块列表、数量和切换说明。',
    tableTitle: '主表格区',
    tableLead: '中间主区域固定由摘要卡、筛选栏、表格和分页说明组成。',
    formTitle: '右侧表单栏',
    formLead: '右栏优先承接当前选中记录的编辑、补录、审核和提交。',
    actionsTitle: '其他处理项',
    actionsLead: '导入、提示词、联调入口和其他补充动作统一放在右栏下半区。',
    rebuildHint: '以后如果你直接说“改成布局1号”，AI 就按这个 demo 和提示词重建。',
    columnsEmployee: '员工姓名',
    columnsExternalId: '外部打卡 ID',
    columnsPunchTime: '打卡时间',
    columnsPunchType: '打卡类型',
    columnsSourceSystem: '外部系统',
    columnsStatus: '状态',
    filterDateFrom: '开始日期',
    filterDateTo: '结束日期',
    filterKeyword: '员工姓名 / 外部打卡 ID',
    filterSourceSystem: '外部系统',
    filterStatus: '状态',
    formEmployee: '员工姓名',
    formPunchTime: '打卡时间',
    formPunchType: '打卡类型',
    formDeviceName: '设备名称',
    formRemark: '补充备注'
  },
  // ja 负责日文界面的标题、按钮和区块文案。
  ja: {
    heroLabel: '共通レイアウト / Demo 工程',
    backendLink: 'バックエンド接続',
    promptDoc: 'プロンプト文書',
    sourceBackend: '実バックエンド',
    sourceFallback: 'ローカルフォールバック',
    layoutGuide: 'レイアウト1号の固定構成',
    layoutGuideLead: '上部ヒーロー欄 + 左ナビ欄 + 中央テーブル欄 + 右フォーム欄 / 補助処理欄',
    navTitle: '機能ナビ',
    navLead: '左側はモジュール一覧、件数、切替説明を固定で受け持ちます。',
    tableTitle: '主テーブル欄',
    tableLead: '中央主領域はサマリーカード、検索欄、テーブル、ページ情報で固定します。',
    formTitle: '右フォーム欄',
    formLead: '右欄は選択中レコードの編集、補正、承認、送信を優先的に受け持ちます。',
    actionsTitle: 'その他処理項目',
    actionsLead: '取込、プロンプト、連携入口などの補助動作は右欄下部へ集約します。',
    rebuildHint: '今後「レイアウト1号へ変更」と言えば、AI はこの demo と提示詞で再構築します。',
    columnsEmployee: '従業員名',
    columnsExternalId: '外部打刻 ID',
    columnsPunchTime: '打刻時間',
    columnsPunchType: '打刻種別',
    columnsSourceSystem: '外部システム',
    columnsStatus: '状態',
    filterDateFrom: '開始日',
    filterDateTo: '終了日',
    filterKeyword: '従業員名 / 外部打刻 ID',
    filterSourceSystem: '外部システム',
    filterStatus: '状態',
    formEmployee: '従業員名',
    formPunchTime: '打刻時間',
    formPunchType: '打刻種別',
    formDeviceName: 'デバイス名',
    formRemark: '補足メモ'
  }
}

// LOCALE_OPTIONS 负责定义 demo 页的中日切换选项。
const LOCALE_OPTIONS = [
  // 中文按钮负责在当前演示页切回中文说明。
  { value: 'zh', label: '中文', short: 'CN' },
  // 日文按钮负责在当前演示页切回日文说明。
  { value: 'ja', label: '日本語', short: 'JP' }
]

// useLayout1DemoWorkbench 把后端 bootstrap、主题切换和双语壳文案统一组织成 demo 页状态入口。
export function useLayout1DemoWorkbench() {
  // locale 记录当前页面展示语言。
  const locale = ref('zh')
  // themeId 和 themeOptions 直接复用 attendance 的主题挂载能力，避免 demo 页只切按钮不切真实主题样式。
  const { themeId, themeOptions } = useAttendanceTheme()
  // bootstrapData 保存当前页面真正渲染用的聚合后端数据。
  const bootstrapData = ref(buildLayout1DemoFallback())
  // dataSource 保存当前数据来自真实后端还是 fallback，方便页面给出联调状态提示。
  const dataSource = ref('fallback')
  // loadError 保存本地读取真实后端失败时的错误信息。
  const loadError = ref('')

  // t 负责按当前语言读取静态壳文案。
  const t = (key) => MESSAGE_CATALOG[locale.value]?.[key] || MESSAGE_CATALOG.zh[key] || key

  // heroOut 直接暴露顶部英雄栏所需数据。
  const heroOut = computed(() => bootstrapData.value.heroOut)
  // navItems 直接暴露左侧导航清单。
  const navItems = computed(() => bootstrapData.value.navItems || [])
  // summaryCards 直接暴露中栏摘要卡数据。
  const summaryCards = computed(() => bootstrapData.value.summaryCards || [])
  // filterPreset 直接暴露筛选栏默认值。
  const filterPreset = computed(() => bootstrapData.value.filterPreset || {})
  // tableRows 直接暴露中栏表格数据。
  const tableRows = computed(() => bootstrapData.value.tableRows || [])
  // formDraftOut 直接暴露右栏表单默认值。
  const formDraftOut = computed(() => bootstrapData.value.formDraftOut || {})
  // actionCards 直接暴露右栏其他处理项列表。
  const actionCards = computed(() => bootstrapData.value.actionCards || [])
  // promptDocPath 直接暴露提示词文档路径。
  const promptDocPath = computed(() => bootstrapData.value.promptDocPath || '')

  // sourceLabel 把真实后端和 fallback 状态映射成用户可读标签。
  const sourceLabel = computed(() => (dataSource.value === 'backend' ? t('sourceBackend') : t('sourceFallback')))

  // tableColumns 统一把中栏表头定义成当前语言下的展示结构。
  const tableColumns = computed(() => [
    // 第一列展示员工姓名。
    { key: 'employeeName', label: t('columnsEmployee') },
    // 第二列展示外部打卡主键。
    { key: 'externalPunchId', label: t('columnsExternalId') },
    // 第三列展示打卡时间。
    { key: 'punchTime', label: t('columnsPunchTime') },
    // 第四列展示打卡类型。
    { key: 'punchType', label: t('columnsPunchType') },
    // 第五列展示外部来源。
    { key: 'sourceSystem', label: t('columnsSourceSystem') },
    // 第六列展示处理状态。
    { key: 'status', label: t('columnsStatus') }
  ])

  // formFields 统一把右栏表单展示成“标签 + 值”的只读演示结构。
  const formFields = computed(() => [
    // 第一行展示当前表单绑定员工。
    { label: t('formEmployee'), value: formDraftOut.value.employeeName || '' },
    // 第二行展示当前表单默认时间。
    { label: t('formPunchTime'), value: formDraftOut.value.punchTime || '' },
    // 第三行展示当前表单默认打卡类型。
    { label: t('formPunchType'), value: formDraftOut.value.punchType || '' },
    // 第四行展示当前表单默认设备名。
    { label: t('formDeviceName'), value: formDraftOut.value.deviceName || '' },
    // 第五行展示当前表单默认备注。
    { label: t('formRemark'), value: formDraftOut.value.remark || '' }
  ])

  // loadBootstrap 在页面挂载时优先尝试真实后端，并在失败时平滑切回 fallback。
  async function loadBootstrap() {
    // 每次发起请求前先清空上次错误，避免旧失败提示残留。
    loadError.value = ''
    // 优先尝试真实后端接口，证明布局1号已经有正式前后台链接。
    try {
      // 成功时直接用真实接口结果初始化整页结构。
      bootstrapData.value = await fetchLayout1DemoBootstrap()
      // 数据来源标记成真实后端，方便页面显式说明当前联调状态。
      dataSource.value = 'backend'
    } catch (error) {
      // 后端暂不可用时退回本地同结构示例数据，保证 demo 依然能讲清楚布局形态。
      bootstrapData.value = buildLayout1DemoFallback()
      // 数据来源标记成 fallback，提示使用者当前只是前端展示回退。
      dataSource.value = 'fallback'
      // 把失败原因保留下来，方便右栏或英雄栏补联调说明。
      loadError.value = error?.message || 'layout1 demo bootstrap unavailable'
    }
  }

  // 组件挂载后立即读取 bootstrap，保证 demo 首屏打开就有完整结构。
  onMounted(() => {
    // 首屏加载动作统一走 loadBootstrap，避免模板层直接触发副作用。
    loadBootstrap()
  })

  // 把当前 demo 页真正需要消费的状态、动作和文案入口统一暴露给视图层。
  return {
    // locale 供 LanguageSwitch 双向绑定当前语言。
    locale,
    // localeOptions 供 LanguageSwitch 展示中日按钮。
    localeOptions: LOCALE_OPTIONS,
    // themeId 供 ThemeSwitch 双向绑定当前主题。
    themeId,
    // themeOptions 供 ThemeSwitch 展示共享主题清单。
    themeOptions,
    // sourceLabel 供页面展示当前数据来源是后端还是 fallback。
    sourceLabel,
    // loadError 供页面展示后端暂不可用时的补充说明。
    loadError,
    // heroOut 供顶部英雄栏直接渲染。
    heroOut,
    // navItems 供左侧导航栏目渲染。
    navItems,
    // summaryCards 供中栏摘要卡渲染。
    summaryCards,
    // filterPreset 供中栏筛选栏渲染默认值。
    filterPreset,
    // tableColumns 供中栏表格头部渲染。
    tableColumns,
    // tableRows 供中栏表格主体渲染。
    tableRows,
    // formFields 供右栏表单只读演示区渲染。
    formFields,
    // actionCards 供右栏其他处理项渲染。
    actionCards,
    // promptDocPath 供右栏直接展示提示词文档路径。
    promptDocPath,
    // t 供视图层读取当前语言壳文案。
    t,
    // loadBootstrap 供后续如果要加“刷新 demo 数据”按钮时继续复用。
    loadBootstrap
  }
}
