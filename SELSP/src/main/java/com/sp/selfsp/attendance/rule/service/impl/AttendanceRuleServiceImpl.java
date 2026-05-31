package com.sp.selfsp.attendance.rule.service.impl;

import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.employee.dao.AttendanceEmployeeDao;
import com.sp.selfsp.attendance.rule.dao.AttendanceRuleDao;
import com.sp.selfsp.attendance.rule.service.AttendanceRuleService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 第七阶段日本规则增强服务实现。
 *
 * <p>负责把正式规则配置、员工适用关系和月次预警聚合成规则工作台可直接消费的数据。</p>
 */
@Service
public class AttendanceRuleServiceImpl implements AttendanceRuleService {

    // 月残业提醒类型统一常量化，避免前后端在英文枚举上出现漂移。
    private static final String ALERT_TYPE_MONTHLY_OVERTIME = "MONTHLY_OVERTIME";
    // 年残业提醒类型独立收口，供规则页和测试共同复用。
    private static final String ALERT_TYPE_YEARLY_OVERTIME = "YEARLY_OVERTIME";
    // 有休不足提醒类型单独收口，保证看板和告警断言走同一枚举。
    private static final String ALERT_TYPE_PAID_LEAVE_REMINDER = "PAID_LEAVE_REMINDER";

    // 规则配置主数据和员工适用关系都从这里进入。
    private final AttendanceRuleDao attendanceRuleDao;
    // 绑定员工适用前要确认员工真实存在，避免孤儿规则关系。
    private final AttendanceEmployeeDao attendanceEmployeeDao;

    public AttendanceRuleServiceImpl(AttendanceRuleDao attendanceRuleDao, AttendanceEmployeeDao attendanceEmployeeDao) {
        this.attendanceRuleDao = attendanceRuleDao;
        this.attendanceEmployeeDao = attendanceEmployeeDao;
    }

    @Override
    public AttendanceOut.RuleWorkbenchOut loadWorkbench(AttendanceIn.RuleWorkbenchQueryIn queryIn) {
        // 查询月份为空时统一回到当前月，保证规则页首次打开就能看到当月风险。
        String normalizedYearMonth = normalizeYearMonth(queryIn == null ? null : queryIn.getYearMonth());
        String yearStartMonth = normalizedYearMonth.substring(0, 4) + "-01";
        List<AttendanceOut.RuleOut> rules = attendanceRuleDao.selectRuleList(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            normalizeQuery(queryIn, normalizedYearMonth)
        );
        // 员工适用列表同时带出月次和年度累计数据，后面预警就不必重复查库。
        List<AttendanceOut.EmployeeRuleAssignmentOut> assignments = attendanceRuleDao.selectAssignments(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            normalizedYearMonth,
            yearStartMonth,
            trimToNull(queryIn == null ? null : queryIn.getKeyword())
        );
        List<AttendanceOut.RuleAlertOut> alerts = buildAlerts(assignments, normalizedYearMonth);
        AttendanceOut.RuleWorkbenchOut workbenchOut = new AttendanceOut.RuleWorkbenchOut();
        // 规则主数据、适用关系、预警和摘要统一打包成一个工作台返回体，供第七阶段页面一次渲染三块内容。
        workbenchOut.setRules(rules);
        workbenchOut.setAssignments(assignments);
        workbenchOut.setAlerts(alerts);
        workbenchOut.setSummary(buildSummary(assignments, alerts));
        return workbenchOut;
    }

    @Override
    @Transactional
    public AttendanceOut.RuleOut createRule(AttendanceIn.RuleSaveIn saveIn) {
        // 新增规则前先统一校验和归一化，避免脏规则被分配给员工。
        validateRuleSaveIn(saveIn, null);
        normalizeRuleSaveIn(saveIn);
        ensureRuleCodeUnique(saveIn.getRuleCode(), null);
        attendanceRuleDao.insertRule(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        AttendanceOut.RuleOut created = attendanceRuleDao.selectRuleByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn.getRuleCode());
        if (created == null) {
            throw new IllegalStateException("规则创建后未能回读到最新数据");
        }
        return created;
    }

    @Override
    @Transactional
    public AttendanceOut.RuleOut updateRule(Long id, AttendanceIn.RuleSaveIn saveIn) {
        // 更新规则前先锁定有效主键，避免误更新不存在规则。
        validateId(id, "ruleId 必须大于 0");
        AttendanceOut.RuleOut existing = requireExistingRule(id);
        validateRuleSaveIn(saveIn, id);
        normalizeRuleSaveIn(saveIn);
        ensureRuleCodeUnique(saveIn.getRuleCode(), id);
        attendanceRuleDao.updateRule(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        AttendanceOut.RuleOut updated = attendanceRuleDao.selectRuleById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        return updated == null ? existing : updated;
    }

    @Override
    @Transactional
    public AttendanceOut.EmployeeRuleAssignmentOut assignRule(Long employeeId, AttendanceIn.RuleAssignmentSaveIn saveIn) {
        // 先锁定有效员工，再允许进入规则适用保存。
        validateId(employeeId, "employeeId 必须大于 0");
        if (attendanceEmployeeDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeId) == null) {
            throw new IllegalArgumentException("员工不存在，id=" + employeeId);
        }
        if (saveIn == null || saveIn.getRuleId() == null || saveIn.getRuleId() <= 0) {
            throw new IllegalArgumentException("ruleId 不能为空");
        }
        AttendanceOut.RuleOut ruleOut = requireExistingRule(saveIn.getRuleId());
        // 停用规则不能继续绑定，避免未来月次口径继续漂移。
        if (Boolean.FALSE.equals(ruleOut.getActiveFlag())) {
            throw new IllegalArgumentException("停用规则不能分配给员工");
        }
        LocalDate effectiveStartDate = saveIn.getEffectiveStartDate() == null ? LocalDate.now() : saveIn.getEffectiveStartDate();
        if (saveIn.getEffectiveEndDate() != null && saveIn.getEffectiveEndDate().isBefore(effectiveStartDate)) {
            throw new IllegalArgumentException("effectiveEndDate 不能早于 effectiveStartDate");
        }
        String note = trimToNull(saveIn.getNote());
        // 员工适用统一把规则关键口径复制回 employee_work_rule，供后续日次和月次计算直接复用。
        if (attendanceRuleDao.countAssignmentByEmployeeId(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeId) > 0) {
            attendanceRuleDao.updateAssignment(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                employeeId,
                ruleOut.getId(),
                ruleOut.getRuleCode(),
                ruleOut.getStandardDailyMinutes(),
                ruleOut.getStandardWeeklyMinutes(),
                effectiveStartDate,
                saveIn.getEffectiveEndDate(),
                note
            );
        } else {
            attendanceRuleDao.insertAssignment(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                employeeId,
                ruleOut.getId(),
                ruleOut.getRuleCode(),
                ruleOut.getStandardDailyMinutes(),
                ruleOut.getStandardWeeklyMinutes(),
                effectiveStartDate,
                saveIn.getEffectiveEndDate(),
                note
            );
        }
        String yearMonth = normalizeYearMonth(null);
        return attendanceRuleDao.selectAssignmentByEmployeeId(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeId,
            yearMonth,
            yearMonth.substring(0, 4) + "-01"
        );
    }

    // 规则页的查询对象统一补齐默认月份和裁剪关键字，保证 DAO 层口径稳定。
    private AttendanceIn.RuleWorkbenchQueryIn normalizeQuery(AttendanceIn.RuleWorkbenchQueryIn source, String normalizedYearMonth) {
        AttendanceIn.RuleWorkbenchQueryIn queryIn = new AttendanceIn.RuleWorkbenchQueryIn();
        // 查询月份始终回填为规范化值，避免 DAO 层再次判断空月份。
        queryIn.setYearMonth(normalizedYearMonth);
        // 关键字统一裁剪首尾空白，避免因为输入空格而查不出规则或员工。
        queryIn.setKeyword(trimToNull(source == null ? null : source.getKeyword()));
        // “只看启用规则”原样透传，保持页面筛选和服务层口径一致。
        queryIn.setActiveOnly(source == null ? null : source.getActiveOnly());
        return queryIn;
    }

    // 按员工实际适用规则和月次累计结果生成提醒，避免前端自己拼业务判断。
    private List<AttendanceOut.RuleAlertOut> buildAlerts(List<AttendanceOut.EmployeeRuleAssignmentOut> assignments, String yearMonth) {
        List<AttendanceOut.RuleAlertOut> alerts = new ArrayList<>();
        int month = YearMonth.parse(yearMonth).getMonthValue();
        for (AttendanceOut.EmployeeRuleAssignmentOut assignment : assignments) {
            // 没绑规则的员工不能产出规则预警，直接跳过避免脏告警进入看板。
            if (assignment.getRuleId() == null) {
                continue;
            }
            Integer monthlyThresholdHours = assignment.getMonthlyOvertimeAlertHours();
            if (monthlyThresholdHours != null && monthlyThresholdHours > 0) {
                // 月阈值统一换算成分钟后比较，保持与月次层 overtime_minutes 同一口径。
                int thresholdMinutes = monthlyThresholdHours * 60;
                int currentMinutes = safeInt(assignment.getMonthlyOvertimeMinutes());
                if (currentMinutes >= thresholdMinutes) {
                    // 命中阈值时生成一条高风险月残业提醒，供规则页看板直接展示。
                    alerts.add(buildMinutesAlert(assignment, yearMonth, ALERT_TYPE_MONTHLY_OVERTIME, currentMinutes, thresholdMinutes, "HIGH"));
                }
            }
            Integer yearlyThresholdHours = assignment.getYearlyOvertimeAlertHours();
            if (yearlyThresholdHours != null && yearlyThresholdHours > 0) {
                // 年阈值同样换算成分钟，与年度累计残业分钟直接比较。
                int thresholdMinutes = yearlyThresholdHours * 60;
                int currentMinutes = safeInt(assignment.getYearlyOvertimeMinutes());
                if (currentMinutes >= thresholdMinutes) {
                    // 年度累计超阈值时产出高风险年残业提醒。
                    alerts.add(buildMinutesAlert(assignment, yearMonth, ALERT_TYPE_YEARLY_OVERTIME, currentMinutes, thresholdMinutes, "HIGH"));
                }
            }
            // 有休提醒从每年后段开始给出，避免年初就对尚无取得记录的员工持续报噪音。
            if (Boolean.TRUE.equals(assignment.getPaidLeaveReminderEnabled()) && month >= 10) {
                double currentDays = safeDouble(assignment.getYearlyPaidLeaveDays());
                if (currentDays < 5D) {
                    AttendanceOut.RuleAlertOut alertOut = new AttendanceOut.RuleAlertOut();
                    // 有休提醒也挂在员工和规则主体上，保证页面能定位到具体人和适用规则。
                    alertOut.setEmployeeId(assignment.getEmployeeId());
                    alertOut.setEmployeeNo(assignment.getEmployeeNo());
                    alertOut.setEmployeeName(assignment.getEmployeeName());
                    alertOut.setRuleId(assignment.getRuleId());
                    alertOut.setRuleName(assignment.getRuleName());
                    alertOut.setAlertType(ALERT_TYPE_PAID_LEAVE_REMINDER);
                    alertOut.setAlertLevel("REMINDER");
                    alertOut.setYearMonth(yearMonth);
                    alertOut.setCurrentValueDays(currentDays);
                    alertOut.setThresholdDays(5D);
                    alerts.add(alertOut);
                }
            }
        }
        return alerts;
    }

    // 分钟型风险共用同一套提醒结构，保证月残业和年残业前端展示一致。
    private AttendanceOut.RuleAlertOut buildMinutesAlert(
        AttendanceOut.EmployeeRuleAssignmentOut assignment,
        String yearMonth,
        String alertType,
        int currentMinutes,
        int thresholdMinutes,
        String alertLevel
    ) {
        AttendanceOut.RuleAlertOut alertOut = new AttendanceOut.RuleAlertOut();
        // 提醒主体始终回写员工标识，供规则页从预警直接定位到谁超限。
        alertOut.setEmployeeId(assignment.getEmployeeId());
        alertOut.setEmployeeNo(assignment.getEmployeeNo());
        alertOut.setEmployeeName(assignment.getEmployeeName());
        // 规则标识一并回写，方便管理员知道当前风险来自哪套规则。
        alertOut.setRuleId(assignment.getRuleId());
        alertOut.setRuleName(assignment.getRuleName());
        // 提醒类型、级别、月份和数值阈值一起补齐，供前端无需再拼业务字段。
        alertOut.setAlertType(alertType);
        alertOut.setAlertLevel(alertLevel);
        alertOut.setYearMonth(yearMonth);
        alertOut.setCurrentValueMinutes(currentMinutes);
        alertOut.setThresholdMinutes(thresholdMinutes);
        return alertOut;
    }

    // 规则页头部卡片统一在服务层汇总，避免前端重复统计多份列表。
    private AttendanceOut.RuleAlertSummaryOut buildSummary(
        List<AttendanceOut.EmployeeRuleAssignmentOut> assignments,
        List<AttendanceOut.RuleAlertOut> alerts
    ) {
        int highRiskCount = 0;
        int reminderCount = 0;
        for (AttendanceOut.RuleAlertOut alert : alerts) {
            if ("HIGH".equals(alert.getAlertLevel())) {
                // 高风险包含月残业和年残业两类超限提醒，统一进入高风险计数。
                highRiskCount += 1;
            } else {
                // 其余提醒目前收口到轻提醒，供页面次级风险卡展示。
                reminderCount += 1;
            }
        }
        int boundEmployeeCount = 0;
        for (AttendanceOut.EmployeeRuleAssignmentOut assignment : assignments) {
            if (assignment.getRuleId() != null) {
                // 只有已经绑了规则的员工才计入“已绑定员工”指标。
                boundEmployeeCount += 1;
            }
        }
        AttendanceOut.RuleAlertSummaryOut summaryOut = new AttendanceOut.RuleAlertSummaryOut();
        // 摘要对象统一在服务层写完，保证头部三张卡与右侧列表来自同一批数据。
        summaryOut.setHighRiskCount(highRiskCount);
        summaryOut.setReminderCount(reminderCount);
        summaryOut.setBoundEmployeeCount(boundEmployeeCount);
        return summaryOut;
    }

    // 规则保存统一校验正式口径，避免规则层数据在前台看起来存在、后台却无法解释。
    private void validateRuleSaveIn(AttendanceIn.RuleSaveIn saveIn, Long currentId) {
        if (saveIn == null) {
            throw new IllegalArgumentException("ruleSaveIn 不能为空");
        }
        requireText(saveIn.getRuleCode(), "ruleCode 不能为空");
        requireText(saveIn.getRuleName(), "ruleName 不能为空");
        if (saveIn.getStandardDailyMinutes() == null || saveIn.getStandardDailyMinutes() <= 0) {
            throw new IllegalArgumentException("standardDailyMinutes 必须大于 0");
        }
        if (saveIn.getStandardWeeklyMinutes() == null || saveIn.getStandardWeeklyMinutes() <= 0) {
            throw new IllegalArgumentException("standardWeeklyMinutes 必须大于 0");
        }
        requireText(saveIn.getNightWorkStart(), "nightWorkStart 不能为空");
        requireText(saveIn.getNightWorkEnd(), "nightWorkEnd 不能为空");
        if (saveIn.getMonthlyOvertimeAlertHours() == null || saveIn.getMonthlyOvertimeAlertHours() <= 0) {
            throw new IllegalArgumentException("monthlyOvertimeAlertHours 必须大于 0");
        }
        if (saveIn.getYearlyOvertimeAlertHours() == null || saveIn.getYearlyOvertimeAlertHours() <= 0) {
            throw new IllegalArgumentException("yearlyOvertimeAlertHours 必须大于 0");
        }
        if (saveIn.getAutoBreakEnabled() == null) {
            throw new IllegalArgumentException("autoBreakEnabled 不能为空");
        }
        if (saveIn.getPaidLeaveReminderEnabled() == null) {
            throw new IllegalArgumentException("paidLeaveReminderEnabled 不能为空");
        }
        if (saveIn.getActiveFlag() == null) {
            throw new IllegalArgumentException("activeFlag 不能为空");
        }
        if (Boolean.TRUE.equals(saveIn.getAutoBreakEnabled())) {
            if (saveIn.getAutoBreakThresholdMinutes() == null || saveIn.getAutoBreakThresholdMinutes() <= 0) {
                throw new IllegalArgumentException("autoBreakThresholdMinutes 必须大于 0");
            }
            if (saveIn.getAutoBreakDeductMinutes() == null || saveIn.getAutoBreakDeductMinutes() <= 0) {
                throw new IllegalArgumentException("autoBreakDeductMinutes 必须大于 0");
            }
        }
        if (StringUtils.hasText(saveIn.getRoundingMode())
            && saveIn.getRoundingUnitMinutes() != null
            && saveIn.getRoundingUnitMinutes() <= 0) {
            throw new IllegalArgumentException("roundingUnitMinutes 必须大于 0");
        }
        if (currentId != null) {
            validateId(currentId, "ruleId 必须大于 0");
        }
    }

    // 规则保存前统一裁剪文本并补默认值，避免展示和唯一性判断漂移。
    private void normalizeRuleSaveIn(AttendanceIn.RuleSaveIn saveIn) {
        saveIn.setRuleCode(saveIn.getRuleCode().trim());
        saveIn.setRuleName(saveIn.getRuleName().trim());
        saveIn.setNightWorkStart(saveIn.getNightWorkStart().trim());
        saveIn.setNightWorkEnd(saveIn.getNightWorkEnd().trim());
        saveIn.setRoundingMode(trimToNull(saveIn.getRoundingMode()));
        saveIn.setNote(trimToNull(saveIn.getNote()));
        if (Boolean.FALSE.equals(saveIn.getAutoBreakEnabled())) {
            // 自动休息关闭时把阈值和扣休分钟统一清零，避免旧值继续误导页面。
            saveIn.setAutoBreakThresholdMinutes(0);
            saveIn.setAutoBreakDeductMinutes(0);
        }
    }

    // 规则编码在租户内必须唯一，避免员工适用和审计记录指向混乱。
    private void ensureRuleCodeUnique(String ruleCode, Long currentId) {
        AttendanceOut.RuleOut existing = attendanceRuleDao.selectRuleByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, ruleCode.trim());
        if (existing == null) {
            return;
        }
        if (currentId != null && currentId.equals(existing.getId())) {
            return;
        }
        throw new IllegalArgumentException("ruleCode 已存在");
    }

    // 统一读取正式规则，不存在时立即拦截。
    private AttendanceOut.RuleOut requireExistingRule(Long id) {
        AttendanceOut.RuleOut ruleOut = attendanceRuleDao.selectRuleById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        if (ruleOut == null) {
            throw new IllegalArgumentException("规则不存在，id=" + id);
        }
        return ruleOut;
    }

    // 月份统一按 yyyy-MM 规格处理，避免前后端口径不一致。
    private String normalizeYearMonth(String yearMonth) {
        if (!StringUtils.hasText(yearMonth)) {
            return YearMonth.now().toString();
        }
        return YearMonth.parse(yearMonth.trim()).toString();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0D : value;
    }

    private void validateId(Long id, String message) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
