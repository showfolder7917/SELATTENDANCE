package com.sp.selfsp.layout1demo.domain.out;

import java.util.List;

// 布局1号 bootstrap 出参统一承接 demo 工程首屏需要的 hero、导航、表格、表单和提示词引用数据。
public record Layout1DemoBootstrapOut(
    // layoutCode 告诉前端当前加载的是哪套标准布局，便于宿主和文档统一命名。
    String layoutCode,
    // heroOut 承接顶部英雄栏展示的布局名称、说明和后端入口提示。
    HeroOut heroOut,
    // navItems 承接左侧导航栏目要展示的模块清单和数量。
    List<NavItemOut> navItems,
    // summaryCards 承接中间区域上方摘要卡的数值和状态色。
    List<SummaryCardOut> summaryCards,
    // filterPreset 承接中间表格栏默认筛选条件，证明布局1号支持“筛选区 + 表格区”组合。
    FilterPresetOut filterPreset,
    // tableRows 承接中间表格栏的示例数据，证明前后端表格契约怎么连接。
    List<TableRowOut> tableRows,
    // formDraftOut 承接右侧表单栏的默认值，证明前后端表单草稿可以直接对接。
    FormDraftOut formDraftOut,
    // actionCards 承接右侧“其他处理项”卡片，让 demo 也覆盖导入、提示词和联调说明。
    List<ActionCardOut> actionCards,
    // promptDocPath 明确指向 AI 重建提示词文档路径，供前端直接展示给使用者。
    String promptDocPath
) {

    // HeroOut 负责描述布局1号的顶部英雄栏正文和后端链接提示。
    public record HeroOut(
        // stageTag 用来告诉使用者当前是“中日双语 / 一键启动 / 布局1号”的示例入口。
        String stageTag,
        // title 作为顶部英雄栏主标题，直接说明 demo 的目的。
        String title,
        // lead 作为顶部英雄栏副标题，解释布局1号解决的页面编排问题。
        String lead,
        // backendPath 用来明确前端当前实际连接的后端 bootstrap 接口。
        String backendPath
    ) {}

    // NavItemOut 负责描述左侧导航栏目里每个模块的稳定键、标题、说明和数量。
    public record NavItemOut(
        // id 作为前端翻译和选中态的稳定键。
        String id,
        // titleZh 用于中文界面展示左侧导航标题。
        String titleZh,
        // titleJa 用于日文界面展示左侧导航标题。
        String titleJa,
        // leadZh 用于中文界面展示模块职责说明。
        String leadZh,
        // leadJa 用于日文界面展示模块职责说明。
        String leadJa,
        // count 用于证明左栏可同时承接模块统计数字。
        int count
    ) {}

    // SummaryCardOut 负责描述顶部摘要卡的数值、状态色和双语文案。
    public record SummaryCardOut(
        // id 作为前端摘要卡翻译与配色映射的稳定键。
        String id,
        // titleZh 用于中文界面展示摘要卡标题。
        String titleZh,
        // titleJa 用于日文界面展示摘要卡标题。
        String titleJa,
        // value 作为摘要卡主数值，证明右上角统计位怎么接后端。
        String value,
        // tone 用于前端选择不同状态卡颜色。
        String tone
    ) {}

    // FilterPresetOut 负责描述中间筛选栏的默认查询条件。
    public record FilterPresetOut(
        // dateFrom 作为开始日期默认值，证明筛选栏可以从后端回带默认区间。
        String dateFrom,
        // dateTo 作为结束日期默认值，证明筛选栏可以从后端回带默认区间。
        String dateTo,
        // keyword 作为关键字默认值，证明筛选栏支持模糊查询字段。
        String keyword,
        // sourceSystem 作为外部系统默认筛选值。
        String sourceSystem,
        // status 作为状态默认筛选值。
        String status
    ) {}

    // TableRowOut 负责描述中间表格栏的一行记录。
    public record TableRowOut(
        // employeeName 作为表格第一列，展示当前记录归属员工。
        String employeeName,
        // externalPunchId 作为表格第二列，展示外部打卡主键。
        String externalPunchId,
        // punchTime 作为表格第三列，展示打卡发生时间。
        String punchTime,
        // punchType 作为表格第四列，展示打卡类型。
        String punchType,
        // sourceSystem 作为表格第五列，展示记录来源系统。
        String sourceSystem,
        // status 作为表格第六列，展示处理状态。
        String status
    ) {}

    // FormDraftOut 负责描述右侧表单栏的默认草稿值。
    public record FormDraftOut(
        // employeeName 作为右栏第一项默认员工。
        String employeeName,
        // punchTime 作为右栏默认时间值。
        String punchTime,
        // punchType 作为右栏默认打卡类型。
        String punchType,
        // deviceName 作为右栏默认设备名。
        String deviceName,
        // remark 作为右栏默认备注，证明多行输入也能从后端回带。
        String remark
    ) {}

    // ActionCardOut 负责描述右栏“其他处理项”卡片。
    public record ActionCardOut(
        // id 作为前端翻译和排序的稳定键。
        String id,
        // titleZh 用于中文界面展示动作卡标题。
        String titleZh,
        // titleJa 用于日文界面展示动作卡标题。
        String titleJa,
        // leadZh 用于中文界面展示动作卡说明。
        String leadZh,
        // leadJa 用于日文界面展示动作卡说明。
        String leadJa,
        // primaryValue 作为动作卡主值，展示文件名、接口路径或提示词名。
        String primaryValue,
        // secondaryValue 作为动作卡次级值，展示补充说明或落点路径。
        String secondaryValue
    ) {}
}
