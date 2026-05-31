/*
 * AttendanceBootstrapServiceImpl.java
 * 轻量首页壳服务实现。
 */
package com.sp.selfsp.attendance.bootstrap.service.impl;

import com.sp.selfsp.attendance.bootstrap.dao.AttendanceBootstrapDao;
import com.sp.selfsp.attendance.bootstrap.service.AttendanceBootstrapService;
import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.tenant.service.AttendanceTenantService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * 轻量首页壳服务实现。
 */
@Service
public class AttendanceBootstrapServiceImpl implements AttendanceBootstrapService {

    // 读取首页壳统计计数，供步骤卡片和下一动作判断复用。
    private final AttendanceBootstrapDao attendanceBootstrapDao;
    // 读取当前租户资料，供首页壳和租户面板初始化保持同源。
    private final AttendanceTenantService attendanceTenantService;

    // 注入首页壳统计 DAO 和租户服务，避免 bootstrap 直接侵入多个子域 DAO。
    public AttendanceBootstrapServiceImpl(
        AttendanceBootstrapDao attendanceBootstrapDao,
        AttendanceTenantService attendanceTenantService
    ) {
        // 保存首页壳统计 DAO，供步骤计数和阶段门禁判断复用。
        this.attendanceBootstrapDao = attendanceBootstrapDao;
        // 保存租户服务，供首页壳直接读取当前租户资料。
        this.attendanceTenantService = attendanceTenantService;
    }

    // 返回轻量首页壳数据，只保留租户摘要、步骤状态和推荐下一动作。
    @Override
    public AttendanceOut.BootstrapSummaryOut getBootstrapSummary() {
        // 读取默认租户下的首页壳计数，供步骤状态和推荐动作统一判断。
        Map<String, Object> counts = attendanceBootstrapDao.selectCounts(AttendanceTenantContext.DEFAULT_TENANT_ID);
        // 初始化首页壳返回对象，承载轻量初始化所需字段。
        AttendanceOut.BootstrapSummaryOut summaryOut = new AttendanceOut.BootstrapSummaryOut();
        // 回填当前租户资料，供首页租户面板直接渲染。
        summaryOut.setTenant(attendanceTenantService.getCurrentTenant());
        // 初始化首页步骤集合，后续按阶段顺序构建壳层引导。
        List<AttendanceOut.BootstrapStepOut> steps = new ArrayList<>();
        // 构建租户初始化步骤，提示是否已建立当前租户基础资料。
        steps.add(buildStep("tenant", "wizard.tenant", countAsInt(counts, "tenantCount"), "guide.tenant"));
        // 构建场所初始化步骤，提示是否已建立工作场所主数据。
        steps.add(buildStep("workplace", "wizard.workplace", countAsInt(counts, "workplaceCount"), "guide.workplace"));
        // 构建员工初始化步骤，提示是否已建立员工主数据。
        steps.add(buildStep("employee", "wizard.employee", countAsInt(counts, "employeeCount"), "guide.employee"));
        // 构建班次模板步骤，提示是否已建立可排班模板。
        steps.add(buildStep("shiftTemplate", "wizard.shiftTemplate", countAsInt(counts, "shiftTemplateCount"), "guide.shiftTemplate"));
        // 构建工时规则步骤，提示是否已补齐排班前置规则数据。
        steps.add(buildStep("workRule", "wizard.workRule", countAsInt(counts, "workRuleCount"), "guide.workRule"));
        // 计算排班阶段是否满足门禁条件，避免前置主数据缺失时误导进入排班。
        boolean scheduleReady = isScheduleReady(counts);
        // 构建排班步骤，并根据门禁状态决定显示可操作还是锁定。
        steps.add(buildPhaseGateStep("schedule", "wizard.schedule", countAsInt(counts, "scheduleCount"), "guide.schedule", scheduleReady));
        // 构建打卡阶段门禁步骤，仅在已存在排班数据时开放第三阶段打卡接收与原始数据管理。
        steps.add(buildPhaseGateStep("punch", "wizard.punch", countAsInt(counts, "punchCount"), "guide.punch", countAsInt(counts, "scheduleCount") > 0));
        // 把步骤列表回填到首页壳结果，供前端工作台导航和向导复用。
        summaryOut.setSteps(steps);
        // 计算推荐下一动作，供首页侧边栏直接提示当前最该完成的步骤。
        summaryOut.setRecommendedNextAction(resolveRecommendedNextAction(steps));
        // 返回轻量首页壳结果，避免再携带各 section 的大列表数据。
        return summaryOut;
    }

    // 构建普通步骤状态，供首页壳展示基础主数据是否已完成初始化。
    private AttendanceOut.BootstrapStepOut buildStep(String stepCode, String titleKey, int count, String description) {
        // 初始化步骤结果对象，承载步骤码、标题、计数和状态。
        AttendanceOut.BootstrapStepOut stepOut = new AttendanceOut.BootstrapStepOut();
        // 写入步骤编码，供前端定位到对应 section。
        stepOut.setStepCode(stepCode);
        // 写入步骤标题 key，供前端按当前语言环境翻译显示。
        stepOut.setTitleKey(titleKey);
        // 写入步骤计数，供前端展示已完成量级。
        stepOut.setCount(count);
        // 写入步骤说明 key，供工作台展示引导文案。
        stepOut.setDescription(description);
        // 根据计数判断步骤是否已具备基础完成状态。
        stepOut.setStatus(count > 0 ? "COMPLETED" : "NEEDS_ACTION");
        // 返回普通步骤结果，供首页壳汇总。
        return stepOut;
    }

    // 构建带阶段门禁的步骤状态，供排班和后续阶段按前置条件解锁。
    private AttendanceOut.BootstrapStepOut buildPhaseGateStep(String stepCode, String titleKey, int count, String description, boolean ready) {
        // 初始化带门禁步骤结果对象，承载阶段步骤的展示状态。
        AttendanceOut.BootstrapStepOut stepOut = new AttendanceOut.BootstrapStepOut();
        // 写入步骤编码，供前端把门禁步骤映射到对应区块。
        stepOut.setStepCode(stepCode);
        // 写入步骤标题 key，供前端多语言显示阶段名称。
        stepOut.setTitleKey(titleKey);
        // 写入步骤计数，供前端展示当前阶段已有记录数量。
        stepOut.setCount(count);
        // 写入步骤说明 key，供前端展示阶段说明文案。
        stepOut.setDescription(description);
        // 前置条件不满足时直接锁定阶段，避免误导用户进入下一阶段。
        if (!ready) {
            // 标记为下一阶段锁定状态，供前端展示为不可操作。
            stepOut.setStatus("LOCKED_NEXT_PHASE");
        } else {
            // 已解锁阶段再按是否已有数据判断为已完成或待处理。
            stepOut.setStatus(count > 0 ? "COMPLETED" : "NEEDS_ACTION");
        }
        // 返回门禁步骤结果，供首页壳汇总。
        return stepOut;
    }

    // 解析推荐下一动作，优先返回第一个仍待处理的步骤标题 key。
    private String resolveRecommendedNextAction(List<AttendanceOut.BootstrapStepOut> steps) {
        // 顺序扫描步骤列表，保持推荐动作与首页壳步骤顺序一致。
        for (AttendanceOut.BootstrapStepOut step : steps) {
            // 找到第一个待处理步骤时立即返回，作为工作台当前主引导。
            if (Objects.equals("NEEDS_ACTION", step.getStatus())) {
                // 返回对应标题 key，供前端直接翻译成推荐动作文案。
                return step.getTitleKey();
            }
        }
        // 全部基础步骤已完成时默认指向排班步骤，作为后续操作入口。
        return "wizard.schedule";
    }

    // 安全读取计数字段，兼容不同数据库驱动返回的 key 大小写差异。
    private int countAsInt(Map<String, Object> counts, String key) {
        // 先按原始 key 读取统计值，适配常规 MyBatis 返回结果。
        Object value = counts.get(key);
        // 原始 key 未命中时再尝试大写 key，兼容 H2 等驱动行为差异。
        if (value == null) {
            // 使用大写 key 再读一次，避免测试环境与生产环境统计口径不一致。
            value = counts.get(key.toUpperCase());
        }
        // 命中数值类型时转换成 int，供步骤状态判断和展示复用。
        if (value instanceof Number number) {
            // 返回标准 int 计数，供后续步骤统一计算状态。
            return number.intValue();
        }
        // 未命中或类型异常时按 0 处理，避免首页壳初始化报错。
        return 0;
    }

    // 判断排班阶段是否已满足前置主数据条件。
    private boolean isScheduleReady(Map<String, Object> counts) {
        // 只有租户、场所、员工、班次模板和工时规则都齐备时才开放排班阶段。
        return countAsInt(counts, "tenantCount") > 0
            && countAsInt(counts, "workplaceCount") > 0
            && countAsInt(counts, "employeeCount") > 0
            && countAsInt(counts, "shiftTemplateCount") > 0
            && countAsInt(counts, "workRuleCount") > 0;
    }
}
