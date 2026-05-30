package com.sp.selfsp.attendance.daily.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.daily.dao.AttendanceDailyDao;
import com.sp.selfsp.attendance.daily.domain.in.AttendanceDailyIn;
import com.sp.selfsp.attendance.daily.domain.out.AttendanceDailyOut;
import com.sp.selfsp.attendance.daily.service.AttendanceDailyService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 第四阶段日次勤怠服务实现。
 *
 * <p>负责把排班快照和原始打卡汇总成日次结果，同时生成异常清单和可追溯的计算日志。</p>
 */
@Service
public class AttendanceDailyServiceImpl implements AttendanceDailyService {

    // 默认页码用于页面第一次进入时先展示第一页日次结果。
    private static final int DEFAULT_PAGE = 1;
    // 默认页大小用于控制第四阶段列表的首次返回量。
    private static final int DEFAULT_PAGE_SIZE = 20;
    // 产品允许的页大小档位固定为 20、50、100、200。
    private static final List<Integer> ALLOWED_PAGE_SIZES = List.of(20, 50, 100, 200);
    // 计算版本用于记录当前日次结果采用的是哪套基础口径。
    private static final String CALC_VERSION = "phase4-v1";

    // 读取和写入日次、异常、计算日志以及排班和打卡快照。
    private final AttendanceDailyDao attendanceDailyDao;
    // JSON 序列化器用于把计算过程上下文写入日志。
    private final ObjectMapper objectMapper;

    // 注入第四阶段所需 DAO 与 JSON 工具，统一处理日次计算编排。
    public AttendanceDailyServiceImpl(
        AttendanceDailyDao attendanceDailyDao,
        ObjectMapper objectMapper
    ) {
        this.attendanceDailyDao = attendanceDailyDao;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AttendanceDailyOut.DailyListOut listDailyResults(AttendanceDailyIn.DailyQueryIn queryIn) {
        // 统一补齐默认日期范围与分页配置，避免列表首次打开时进入全量查询。
        AttendanceDailyIn.DailyQueryIn normalizedQuery = normalizeQuery(queryIn);
        // 查询前先补算本次筛选范围缺失的日次结果，保证列表进入时就能看到真实结论。
        ensureDailyResults(normalizedQuery, "LIST_VIEW");
        int offset = (normalizedQuery.getPage() - 1) * normalizedQuery.getPageSize();
        List<AttendanceDailyOut.DailyItemOut> items = attendanceDailyDao.selectDailyList(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            normalizedQuery,
            offset,
            normalizedQuery.getPageSize()
        );
        Integer total = attendanceDailyDao.countDailyList(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery);
        AttendanceDailyOut.DailyListOut listOut = new AttendanceDailyOut.DailyListOut();
        listOut.setItems(items);
        listOut.setTotal(total == null ? 0 : total);
        listOut.setPage(normalizedQuery.getPage());
        listOut.setPageSize(normalizedQuery.getPageSize());
        listOut.setTotalPages(calculateTotalPages(total == null ? 0 : total, normalizedQuery.getPageSize()));
        listOut.setSummary(buildSummary(
            attendanceDailyDao.countDailySummary(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery)
        ));
        return listOut;
    }

    @Override
    public AttendanceDailyOut.DailyDetailOut getDailyDetail(Long dailyId) {
        // 先确认目标日次存在，再拼接右侧详情抽屉所需的排班、打卡、异常和过程日志。
        AttendanceDailyOut.DailyDetailOut detailOut = requireDailyDetail(dailyId);
        detailOut.setSchedule(
            attendanceDailyDao.selectScheduleSnapshot(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                detailOut.getEmployeeId(),
                detailOut.getWorkDate()
            )
        );
        detailOut.setPunches(
            attendanceDailyDao.selectProcessedPunches(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                detailOut.getEmployeeId(),
                detailOut.getWorkDate()
            )
        );
        detailOut.setExceptions(
            attendanceDailyDao.selectExceptionsByDailyId(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                detailOut.getId()
            )
        );
        detailOut.setCalcSteps(
            attendanceDailyDao.selectCalcStepsByEmployeeDate(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                detailOut.getEmployeeId(),
                detailOut.getWorkDate()
            )
        );
        return detailOut;
    }

    @Override
    @Transactional
    public AttendanceDailyOut.RecalculateResultOut recalculateDaily(AttendanceDailyIn.DailyRecalculateIn recalculateIn) {
        // 校验单日重算必填字段，避免把空员工或空日期带入重算逻辑。
        if (recalculateIn == null || recalculateIn.getEmployeeId() == null || !StringUtils.hasText(recalculateIn.getWorkDate())) {
            throw new IllegalArgumentException("employeeId 和 workDate 不能为空");
        }
        LocalDate workDate = parseDate(recalculateIn.getWorkDate());
        RecalculationOutcome outcome = recalculateOneDay(recalculateIn.getEmployeeId(), workDate, "MANUAL_RECALCULATE");
        AttendanceDailyOut.RecalculateResultOut resultOut = new AttendanceDailyOut.RecalculateResultOut();
        resultOut.setRequestedCount(1);
        resultOut.setSuccessCount(outcome.success ? 1 : 0);
        resultOut.setFailedCount(outcome.success ? 0 : 1);
        resultOut.setDetail(getDailyDetail(outcome.dailyId));
        return resultOut;
    }

    @Override
    @Transactional
    public AttendanceDailyOut.RecalculateResultOut recalculateRange(AttendanceDailyIn.DailyRecalculateRangeIn recalculateRangeIn) {
        // 把范围重算入参复用到查询口径中，保持筛选与列表一致。
        AttendanceDailyIn.DailyQueryIn queryIn = new AttendanceDailyIn.DailyQueryIn();
        queryIn.setStartDate(recalculateRangeIn == null ? null : recalculateRangeIn.getStartDate());
        queryIn.setEndDate(recalculateRangeIn == null ? null : recalculateRangeIn.getEndDate());
        queryIn.setWorkplaceId(recalculateRangeIn == null ? null : recalculateRangeIn.getWorkplaceId());
        queryIn.setDepartmentId(recalculateRangeIn == null ? null : recalculateRangeIn.getDepartmentId());
        queryIn.setEmployeeKeyword(recalculateRangeIn == null ? null : recalculateRangeIn.getEmployeeKeyword());
        queryIn.setExceptionOnly(recalculateRangeIn == null ? null : recalculateRangeIn.getExceptionOnly());
        AttendanceDailyIn.DailyQueryIn normalizedQuery = normalizeQuery(queryIn);
        List<Map<String, Object>> targets = attendanceDailyDao.selectRecalculationTargets(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            normalizedQuery
        );
        int successCount = 0;
        int failedCount = 0;
        for (Map<String, Object> target : targets) {
            try {
                recalculateOneDay(
                    longValue(readMapValue(target, "employeeId")),
                    localDateValue(readMapValue(target, "workDate")),
                    "BATCH_RECALCULATE"
                );
                successCount += 1;
            } catch (RuntimeException error) {
                failedCount += 1;
            }
        }
        AttendanceDailyOut.RecalculateResultOut resultOut = new AttendanceDailyOut.RecalculateResultOut();
        resultOut.setRequestedCount(targets.size());
        resultOut.setSuccessCount(successCount);
        resultOut.setFailedCount(failedCount);
        return resultOut;
    }

    @Override
    @Transactional
    public void lockDaily(Long dailyId) {
        // 第五阶段只有已生成的日次结果才能进入锁定，避免空主键或不存在主键直接写坏状态。
        Map<String, Object> dailyMeta = requireDailyMeta(dailyId);
        // 已经锁定的日次直接跳过，保证重复点击锁定按钮时不会再触发额外状态抖动。
        if (booleanValue(readMapValue(dailyMeta, "lockedFlag"))) {
            return;
        }
        // 只允许未锁定日次进入锁定态，防止把无效状态误标记成锁定完成。
        if (!isLockAllowed(dailyMeta)) {
            throw new IllegalStateException("当前日次结果状态不允许锁定");
        }
        attendanceDailyDao.updateDailyLockedState(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyId,
            "LOCKED",
            "LOCKED",
            true,
            LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void unlockDaily(Long dailyId) {
        // 解锁动作同样先验证主键存在，避免前端旧数据触发无效解锁。
        Map<String, Object> dailyMeta = requireDailyMeta(dailyId);
        // 未锁定记录不需要重复解锁，保持操作幂等。
        if (!booleanValue(readMapValue(dailyMeta, "lockedFlag"))) {
            return;
        }
        attendanceDailyDao.updateDailyLockedState(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyId,
            "RESOLVED",
            stringValue(readMapValue(dailyMeta, "approvalStatus")).isEmpty() ? "APPROVED" : stringValue(readMapValue(dailyMeta, "approvalStatus")),
            false,
            null
        );
    }

    // 查询列表前按当前筛选范围自动补算缺失结果，保证第四阶段页面进入即有结论。
    private void ensureDailyResults(AttendanceDailyIn.DailyQueryIn queryIn, String triggerType) {
        List<Map<String, Object>> targets = attendanceDailyDao.selectCalculationTargets(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            queryIn
        );
        for (Map<String, Object> target : targets) {
            recalculateOneDay(
                longValue(readMapValue(target, "employeeId")),
                localDateValue(readMapValue(target, "workDate")),
                triggerType
            );
        }
    }

    // 对单个员工单天执行完整日次重算，并返回本次写入结果主键。
    private RecalculationOutcome recalculateOneDay(Long employeeId, LocalDate workDate, String triggerType) {
        // 第五阶段若日次已进入审批或锁定，就不能再被重算覆盖最终业务结论。
        Map<String, Object> currentDaily = attendanceDailyDao.selectDailyIdentity(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeId,
            workDate
        );
        if (isProtectedFromRecalculation(currentDaily)) {
            Long protectedDailyId = longValue(readMapValue(currentDaily, "id"));
            // 列表补算只应静默跳过受保护记录，避免打开列表就因为审批中的数据抛错。
            if (Objects.equals("LIST_VIEW", triggerType)) {
                return new RecalculationOutcome(true, protectedDailyId);
            }
            throw new IllegalStateException("当前日次结果已进入审批或锁定阶段，不能直接重算");
        }
        AttendanceDailyOut.ScheduleSnapshotOut schedule = attendanceDailyDao.selectScheduleSnapshot(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeId,
            workDate
        );
        List<AttendanceDailyOut.PunchSnapshotOut> punches = attendanceDailyDao.selectProcessedPunches(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeId,
            workDate
        );

        CalculationResult calculationResult = calculateDaily(schedule, punches);
        attendanceDailyDao.mergeDaily(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeId,
            workDate,
            schedule == null ? null : schedule.getShiftScheduleId(),
            calculationResult.workDayType,
            calculationResult.scheduledStartTime,
            calculationResult.scheduledEndTime,
            calculationResult.scheduledBreakMinutes,
            calculationResult.scheduledWorkMinutes,
            calculationResult.actualClockIn,
            calculationResult.actualClockOut,
            calculationResult.actualBreakMinutes,
            calculationResult.actualWorkMinutes,
            calculationResult.lateMinutes,
            calculationResult.earlyLeaveMinutes,
            calculationResult.absenceMinutes,
            calculationResult.normalWorkMinutes,
            0,
            0,
            0,
            calculationResult.holidayWorkMinutes,
            calculationResult.status,
            "NONE",
            calculationResult.exceptionFlag ? "UNHANDLED" : "RESOLVED",
            calculationResult.exceptionFlag,
            false,
            LocalDateTime.now(),
            CALC_VERSION,
            calculationResult.calcMessage
        );

        Map<String, Object> identity = attendanceDailyDao.selectDailyIdentity(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeId,
            workDate
        );
        Long dailyId = longValue(readMapValue(identity, "id"));
        attendanceDailyDao.deleteExceptionsByDailyId(AttendanceTenantContext.DEFAULT_TENANT_ID, dailyId);
        for (ExceptionPlan exceptionPlan : calculationResult.exceptions) {
            attendanceDailyDao.insertException(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                employeeId,
                workDate,
                dailyId,
                exceptionPlan.exceptionType,
                exceptionPlan.exceptionLevel,
                "OPEN",
                exceptionPlan.message,
                exceptionPlan.suggestedAction
            );
        }
        attendanceDailyDao.deleteCalcLogsByEmployeeDate(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeId, workDate);
        int stepIndex = 1;
        for (String logLine : calculationResult.logs) {
            attendanceDailyDao.insertCalcLog(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                employeeId,
                workDate,
                dailyId,
                triggerType,
                null,
                "SUCCESS",
                "STEP_" + stepIndex,
                logLine,
                toJsonString(Map.of("status", calculationResult.status, "workDate", workDate.toString()))
            );
            stepIndex += 1;
        }
        return new RecalculationOutcome(true, dailyId);
    }

    // 根据排班和打卡快照产出第四阶段第一版的基础日次结果与异常。
    private CalculationResult calculateDaily(
        AttendanceDailyOut.ScheduleSnapshotOut schedule,
        List<AttendanceDailyOut.PunchSnapshotOut> punches
    ) {
        CalculationResult result = new CalculationResult();
        result.logs = new ArrayList<>();
        result.exceptions = new ArrayList<>();
        result.workDayType = schedule == null ? "NO_SCHEDULE" : defaultText(schedule.getWorkDayType(), "WORKDAY");
        result.scheduledStartTime = schedule == null ? null : schedule.getStartTime();
        result.scheduledEndTime = schedule == null ? null : schedule.getEndTime();
        result.scheduledBreakMinutes = schedule == null ? 0 : intValue(schedule.getBreakMinutes());
        result.scheduledWorkMinutes = calculateScheduledWorkMinutes(schedule);

        AttendanceDailyOut.PunchSnapshotOut firstClockIn = null;
        AttendanceDailyOut.PunchSnapshotOut lastClockOut = null;
        List<AttendanceDailyOut.PunchSnapshotOut> breakStarts = new ArrayList<>();
        List<AttendanceDailyOut.PunchSnapshotOut> breakEnds = new ArrayList<>();
        for (AttendanceDailyOut.PunchSnapshotOut punch : punches) {
            if (Objects.equals("CLOCK_IN", punch.getPunchType())) {
                if (firstClockIn == null || punch.getPunchTime().isBefore(firstClockIn.getPunchTime())) {
                    firstClockIn = punch;
                }
            } else if (Objects.equals("CLOCK_OUT", punch.getPunchType())) {
                if (lastClockOut == null || punch.getPunchTime().isAfter(lastClockOut.getPunchTime())) {
                    lastClockOut = punch;
                }
            } else if (Objects.equals("BREAK_START", punch.getPunchType())) {
                breakStarts.add(punch);
            } else if (Objects.equals("BREAK_END", punch.getPunchType())) {
                breakEnds.add(punch);
            }
        }
        result.actualClockIn = firstClockIn == null ? null : firstClockIn.getPunchTime();
        result.actualClockOut = lastClockOut == null ? null : lastClockOut.getPunchTime();
        result.actualBreakMinutes = calculateBreakMinutes(breakStarts, breakEnds);
        result.actualWorkMinutes = calculateActualWorkMinutes(result.actualClockIn, result.actualClockOut, result.actualBreakMinutes);
        result.logs.add(schedule == null ? "当天没有排班快照，按无排班逻辑进入计算。" : "已读取当天排班快照：" + defaultText(schedule.getLabel(), "未命名班次"));
        result.logs.add("当天有效打卡数量为 " + punches.size() + " 条。");
        if (result.actualClockIn != null) {
            result.logs.add("已选中最早上班卡：" + result.actualClockIn);
        }
        if (result.actualClockOut != null) {
            result.logs.add("已选中最晚下班卡：" + result.actualClockOut);
        }

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
            result.lateMinutes = calculateLateMinutes(result.actualClockIn, result.scheduledStartTime);
            result.earlyLeaveMinutes = calculateEarlyLeaveMinutes(result.actualClockOut, result.scheduledEndTime);
            result.normalWorkMinutes = Math.max(0, result.actualWorkMinutes);
            if (Objects.equals("REST", result.workDayType) || Objects.equals("OFF", result.workDayType)) {
                result.status = "HOLIDAY_WORK";
                result.exceptionFlag = true;
                result.holidayWorkMinutes = result.actualWorkMinutes;
                result.calcMessage = "休息日存在有效打卡。";
                result.exceptions.add(new ExceptionPlan("HOLIDAY_WORK", "WARN", "休息日检测到有效打卡。", "查看原始打卡"));
                result.logs.add("当前工作日类型为休息日，但存在有效打卡，因此判定为休日出勤。");
                return result;
            }
            if (result.lateMinutes > 0) {
                result.status = "LATE";
                result.exceptionFlag = true;
                result.calcMessage = "实际上班晚于计划开始。";
                result.exceptions.add(new ExceptionPlan("LATE", "WARN", "实际上班时间晚于计划开始 " + result.lateMinutes + " 分钟。", "查看原始打卡"));
                result.logs.add("实际上班时间晚于计划开始 " + result.lateMinutes + " 分钟，因此判定为迟到。");
                if (result.earlyLeaveMinutes > 0) {
                    result.exceptions.add(new ExceptionPlan("EARLY_LEAVE", "WARN", "实际下班时间早于计划结束 " + result.earlyLeaveMinutes + " 分钟。", "查看原始打卡"));
                    result.logs.add("同时检测到早退 " + result.earlyLeaveMinutes + " 分钟。");
                }
                return result;
            }
            if (result.earlyLeaveMinutes > 0) {
                result.status = "EARLY_LEAVE";
                result.exceptionFlag = true;
                result.calcMessage = "实际下班早于计划结束。";
                result.exceptions.add(new ExceptionPlan("EARLY_LEAVE", "WARN", "实际下班时间早于计划结束 " + result.earlyLeaveMinutes + " 分钟。", "查看原始打卡"));
                result.logs.add("实际下班时间早于计划结束 " + result.earlyLeaveMinutes + " 分钟，因此判定为早退。");
                return result;
            }
            result.status = "NORMAL";
            result.exceptionFlag = false;
            result.calcMessage = "按完整上下班卡生成正常日次结果。";
            result.logs.add("已取得完整上下班卡，且无迟到早退，因此判定为正常。");
            return result;
        }

        result.status = "NORMAL";
        result.exceptionFlag = false;
        result.calcMessage = "未命中异常分支。";
        result.logs.add("未命中任何异常分支，按正常结果收口。");
        return result;
    }

    // 把查询入参补齐默认值，保持列表和自动补算使用同一口径。
    private AttendanceDailyIn.DailyQueryIn normalizeQuery(AttendanceDailyIn.DailyQueryIn queryIn) {
        AttendanceDailyIn.DailyQueryIn normalized = queryIn == null ? new AttendanceDailyIn.DailyQueryIn() : queryIn;
        normalized.setStartDate(defaultText(normalized.getStartDate(), "2026-05-01"));
        normalized.setEndDate(defaultText(normalized.getEndDate(), "2026-05-31"));
        normalized.setEmployeeKeyword(normalizeText(normalized.getEmployeeKeyword()));
        normalized.setStatus(normalizeText(normalized.getStatus()));
        normalized.setExceptionOnly(Boolean.TRUE.equals(normalized.getExceptionOnly()));
        normalized.setPage(normalized.getPage() == null || normalized.getPage() <= 0 ? DEFAULT_PAGE : normalized.getPage());
        normalized.setPageSize(normalizePageSize(normalized.getPageSize()));
        return normalized;
    }

    // 限制页大小只允许固定档位，避免任意数字冲击数据库分页稳定性。
    private Integer normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return ALLOWED_PAGE_SIZES.contains(pageSize) ? pageSize : DEFAULT_PAGE_SIZE;
    }

    // 预先计算总页数，避免前端自己换算时处理空数据边界。
    private Integer calculateTotalPages(int total, int pageSize) {
        if (total <= 0) {
            return 1;
        }
        return (total + pageSize - 1) / pageSize;
    }

    // 把数据库状态统计收敛到页面顶部四张卡的口径。
    private AttendanceDailyOut.DailySummaryOut buildSummary(List<Map<String, Object>> rows) {
        AttendanceDailyOut.DailySummaryOut summaryOut = new AttendanceDailyOut.DailySummaryOut();
        summaryOut.setNormalCount(0);
        summaryOut.setLateCount(0);
        summaryOut.setMissingClockCount(0);
        summaryOut.setAbsenceCount(0);
        for (Map<String, Object> row : rows) {
            String status = stringValue(readMapValue(row, "status"));
            int total = intValue(readMapValue(row, "totalCount"));
            if (Objects.equals("NORMAL", status)) {
                summaryOut.setNormalCount(summaryOut.getNormalCount() + total);
            } else if (Objects.equals("LATE", status) || Objects.equals("EARLY_LEAVE", status)) {
                summaryOut.setLateCount(summaryOut.getLateCount() + total);
            } else if (Objects.equals("MISSING_CLOCK_IN", status) || Objects.equals("MISSING_CLOCK_OUT", status)) {
                summaryOut.setMissingClockCount(summaryOut.getMissingClockCount() + total);
            } else if (Objects.equals("ABSENCE", status)) {
                summaryOut.setAbsenceCount(summaryOut.getAbsenceCount() + total);
            }
        }
        return summaryOut;
    }

    // 读取详情前先确认目标日次存在。
    private AttendanceDailyOut.DailyDetailOut requireDailyDetail(Long dailyId) {
        if (dailyId == null || dailyId <= 0) {
            throw new IllegalArgumentException("dailyId 不能为空");
        }
        AttendanceDailyOut.DailyDetailOut detailOut = attendanceDailyDao.selectDailyDetailById(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyId
        );
        if (detailOut == null) {
            throw new IllegalArgumentException("未找到对应日次结果");
        }
        return detailOut;
    }

    // 计算计划工时分钟，用于无打卡缺勤时给出基础分钟数。
    private Integer calculateScheduledWorkMinutes(AttendanceDailyOut.ScheduleSnapshotOut schedule) {
        if (schedule == null || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return 0;
        }
        return Math.max(0, (int) Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes() - intValue(schedule.getBreakMinutes()));
    }

    // 计算休息分钟，只对成对出现的休息开始和结束做累加。
    private Integer calculateBreakMinutes(
        List<AttendanceDailyOut.PunchSnapshotOut> breakStarts,
        List<AttendanceDailyOut.PunchSnapshotOut> breakEnds
    ) {
        int pairCount = Math.min(breakStarts.size(), breakEnds.size());
        int totalMinutes = 0;
        for (int index = 0; index < pairCount; index++) {
            totalMinutes += Math.max(0, Duration.between(breakStarts.get(index).getPunchTime(), breakEnds.get(index).getPunchTime()).toMinutes());
        }
        return totalMinutes;
    }

    // 计算实际工时分钟，只有完整上下班卡时才输出有效值。
    private Integer calculateActualWorkMinutes(LocalDateTime actualClockIn, LocalDateTime actualClockOut, Integer breakMinutes) {
        if (actualClockIn == null || actualClockOut == null) {
            return 0;
        }
        return Math.max(0, (int) Duration.between(actualClockIn, actualClockOut).toMinutes() - intValue(breakMinutes));
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

    // 第五阶段已进入审批、退回、通过或锁定的日次，必须保护最终结果不被重算覆盖。
    private boolean isProtectedFromRecalculation(Map<String, Object> identity) {
        if (identity == null || identity.isEmpty()) {
            return false;
        }
        if (booleanValue(readMapValue(identity, "lockedFlag"))) {
            return true;
        }
        String approvalStatus = stringValue(readMapValue(identity, "approvalStatus"));
        if (
            Objects.equals("SUBMITTED", approvalStatus)
                || Objects.equals("RETURNED", approvalStatus)
                || Objects.equals("APPROVED", approvalStatus)
                || Objects.equals("LOCKED", approvalStatus)
        ) {
            return true;
        }
        String handlingStatus = stringValue(readMapValue(identity, "handlingStatus"));
        return Objects.equals("IN_REVIEW", handlingStatus) || Objects.equals("LOCKED", handlingStatus);
    }

    // 只有尚未锁定的日次才允许进入锁定动作，避免锁定状态被重复覆盖。
    private boolean isLockAllowed(Map<String, Object> dailyMeta) {
        return dailyMeta != null && !booleanValue(readMapValue(dailyMeta, "lockedFlag"));
    }

    // 第五阶段大量审批动作都需要先确认日次主键有效，这里统一封装存在性检查。
    private Map<String, Object> requireDailyMeta(Long dailyId) {
        if (dailyId == null || dailyId <= 0) {
            throw new IllegalArgumentException("dailyId 不能为空");
        }
        Map<String, Object> dailyMeta = attendanceDailyDao.selectDailyMetaById(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyId
        );
        if (dailyMeta == null || dailyMeta.isEmpty()) {
            throw new IllegalArgumentException("未找到对应日次结果");
        }
        return dailyMeta;
    }

    // 把日期字符串统一解析成 LocalDate，保证第四阶段所有日期口径一致。
    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException error) {
            throw new IllegalArgumentException("日期格式不正确：" + value);
        }
    }

    // 把文本收敛成统一 trim 口径，减少空白字符污染查询。
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    // 对可能为空的文本做统一默认值回填。
    private String defaultText(String value, String defaultValue) {
        String normalized = normalizeText(value);
        return StringUtils.hasText(normalized) ? normalized : defaultValue;
    }

    // 把任意对象安全转成 long，供目标查询结果回读 employeeId 与 dailyId。
    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    // 把任意对象安全转成 int，供统计与分钟计算复用。
    private int intValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    // 把任意对象安全转成字符串，供日志和统计分支复用。
    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    // 兼容数据库里 TINYINT、BOOLEAN 和字符串三种锁定标记返回值。
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

    // 兼容不同数据库驱动下 Map 键名大小写差异，避免第四阶段批量目标字段回读失败。
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

    // 兼容 SQL DATE、LocalDate 和字符串三种来源，保证候选日期能稳定进入重算链路。
    private LocalDate localDateValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("日期格式不正确：");
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return parseDate(stringValue(value));
    }

    // 把计算过程上下文序列化为 JSON，供计算日志落库。
    private String toJsonString(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException error) {
            throw new IllegalArgumentException("计算日志序列化失败", error);
        }
    }

    // 记录单次重算的主键结果，便于返回详情。
    private static class RecalculationOutcome {
        private final boolean success;
        private final Long dailyId;

        private RecalculationOutcome(boolean success, Long dailyId) {
            this.success = success;
            this.dailyId = dailyId;
        }
    }

    // 聚合一次日次计算产出的所有字段和解释信息。
    private static class CalculationResult {
        private Long shiftScheduleId;
        private String workDayType;
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;
        private Integer scheduledBreakMinutes;
        private Integer scheduledWorkMinutes;
        private LocalDateTime actualClockIn;
        private LocalDateTime actualClockOut;
        private Integer actualBreakMinutes = 0;
        private Integer actualWorkMinutes = 0;
        private Integer lateMinutes = 0;
        private Integer earlyLeaveMinutes = 0;
        private Integer absenceMinutes = 0;
        private Integer normalWorkMinutes = 0;
        private Integer holidayWorkMinutes = 0;
        private String status;
        private Boolean exceptionFlag = false;
        private String calcMessage;
        private List<String> logs;
        private List<ExceptionPlan> exceptions;
    }

    // 记录一条异常计划，供重算后统一写入异常表。
    private record ExceptionPlan(
        String exceptionType,
        String exceptionLevel,
        String message,
        String suggestedAction
    ) {
    }
}
