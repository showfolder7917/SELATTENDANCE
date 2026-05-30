package com.sp.selfsp.attendance.daily.service.impl;

import com.sp.selfsp.attendance.daily.domain.out.AttendanceDailyOut;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.util.StringUtils;

/**
 * 第七阶段日次规则共享计算器。
 *
 * <p>负责把原始打卡初算与审批后 final 结果重算统一到同一套分钟口径里，避免两条链路各自维护一份规则算法。</p>
 */
final class AttendanceDailyMetricCalculator {

    // 法定休日使用统一常量，避免日次、月次和预警层出现多种拼写。
    static final String HOLIDAY_TYPE_LEGAL = "LEGAL_HOLIDAY";
    // 所定休日使用统一常量，保证休日工时统计口径一致。
    static final String HOLIDAY_TYPE_SCHEDULED = "SCHEDULED_HOLIDAY";

    // 根据排班、原始打卡和正式规则产出完整日次结果，供第四阶段初算与第七阶段增强口径共用。
    CalculationResult calculateFromPunches(
        AttendanceDailyOut.ScheduleSnapshotOut schedule,
        List<AttendanceDailyOut.PunchSnapshotOut> punches,
        Map<String, Object> applicableRule
    ) {
        CalculationResult result = buildBaseResult(schedule, applicableRule);
        result.logs.add(schedule == null ? "当天没有排班快照，按无排班逻辑进入计算。" : "已读取当天排班快照：" + defaultText(schedule.getLabel(), "未命名班次"));
        result.logs.add("当天有效打卡数量为 " + punches.size() + " 条。");
        if (result.appliedRuleId != null) {
            result.logs.add("已命中正式规则：" + defaultText(result.appliedRuleName, stringValue(readMapValue(applicableRule, "ruleCode"))) + "。");
        } else {
            result.logs.add("当天未命中正式规则，按排班基础口径继续计算。");
        }

        AttendanceDailyOut.PunchSnapshotOut firstClockIn = null;
        AttendanceDailyOut.PunchSnapshotOut lastClockOut = null;
        List<AttendanceDailyOut.PunchSnapshotOut> breakStarts = new ArrayList<>();
        List<AttendanceDailyOut.PunchSnapshotOut> breakEnds = new ArrayList<>();
        for (AttendanceDailyOut.PunchSnapshotOut punch : punches) {
            // 上班卡统一挑最早一条，确保迟到和工作起点都基于同一标准。
            if (Objects.equals("CLOCK_IN", punch.getPunchType())) {
                if (firstClockIn == null || punch.getPunchTime().isBefore(firstClockIn.getPunchTime())) {
                    firstClockIn = punch;
                }
            } else if (Objects.equals("CLOCK_OUT", punch.getPunchType())) {
                // 下班卡统一挑最晚一条，避免中途误打卡把全天工时截短。
                if (lastClockOut == null || punch.getPunchTime().isAfter(lastClockOut.getPunchTime())) {
                    lastClockOut = punch;
                }
            } else if (Objects.equals("BREAK_START", punch.getPunchType())) {
                // 休息开始单独收集，后续与休息结束配对成净工作区间。
                breakStarts.add(punch);
            } else if (Objects.equals("BREAK_END", punch.getPunchType())) {
                // 休息结束单独收集，保持休息计算与打卡顺序解耦。
                breakEnds.add(punch);
            }
        }
        result.actualClockIn = firstClockIn == null ? null : firstClockIn.getPunchTime();
        result.actualClockOut = lastClockOut == null ? null : lastClockOut.getPunchTime();
        List<TimeRange> breakRanges = buildBreakRanges(breakStarts, breakEnds);
        result.actualBreakMinutes = calculateBreakMinutes(breakRanges);
        result.finalBreakMinutes = resolveFinalBreakMinutes(
            result.actualClockIn,
            result.actualClockOut,
            result.actualBreakMinutes,
            applicableRule
        );
        result.actualWorkMinutes = roundMinutes(
            calculateActualWorkMinutes(result.actualClockIn, result.actualClockOut, result.finalBreakMinutes),
            applicableRule
        );
        appendBasePresenceLogs(result);

        if (schedule == null && !punches.isEmpty()) {
            result.status = "NO_SCHEDULE";
            result.exceptionFlag = true;
            result.calcMessage = "无排班但存在有效打卡。";
            result.exceptions.add(new ExceptionPlan("NO_SCHEDULE_PUNCH", "WARN", "当天没有排班，但检测到有效打卡记录。", "查看原始打卡"));
            result.logs.add("由于没有排班但存在打卡，因此判定为无排班出勤。");
            return result;
        }

        if (schedule != null && punches.isEmpty()) {
            result.status = "ABSENCE";
            result.exceptionFlag = true;
            result.absenceMinutes = result.scheduledWorkMinutes;
            result.calcMessage = "有排班但没有有效打卡。";
            result.exceptions.add(new ExceptionPlan("SCHEDULE_NO_PUNCH", "ERROR", "当天有排班，但没有有效打卡，按缺勤处理。", "去打卡记录页处理"));
            result.logs.add("当天有排班但没有有效打卡，因此按缺勤处理。");
            return result;
        }

        if (schedule != null && result.actualClockIn == null && result.actualClockOut != null) {
            result.status = "MISSING_CLOCK_IN";
            result.exceptionFlag = true;
            result.calcMessage = "缺少上班卡。";
            result.exceptions.add(new ExceptionPlan("MISSING_CLOCK_IN", "ERROR", "只找到下班卡，缺少上班卡。", "去打卡记录页处理"));
            result.logs.add("只找到下班卡，没有上班卡，因此判定为缺上班卡。");
            return result;
        }

        if (schedule != null && result.actualClockIn != null && result.actualClockOut == null) {
            result.status = "MISSING_CLOCK_OUT";
            result.exceptionFlag = true;
            result.lateMinutes = calculateLateMinutes(result.actualClockIn, result.scheduledStartTime);
            result.calcMessage = "缺少下班卡。";
            result.exceptions.add(new ExceptionPlan("MISSING_CLOCK_OUT", "ERROR", "只找到上班卡，缺少下班卡。", "去打卡记录页处理"));
            result.logs.add("只找到上班卡，没有下班卡，因此判定为缺下班卡。");
            return result;
        }

        if (schedule != null && result.actualClockIn != null && result.actualClockOut != null) {
            applyCompletedWorkMetrics(result, applicableRule, buildWorkRanges(result.actualClockIn, result.actualClockOut, breakRanges), true);
            return result;
        }

        result.status = "NORMAL";
        result.exceptionFlag = false;
        result.calcMessage = "未命中异常分支。";
        result.logs.add("未命中任何异常分支，按正常结果收口。");
        return result;
    }

    // 根据审批后的最终时间和最终休息重建增强字段，保证审批流与初算共用同一套规则算法。
    CalculationResult calculateFromResolvedFinal(
        AttendanceDailyOut.ScheduleSnapshotOut schedule,
        Map<String, Object> applicableRule,
        LocalDateTime finalClockIn,
        LocalDateTime finalClockOut,
        Integer finalBreakMinutes
    ) {
        CalculationResult result = buildBaseResult(schedule, applicableRule);
        result.actualClockIn = finalClockIn;
        result.actualClockOut = finalClockOut;
        result.actualBreakMinutes = intValue(finalBreakMinutes);
        result.finalBreakMinutes = intValue(finalBreakMinutes);
        result.actualWorkMinutes = roundMinutes(
            calculateActualWorkMinutes(finalClockIn, finalClockOut, result.finalBreakMinutes),
            applicableRule
        );
        // 审批后没有逐段休息区间，只能按最终上下班重建一个总工作区间，再用统一口径计算深夜与休日。
        List<TimeRange> resolvedWorkRanges = finalClockIn != null && finalClockOut != null && finalClockOut.isAfter(finalClockIn)
            ? List.of(new TimeRange(finalClockIn, finalClockOut))
            : List.of();
        applyCompletedWorkMetrics(result, applicableRule, resolvedWorkRanges, false);
        return result;
    }

    // 统一初始化一份可复用的计算结果对象，确保初算与审批后重算使用相同的基础字段来源。
    private CalculationResult buildBaseResult(
        AttendanceDailyOut.ScheduleSnapshotOut schedule,
        Map<String, Object> applicableRule
    ) {
        CalculationResult result = new CalculationResult();
        result.logs = new ArrayList<>();
        result.exceptions = new ArrayList<>();
        result.workDayType = schedule == null ? "NO_SCHEDULE" : defaultText(schedule.getWorkDayType(), "WORKDAY");
        result.shiftScheduleId = schedule == null ? null : schedule.getShiftScheduleId();
        result.scheduledStartTime = schedule == null ? null : schedule.getStartTime();
        result.scheduledEndTime = schedule == null ? null : schedule.getEndTime();
        result.scheduledBreakMinutes = schedule == null ? 0 : intValue(schedule.getBreakMinutes());
        result.scheduledWorkMinutes = calculateScheduledWorkMinutes(schedule);
        result.appliedRuleId = longValue(readMapValue(applicableRule, "ruleId"));
        result.appliedRuleName = stringValue(readMapValue(applicableRule, "ruleName"));
        return result;
    }

    // 完整上下班场景下统一计算迟到、早退、深夜、休日和残业，避免初算与审批后重算分叉维护。
    private void applyCompletedWorkMetrics(
        CalculationResult result,
        Map<String, Object> applicableRule,
        List<TimeRange> workRanges,
        boolean allowAttendanceExceptions
    ) {
        result.lateMinutes = calculateLateMinutes(result.actualClockIn, result.scheduledStartTime);
        result.earlyLeaveMinutes = calculateEarlyLeaveMinutes(result.actualClockOut, result.scheduledEndTime);
        result.nightWorkMinutes = calculateNightWorkMinutes(workRanges, applicableRule);
        result.holidayType = resolveHolidayType(result.workDayType);
        if (result.holidayType != null) {
            result.status = "HOLIDAY_WORK";
            result.exceptionFlag = allowAttendanceExceptions;
            result.holidayWorkMinutes = result.actualWorkMinutes;
            result.overtimeMinutes = result.actualWorkMinutes;
            result.legalOvertimeMinutes = Objects.equals(HOLIDAY_TYPE_LEGAL, result.holidayType)
                ? result.actualWorkMinutes
                : 0;
            result.normalWorkMinutes = 0;
            if (allowAttendanceExceptions) {
                result.calcMessage = "休息日存在有效打卡。";
                result.exceptions.add(new ExceptionPlan("HOLIDAY_WORK", "WARN", "休息日检测到有效打卡。", "查看原始打卡"));
                result.logs.add("当前工作日类型命中 " + result.holidayType + "，因此把当天全部工时记入休日出勤。");
            }
            return;
        }
        int standardDailyMinutes = resolveStandardDailyMinutes(result.scheduledWorkMinutes, applicableRule);
        result.normalWorkMinutes = Math.min(result.actualWorkMinutes, standardDailyMinutes);
        result.overtimeMinutes = calculateOvertimeMinutes(result.actualWorkMinutes, standardDailyMinutes, applicableRule);
        result.legalOvertimeMinutes = result.overtimeMinutes;
        if (allowAttendanceExceptions && result.lateMinutes > 0) {
            result.status = "LATE";
            result.exceptionFlag = true;
            result.calcMessage = "实际上班晚于计划开始。";
            result.exceptions.add(new ExceptionPlan("LATE", "WARN", "实际上班时间晚于计划开始 " + result.lateMinutes + " 分钟。", "查看原始打卡"));
            result.logs.add("实际上班时间晚于计划开始 " + result.lateMinutes + " 分钟，因此判定为迟到。");
            if (result.earlyLeaveMinutes > 0) {
                result.exceptions.add(new ExceptionPlan("EARLY_LEAVE", "WARN", "实际下班时间早于计划结束 " + result.earlyLeaveMinutes + " 分钟。", "查看原始打卡"));
                result.logs.add("同时检测到早退 " + result.earlyLeaveMinutes + " 分钟。");
            }
            return;
        }
        if (allowAttendanceExceptions && result.earlyLeaveMinutes > 0) {
            result.status = "EARLY_LEAVE";
            result.exceptionFlag = true;
            result.calcMessage = "实际下班早于计划结束。";
            result.exceptions.add(new ExceptionPlan("EARLY_LEAVE", "WARN", "实际下班时间早于计划结束 " + result.earlyLeaveMinutes + " 分钟。", "查看原始打卡"));
            result.logs.add("实际下班时间早于计划结束 " + result.earlyLeaveMinutes + " 分钟，因此判定为早退。");
            return;
        }
        result.status = "NORMAL";
        result.exceptionFlag = false;
        result.calcMessage = allowAttendanceExceptions ? "按完整上下班卡生成正常日次结果。" : "审批修改最终时间后已按统一规则刷新结果。";
        if (allowAttendanceExceptions) {
            result.logs.add("规则计算结果：正常工时 " + result.normalWorkMinutes + " 分钟，残业 " + result.overtimeMinutes + " 分钟，深夜 " + result.nightWorkMinutes + " 分钟。");
            result.logs.add("已取得完整上下班卡，且无迟到早退，因此判定为正常。");
        }
    }

    // 统一记录初始出勤识别结果，避免日志口径在两套算法之间漂移。
    private void appendBasePresenceLogs(CalculationResult result) {
        if (result.actualClockIn != null) {
            result.logs.add("已选中最早上班卡：" + result.actualClockIn);
        }
        if (result.actualClockOut != null) {
            result.logs.add("已选中最晚下班卡：" + result.actualClockOut);
        }
        if (result.finalBreakMinutes > result.actualBreakMinutes) {
            result.logs.add("人工休息不足时已按正式规则补足自动休息至 " + result.finalBreakMinutes + " 分钟。");
        } else {
            result.logs.add("当天最终休息分钟为 " + result.finalBreakMinutes + " 分钟。");
        }
    }

    // 计算计划工时分钟，用于缺勤、标准工时和后续残业判断。
    private Integer calculateScheduledWorkMinutes(AttendanceDailyOut.ScheduleSnapshotOut schedule) {
        if (schedule == null || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return 0;
        }
        return Math.max(0, (int) Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes() - intValue(schedule.getBreakMinutes()));
    }

    // 把休息开始和结束打卡收口成成对区间，供自动休息、深夜交集和净工作区间共用。
    private List<TimeRange> buildBreakRanges(
        List<AttendanceDailyOut.PunchSnapshotOut> breakStarts,
        List<AttendanceDailyOut.PunchSnapshotOut> breakEnds
    ) {
        int pairCount = Math.min(breakStarts.size(), breakEnds.size());
        List<TimeRange> ranges = new ArrayList<>();
        for (int index = 0; index < pairCount; index++) {
            LocalDateTime start = breakStarts.get(index).getPunchTime();
            LocalDateTime end = breakEnds.get(index).getPunchTime();
            if (start == null || end == null || !end.isAfter(start)) {
                continue;
            }
            ranges.add(new TimeRange(start, end));
        }
        ranges.sort(Comparator.comparing(TimeRange::start));
        return ranges;
    }

    // 休息分钟统一由区间累加得出，避免不同入口对休息分钟做各自估算。
    private Integer calculateBreakMinutes(List<TimeRange> breakRanges) {
        int totalMinutes = 0;
        for (TimeRange breakRange : breakRanges) {
            totalMinutes += breakRange.minutes();
        }
        return totalMinutes;
    }

    // 自动休息优先保留人工休息；只有不足阈值时才补足到规则要求的最小扣休分钟。
    private Integer resolveFinalBreakMinutes(
        LocalDateTime actualClockIn,
        LocalDateTime actualClockOut,
        Integer actualBreakMinutes,
        Map<String, Object> applicableRule
    ) {
        int manualBreakMinutes = intValue(actualBreakMinutes);
        if (actualClockIn == null || actualClockOut == null) {
            return manualBreakMinutes;
        }
        if (!booleanValue(readMapValue(applicableRule, "autoBreakEnabled"))) {
            return manualBreakMinutes;
        }
        int thresholdMinutes = intValue(readMapValue(applicableRule, "autoBreakThresholdMinutes"));
        int deductMinutes = intValue(readMapValue(applicableRule, "autoBreakDeductMinutes"));
        if (thresholdMinutes <= 0 || deductMinutes <= 0) {
            return manualBreakMinutes;
        }
        int attendanceSpanMinutes = Math.max(0, (int) Duration.between(actualClockIn, actualClockOut).toMinutes());
        if (attendanceSpanMinutes < thresholdMinutes) {
            return manualBreakMinutes;
        }
        return Math.max(manualBreakMinutes, deductMinutes);
    }

    // 计算实际工时分钟，只有完整上下班区间时才输出有效值。
    private Integer calculateActualWorkMinutes(LocalDateTime actualClockIn, LocalDateTime actualClockOut, Integer breakMinutes) {
        if (actualClockIn == null || actualClockOut == null) {
            return 0;
        }
        return Math.max(0, (int) Duration.between(actualClockIn, actualClockOut).toMinutes() - intValue(breakMinutes));
    }

    // 工作区间先减去休息区间，后续深夜交集和休日工时都统一基于这些净工作区间计算。
    private List<TimeRange> buildWorkRanges(
        LocalDateTime actualClockIn,
        LocalDateTime actualClockOut,
        List<TimeRange> breakRanges
    ) {
        List<TimeRange> workRanges = new ArrayList<>();
        if (actualClockIn == null || actualClockOut == null || !actualClockOut.isAfter(actualClockIn)) {
            return workRanges;
        }
        LocalDateTime cursor = actualClockIn;
        for (TimeRange breakRange : breakRanges) {
            if (!breakRange.end().isAfter(cursor)) {
                continue;
            }
            if (breakRange.start().isAfter(actualClockOut)) {
                break;
            }
            LocalDateTime segmentEnd = breakRange.start().isBefore(actualClockOut) ? breakRange.start() : actualClockOut;
            if (segmentEnd.isAfter(cursor)) {
                workRanges.add(new TimeRange(cursor, segmentEnd));
            }
            if (breakRange.end().isAfter(cursor)) {
                cursor = breakRange.end().isAfter(actualClockOut) ? actualClockOut : breakRange.end();
            }
        }
        if (actualClockOut.isAfter(cursor)) {
            workRanges.add(new TimeRange(cursor, actualClockOut));
        }
        return workRanges;
    }

    // 深夜工时按配置时段逐日求交集，兼容 22:00-05:00 这种跨日夜勤窗口。
    private Integer calculateNightWorkMinutes(List<TimeRange> workRanges, Map<String, Object> applicableRule) {
        if (!booleanValue(readMapValue(applicableRule, "nightWorkEnabled")) || workRanges.isEmpty()) {
            return 0;
        }
        LocalTime nightStart = parseLocalTime(readMapValue(applicableRule, "nightWorkStart"), LocalTime.of(22, 0));
        LocalTime nightEnd = parseLocalTime(readMapValue(applicableRule, "nightWorkEnd"), LocalTime.of(5, 0));
        int totalMinutes = 0;
        for (TimeRange workRange : workRanges) {
            LocalDate cursorDate = workRange.start().toLocalDate().minusDays(1);
            LocalDate endDate = workRange.end().toLocalDate();
            while (!cursorDate.isAfter(endDate)) {
                LocalDateTime nightWindowStart = LocalDateTime.of(cursorDate, nightStart);
                LocalDateTime nightWindowEnd = LocalDateTime.of(cursorDate, nightEnd);
                if (!nightEnd.isAfter(nightStart)) {
                    nightWindowEnd = nightWindowEnd.plusDays(1);
                }
                totalMinutes += overlapMinutes(workRange.start(), workRange.end(), nightWindowStart, nightWindowEnd);
                cursorDate = cursorDate.plusDays(1);
            }
        }
        return totalMinutes;
    }

    // 休日类型先按班次的工作日分类判断，把法定休日和所定休日沉淀成两档常量。
    private String resolveHolidayType(String workDayType) {
        if (!StringUtils.hasText(workDayType)) {
            return null;
        }
        String normalized = workDayType.trim().toUpperCase(Locale.ROOT);
        if (Objects.equals("LEGAL_HOLIDAY", normalized) || Objects.equals("STATUTORY_HOLIDAY", normalized)) {
            return HOLIDAY_TYPE_LEGAL;
        }
        if (
            Objects.equals("REST", normalized)
                || Objects.equals("OFF", normalized)
                || Objects.equals("HOLIDAY", normalized)
                || Objects.equals("SCHEDULED_HOLIDAY", normalized)
        ) {
            return HOLIDAY_TYPE_SCHEDULED;
        }
        return null;
    }

    // 工作日标准工时优先按正式规则读取，未配置时回落到排班计划工时。
    private int resolveStandardDailyMinutes(Integer scheduledWorkMinutes, Map<String, Object> applicableRule) {
        int standardDailyMinutes = intValue(readMapValue(applicableRule, "standardDailyMinutes"));
        if (standardDailyMinutes > 0) {
            return standardDailyMinutes;
        }
        return Math.max(0, intValue(scheduledWorkMinutes));
    }

    // 第一版残业先按超出规则标准工时的分钟数计算，并尊重员工规则里的残业开关。
    private Integer calculateOvertimeMinutes(Integer actualWorkMinutes, int standardDailyMinutes, Map<String, Object> applicableRule) {
        if (!booleanValue(readMapValue(applicableRule, "overtimeEnabled"))) {
            return 0;
        }
        return Math.max(0, intValue(actualWorkMinutes) - Math.max(0, standardDailyMinutes));
    }

    // 取整规则统一落在分钟层，保持前后端和导出结果看到的是同一套分钟值。
    private Integer roundMinutes(Integer minutes, Map<String, Object> applicableRule) {
        int originalMinutes = intValue(minutes);
        int unitMinutes = intValue(readMapValue(applicableRule, "roundingUnitMinutes"));
        String roundingMode = stringValue(readMapValue(applicableRule, "roundingMode"));
        if (originalMinutes <= 0 || unitMinutes <= 0 || !StringUtils.hasText(roundingMode)) {
            return originalMinutes;
        }
        double base = (double) originalMinutes / unitMinutes;
        return switch (roundingMode) {
            case "ROUND_UP" -> (int) Math.ceil(base) * unitMinutes;
            case "ROUND_DOWN" -> (int) Math.floor(base) * unitMinutes;
            case "ROUND_NEAREST" -> (int) Math.round(base) * unitMinutes;
            default -> originalMinutes;
        };
    }

    // 统一计算两个时间窗交集分钟，避免深夜等算法各写一套边界判断。
    private int overlapMinutes(
        LocalDateTime startA,
        LocalDateTime endA,
        LocalDateTime startB,
        LocalDateTime endB
    ) {
        LocalDateTime overlapStart = startA.isAfter(startB) ? startA : startB;
        LocalDateTime overlapEnd = endA.isBefore(endB) ? endA : endB;
        if (!overlapEnd.isAfter(overlapStart)) {
            return 0;
        }
        return (int) Duration.between(overlapStart, overlapEnd).toMinutes();
    }

    // 统一解析 HH:mm 规则时间，解析失败时直接回到默认值，避免脏规则把整次重算打断。
    private LocalTime parseLocalTime(Object value, LocalTime defaultValue) {
        String text = stringValue(value);
        if (!StringUtils.hasText(text)) {
            return defaultValue;
        }
        try {
            return LocalTime.parse(text);
        } catch (DateTimeParseException error) {
            return defaultValue;
        }
    }

    // 计算迟到分钟，只有计划开始和实际上班同时存在时才有效。
    private Integer calculateLateMinutes(LocalDateTime actualClockIn, LocalDateTime scheduledStartTime) {
        if (actualClockIn == null || scheduledStartTime == null || !actualClockIn.isAfter(scheduledStartTime)) {
            return 0;
        }
        return (int) Duration.between(scheduledStartTime, actualClockIn).toMinutes();
    }

    // 计算早退分钟，只有计划结束和实际下班同时存在时才有效。
    private Integer calculateEarlyLeaveMinutes(LocalDateTime actualClockOut, LocalDateTime scheduledEndTime) {
        if (actualClockOut == null || scheduledEndTime == null || !actualClockOut.isBefore(scheduledEndTime)) {
            return 0;
        }
        return (int) Duration.between(actualClockOut, scheduledEndTime).toMinutes();
    }

    // 对可能为空的文本做统一默认值回填。
    private String defaultText(String value, String defaultValue) {
        String normalized = value == null ? "" : value.trim();
        return StringUtils.hasText(normalized) ? normalized : defaultValue;
    }

    // 把任意对象安全转成 long，供规则主键等字段稳定回读。
    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    // 把任意对象安全转成 int，供分钟计算复用。
    private int intValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    // 把任意对象安全转成字符串，供规则字段和日志分支复用。
    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    // 兼容数据库里 TINYINT、BOOLEAN 和字符串三种布尔返回值。
    private boolean booleanValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        String text = stringValue(value);
        return Objects.equals("true", text.toLowerCase(Locale.ROOT)) || Objects.equals("1", text);
    }

    // 兼容 SQL Map 键名大小写差异，避免规则字段因驱动差异读不到。
    private Object readMapValue(Map<String, Object> source, String preferredKey) {
        if (source == null || preferredKey == null) {
            return null;
        }
        if (source.containsKey(preferredKey)) {
            return source.get(preferredKey);
        }
        String upperKey = preferredKey.toUpperCase(Locale.ROOT);
        if (source.containsKey(upperKey)) {
            return source.get(upperKey);
        }
        String lowerKey = preferredKey.toLowerCase(Locale.ROOT);
        if (source.containsKey(lowerKey)) {
            return source.get(lowerKey);
        }
        return null;
    }

    // 聚合一次日次计算产出的所有字段和解释信息，供初算和审批后重算共享。
    static final class CalculationResult {
        Long shiftScheduleId;
        String workDayType;
        LocalDateTime scheduledStartTime;
        LocalDateTime scheduledEndTime;
        Integer scheduledBreakMinutes;
        Integer scheduledWorkMinutes;
        LocalDateTime actualClockIn;
        LocalDateTime actualClockOut;
        Integer actualBreakMinutes = 0;
        Integer finalBreakMinutes = 0;
        Integer actualWorkMinutes = 0;
        Integer lateMinutes = 0;
        Integer earlyLeaveMinutes = 0;
        Integer absenceMinutes = 0;
        Integer normalWorkMinutes = 0;
        Integer overtimeMinutes = 0;
        Integer legalOvertimeMinutes = 0;
        Integer nightWorkMinutes = 0;
        Integer holidayWorkMinutes = 0;
        String holidayType;
        Long appliedRuleId;
        String appliedRuleName;
        String status;
        Boolean exceptionFlag = false;
        String calcMessage;
        List<String> logs;
        List<ExceptionPlan> exceptions;
    }

    // 用统一的开始结束时间表达工作区间和休息区间，便于深夜交集和工时扣减复用。
    private record TimeRange(LocalDateTime start, LocalDateTime end) {

        private int minutes() {
            return Math.max(0, (int) Duration.between(start, end).toMinutes());
        }
    }

    // 记录一条异常计划，供重算后统一写入异常表。
    record ExceptionPlan(
        String exceptionType,
        String exceptionLevel,
        String message,
        String suggestedAction
    ) {
    }
}
