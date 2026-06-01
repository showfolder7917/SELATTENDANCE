package com.sp.selfsp.layout1demo.service.impl;

import com.sp.selfsp.layout1demo.domain.out.Layout1DemoBootstrapOut;
import com.sp.selfsp.layout1demo.service.Layout1DemoService;
import java.util.List;
import org.springframework.stereotype.Service;

// 布局1号服务实现负责把“公共布局 demo”所需的所有示例数据集中组装成一个稳定出参。
@Service
public class Layout1DemoServiceImpl implements Layout1DemoService {

    // getBootstrap 把英雄栏、左栏导航、中栏摘要与表格、右栏表单和提示词引用一次性返回给前端。
    @Override
    public Layout1DemoBootstrapOut getBootstrap() {
        // 直接返回稳定聚合对象，方便前端用一条 bootstrap 请求完成整页初始化。
        return new Layout1DemoBootstrapOut(
            // layoutCode 固定命名为布局1号，方便用户和 AI 提示词统一引用。
            "布局1号",
            // heroOut 统一描述 demo 页顶部英雄栏的标题和后端链接说明。
            buildHeroOut(),
            // navItems 统一提供左侧导航栏目示例。
            buildNavItems(),
            // summaryCards 统一提供中部摘要卡示例。
            buildSummaryCards(),
            // filterPreset 统一提供筛选栏默认值。
            buildFilterPreset(),
            // tableRows 统一提供中间表格示例行。
            buildTableRows(),
            // formDraftOut 统一提供右栏表单默认值。
            buildFormDraftOut(),
            // actionCards 统一提供右栏其他处理项说明。
            buildActionCards(),
            // promptDocPath 直接把 AI 重建提示词路径回给前端，方便页面显式展示。
            "SELATTENDANCE/doc/项目架构/主要架构/示例/布局1号/布局1号_AI重建提示词_2026-06-01.md"
        );
    }

    // buildHeroOut 负责生成顶部英雄栏的主标题、副标题和接口路径提示。
    private Layout1DemoBootstrapOut.HeroOut buildHeroOut() {
        // 返回英雄栏数据，证明布局1号从一开始就包含后端链接说明。
        return new Layout1DemoBootstrapOut.HeroOut(
            // stageTag 明确告诉使用者这是中日双语和一键启动场景下的标准布局 demo。
            "中日双语 / 一键启动 / 布局1号",
            // title 直接说明当前页面是公共布局模板的可运行示例。
            "布局1号 Demo 工程",
            // lead 解释这套布局固定由英雄栏、左侧导航、中间表格和右侧表单组成。
            "把英雄栏、左侧导航、中间表格栏、右侧表单栏和其他处理项沉淀成可重建的公共布局模板。",
            // backendPath 直接回显本页连接的 bootstrap 接口路径。
            "/api/layout1-demo/bootstrap"
        );
    }

    // buildNavItems 负责生成左侧导航栏目示例，让页面能稳定演示“模块列表 + 数量”的结构。
    private List<Layout1DemoBootstrapOut.NavItemOut> buildNavItems() {
        // 返回固定导航清单，证明布局1号适合承载“列表模块切换 + 数量提醒”的工作台左栏。
        return List.of(
            // 首项用来解释如何进入示例和看懂布局结构。
            new Layout1DemoBootstrapOut.NavItemOut("guide", "初始向导", "初期ガイド", "查看这一号布局的组成、落点和复建方式。", "この一号レイアウトの構成、配置先、再構築方法を確認します。", 7),
            // 第二项演示审批入口类型的左栏模块。
            new Layout1DemoBootstrapOut.NavItemOut("approval", "审批所", "承認窓口", "集中管理需要人工处理的表格记录和动作。", "人手対応が必要な表データと操作を集中的に扱います。", 2),
            // 第三项演示主数据模块类型的左栏模块。
            new Layout1DemoBootstrapOut.NavItemOut("department", "部门", "部門", "维护与当前表格和表单相关的组织维度。", "現在の表とフォームに関係する組織軸を保守します。", 3),
            // 第四项演示人员模块类型的左栏模块。
            new Layout1DemoBootstrapOut.NavItemOut("employee", "员工", "従業員", "让右侧表单可以直接绑定人员上下文。", "右側フォームが従業員コンテキストを直接扱えるようにします。", 4),
            // 第五项演示外部导入型模块。
            new Layout1DemoBootstrapOut.NavItemOut("import", "外部接入", "外部連携", "记录导入、Webhook 和桥接类处理入口。", "取込、Webhook、ブリッジ系の処理入口を表現します。", 0),
            // 第六项演示批处理模块。
            new Layout1DemoBootstrapOut.NavItemOut("batch", "日本规则", "日本ルール", "展示批量规则、模板与统一配置的放置方式。", "一括ルール、テンプレート、共通設定の配置方法を示します。", 0),
            // 第七项演示最终业务模块。
            new Layout1DemoBootstrapOut.NavItemOut("punch", "打卡记录", "打刻記録", "第三列默认聚焦的主业务模块，适合表格 + 表单组合。", "第三列が初期フォーカスする主業務モジュールで、表 + フォーム構成に適します。", 57)
        );
    }

    // buildSummaryCards 负责生成中间区域顶部摘要卡。
    private List<Layout1DemoBootstrapOut.SummaryCardOut> buildSummaryCards() {
        // 返回四张摘要卡，证明布局1号支持“摘要统计 + 表格主区”组合。
        return List.of(
            // 已处理数量卡。
            new Layout1DemoBootstrapOut.SummaryCardOut("processed", "已处理", "処理済み", "48", "info"),
            // 未匹配数量卡。
            new Layout1DemoBootstrapOut.SummaryCardOut("unmatched", "未匹配", "未照合", "3", "warning"),
            // 失败数量卡。
            new Layout1DemoBootstrapOut.SummaryCardOut("failed", "失败", "失敗", "2", "danger"),
            // 已归档数量卡。
            new Layout1DemoBootstrapOut.SummaryCardOut("archived", "已归档", "保管済み", "4", "muted")
        );
    }

    // buildFilterPreset 负责生成筛选栏默认值。
    private Layout1DemoBootstrapOut.FilterPresetOut buildFilterPreset() {
        // 返回固定筛选默认值，证明表格栏可以被后端默认查询条件驱动。
        return new Layout1DemoBootstrapOut.FilterPresetOut(
            // dateFrom 默认展示一个完整月。
            "2026/05/01",
            // dateTo 默认展示一个完整月。
            "2026/05/31",
            // keyword 默认留空，表示等待用户输入员工或外部打卡 ID。
            "",
            // sourceSystem 默认展示全部来源。
            "ALL",
            // status 默认展示全部状态。
            "ALL"
        );
    }

    // buildTableRows 负责生成中间表格示例行。
    private List<Layout1DemoBootstrapOut.TableRowOut> buildTableRows() {
        // 返回固定表格数据，证明布局1号的中栏主区适合直接展示列表和状态徽标。
        return List.of(
            // 第一条记录模拟已处理的入场打卡。
            new Layout1DemoBootstrapOut.TableRowOut("山田太郎e0001", "KOT-90001", "2026-05-14 10:50:00", "CLOCK_IN", "WEBHOOK", "PROCESSED"),
            // 第二条记录模拟 CSV 导入的入场打卡。
            new Layout1DemoBootstrapOut.TableRowOut("山田太郎e0001", "KOT-90001", "2026-05-14 10:44:00", "CLOCK_IN", "CSV_IMPORT", "PROCESSED"),
            // 第三条记录模拟另一条已处理记录。
            new Layout1DemoBootstrapOut.TableRowOut("山田太郎e0001", "KOT-90001", "2026-05-14 10:38:00", "CLOCK_IN", "CSV_IMPORT", "PROCESSED"),
            // 第四条记录模拟出场打卡。
            new Layout1DemoBootstrapOut.TableRowOut("山田太郎e0001", "KOT-90001", "2026-05-13 09:43:00", "CLOCK_OUT", "CSV_IMPORT", "PROCESSED"),
            // 第五条记录模拟仍已处理的出场数据。
            new Layout1DemoBootstrapOut.TableRowOut("山田太郎e0001", "KOT-90001", "2026-05-13 09:37:00", "CLOCK_OUT", "CSV_IMPORT", "PROCESSED"),
            // 第六条记录模拟被忽略的数据，证明表格状态位可呈现不同处理结果。
            new Layout1DemoBootstrapOut.TableRowOut("山田太郎e0001", "KOT-90001", "2026-05-13 09:31:00", "CLOCK_OUT", "CSV_IMPORT", "IGNORED")
        );
    }

    // buildFormDraftOut 负责生成右栏表单草稿。
    private Layout1DemoBootstrapOut.FormDraftOut buildFormDraftOut() {
        // 返回右栏默认草稿，证明后端可以预填当前处理动作所需的表单上下文。
        return new Layout1DemoBootstrapOut.FormDraftOut(
            // employeeName 默认锁定到一名演示员工。
            "演示员工A",
            // punchTime 默认回到一条待补录时间。
            "2026/05/28 09:00",
            // punchType 默认展示上班打卡。
            "CLOCK_IN",
            // deviceName 默认展示补录设备。
            "管理员手动补录",
            // remark 默认说明当前动作为什么要存在于布局1号右栏。
            "右侧表单栏适合承接当前选中行的编辑、补录、审核与提交动作。"
        );
    }

    // buildActionCards 负责生成右栏其他处理项卡片。
    private List<Layout1DemoBootstrapOut.ActionCardOut> buildActionCards() {
        // 返回三张处理项卡，证明布局1号右栏不仅能放表单，还能放导入、链接和提示词引用。
        return List.of(
            // 第一张卡说明手动补录的业务用途。
            new Layout1DemoBootstrapOut.ActionCardOut("manual-entry", "手动补录", "手動補正", "可直接承接当前选中表格记录的人工修正。", "現在選択中の表レコードに対する手動補正を受け持てます。", "提交补录", "右栏表单默认就是这类动作的承载位"),
            // 第二张卡说明 CSV 导入文件的落点。
            new Layout1DemoBootstrapOut.ActionCardOut("csv-import", "CSV 导入", "CSV取込", "先预览、再正式导入，避免一次把错误数据推进主流程。", "プレビュー後に正式取込し、誤データの本流流入を防ぎます。", "attendance-punch-import.csv", "适合放在右栏“其他处理项”里作为辅助动作"),
            // 第三张卡把 AI 重建提示词和文档路径回显给用户。
            new Layout1DemoBootstrapOut.ActionCardOut("prompt-doc", "AI 重建提示词", "AI再構築プロンプト", "以后只要说“改成布局1号”，就按这份提示词重建。", "今後「レイアウト1号へ変更」と言えば、このプロンプトで再構築します。", "布局1号_AI重建提示词_2026-06-01.md", "文档已落在 SELATTENDANCE/doc/项目架构/主要架构/示例/布局1号")
        );
    }
}
