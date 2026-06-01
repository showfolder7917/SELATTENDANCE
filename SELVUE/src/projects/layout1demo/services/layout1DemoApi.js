// requestJson 统一承接 demo 工程对后端 bootstrap 接口的访问和错误处理。
import { requestJson } from '../../../shared/services/request'

// fetchLayout1DemoBootstrap 先尝试读取真实后端 demo 数据，证明布局1号有正式前后台链接。
export const fetchLayout1DemoBootstrap = () => requestJson('/api/layout1-demo/bootstrap')

// buildLayout1DemoFallback 在本地后端未重启或接口暂不可用时提供稳定回退，让 demo 页面仍可直接打开讲解布局。
export function buildLayout1DemoFallback() {
  // 回退对象保持和后端正式契约同结构，避免前端为了 fallback 再维护第二套页面模型。
  return {
    // layoutCode 继续固定写成布局1号，保持和正式接口一致。
    layoutCode: '布局1号',
    // heroOut 保留顶部英雄栏需要的标题、副标题和接口路径说明。
    heroOut: {
      // stageTag 说明当前仍是中日双语和一键启动场景下的布局模板。
      stageTag: '中日双语 / 一键启动 / 布局1号',
      // title 直接告诉用户这是 demo 工程。
      title: '布局1号 Demo 工程',
      // lead 解释这套布局由英雄栏、导航、表格和表单组成。
      lead: '前端优先尝试真实后端 bootstrap；若本地后端未重启，则回退到同结构示例数据继续展示布局。',
      // backendPath 仍明确指出正式对接接口地址。
      backendPath: '/api/layout1-demo/bootstrap'
    },
    // navItems 保留左栏模块示例，让 fallback 和正式后端展示同一布局结构。
    navItems: [
      // guide 项负责说明如何看懂布局1号。
      { id: 'guide', titleZh: '初始向导', titleJa: '初期ガイド', leadZh: '查看布局1号的构成、落点和复建方式。', leadJa: 'レイアウト1号の構成、配置先、再構築方法を確認します。', count: 7 },
      // approval 项演示审批类左栏模块。
      { id: 'approval', titleZh: '审批所', titleJa: '承認窓口', leadZh: '集中处理需要人工判断的表格记录。', leadJa: '人手判断が必要な表データを集中的に扱います。', count: 2 },
      // department 项演示组织主数据类左栏模块。
      { id: 'department', titleZh: '部门', titleJa: '部門', leadZh: '维护和当前主表、右栏表单相关的组织维度。', leadJa: '主テーブルと右フォームに関係する組織軸を保守します。', count: 3 },
      // employee 项演示人员主数据类左栏模块。
      { id: 'employee', titleZh: '员工', titleJa: '従業員', leadZh: '把主表和右栏处理动作绑定到具体员工。', leadJa: '主表と右側アクションを具体的な従業員に結び付けます。', count: 4 },
      // import 项演示外部接入模块。
      { id: 'import', titleZh: '外部接入', titleJa: '外部連携', leadZh: '用于承接 CSV、Webhook 和桥接处理。', leadJa: 'CSV、Webhook、ブリッジ処理を受け持ちます。', count: 0 },
      // batch 项演示规则与批处理类模块。
      { id: 'batch', titleZh: '日本规则', titleJa: '日本ルール', leadZh: '展示规则、模板和批处理配置的放置方式。', leadJa: 'ルール、テンプレート、一括処理設定の配置方法を示します。', count: 0 },
      // punch 项作为默认主模块，展示左中右三栏如何配合。
      { id: 'punch', titleZh: '打卡记录', titleJa: '打刻記録', leadZh: '第三列默认聚焦的主业务模块，适合表格 + 表单组合。', leadJa: '第三列が初期フォーカスする主業務モジュールで、表 + フォーム構成に適します。', count: 57 }
    ],
    // summaryCards 保留中栏顶部摘要卡示例。
    summaryCards: [
      // processed 卡片表示已处理数量。
      { id: 'processed', titleZh: '已处理', titleJa: '処理済み', value: '48', tone: 'info' },
      // unmatched 卡片表示未匹配数量。
      { id: 'unmatched', titleZh: '未匹配', titleJa: '未照合', value: '3', tone: 'warning' },
      // failed 卡片表示失败数量。
      { id: 'failed', titleZh: '失败', titleJa: '失敗', value: '2', tone: 'danger' },
      // archived 卡片表示已归档数量。
      { id: 'archived', titleZh: '已归档', titleJa: '保管済み', value: '4', tone: 'muted' }
    ],
    // filterPreset 保留表格筛选栏默认值。
    filterPreset: {
      // dateFrom 作为默认开始日期。
      dateFrom: '2026/05/01',
      // dateTo 作为默认结束日期。
      dateTo: '2026/05/31',
      // keyword 默认留空。
      keyword: '',
      // sourceSystem 默认展示全部来源。
      sourceSystem: 'ALL',
      // status 默认展示全部状态。
      status: 'ALL'
    },
    // tableRows 保留中栏表格示例数据。
    tableRows: [
      // 第一行模拟已处理记录。
      { employeeName: '山田太郎e0001', externalPunchId: 'KOT-90001', punchTime: '2026-05-14 10:50:00', punchType: 'CLOCK_IN', sourceSystem: 'WEBHOOK', status: 'PROCESSED' },
      // 第二行模拟 CSV 导入记录。
      { employeeName: '山田太郎e0001', externalPunchId: 'KOT-90001', punchTime: '2026-05-14 10:44:00', punchType: 'CLOCK_IN', sourceSystem: 'CSV_IMPORT', status: 'PROCESSED' },
      // 第三行继续展示已处理记录。
      { employeeName: '山田太郎e0001', externalPunchId: 'KOT-90001', punchTime: '2026-05-14 10:38:00', punchType: 'CLOCK_IN', sourceSystem: 'CSV_IMPORT', status: 'PROCESSED' },
      // 第四行演示出场打卡记录。
      { employeeName: '山田太郎e0001', externalPunchId: 'KOT-90001', punchTime: '2026-05-13 09:43:00', punchType: 'CLOCK_OUT', sourceSystem: 'CSV_IMPORT', status: 'PROCESSED' }
    ],
    // formDraftOut 保留右栏表单默认值。
    formDraftOut: {
      // employeeName 作为默认员工字段。
      employeeName: '演示员工A',
      // punchTime 作为默认打卡时间。
      punchTime: '2026/05/28 09:00',
      // punchType 作为默认打卡类型。
      punchType: 'CLOCK_IN',
      // deviceName 作为默认设备名。
      deviceName: '管理员手动补录',
      // remark 作为默认备注说明。
      remark: '右侧表单栏适合承接当前选中行的编辑、补录、审核与提交动作。'
    },
    // actionCards 保留右栏其他处理项说明。
    actionCards: [
      // manual-entry 卡说明右栏主表单的用途。
      { id: 'manual-entry', titleZh: '手动补录', titleJa: '手動補正', leadZh: '可直接承接当前选中表格记录的人工修正。', leadJa: '現在選択中の表レコードに対する手動補正を受け持てます。', primaryValue: '提交补录', secondaryValue: '右栏主表单默认就是这类动作的承载位' },
      // csv-import 卡说明右栏附加动作用途。
      { id: 'csv-import', titleZh: 'CSV 导入', titleJa: 'CSV取込', leadZh: '先预览、再正式导入，避免一次把错误数据推进主流程。', leadJa: 'プレビュー後に正式取込し、誤データの本流流入を防ぎます。', primaryValue: 'attendance-punch-import.csv', secondaryValue: '适合放在右栏“其他处理项”里作为辅助动作' },
      // prompt-doc 卡说明 AI 重建提示词用途。
      { id: 'prompt-doc', titleZh: 'AI 重建提示词', titleJa: 'AI再構築プロンプト', leadZh: '以后只要说“改成布局1号”，就按这份提示词重建。', leadJa: '今後「レイアウト1号へ変更」と言えば、このプロンプトで再構築します。', primaryValue: '布局1号_AI重建提示词_2026-06-01.md', secondaryValue: '文档已落在 SELATTENDANCE/doc/项目架构/主要架构/示例/布局1号' }
    ],
    // promptDocPath 保持正式文档路径，方便右栏直接回显。
    promptDocPath: 'SELATTENDANCE/doc/项目架构/主要架构/示例/布局1号/布局1号_AI重建提示词_2026-06-01.md'
  }
}
