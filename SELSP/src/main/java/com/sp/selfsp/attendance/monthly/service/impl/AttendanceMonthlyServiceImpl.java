package com.sp.selfsp.attendance.monthly.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.daily.domain.in.AttendanceDailyIn;
import com.sp.selfsp.attendance.daily.service.AttendanceDailyService;
import com.sp.selfsp.attendance.monthly.dao.AttendanceMonthlyDao;
import com.sp.selfsp.attendance.monthly.domain.in.AttendanceMonthlyIn;
import com.sp.selfsp.attendance.monthly.domain.out.AttendanceMonthlyOut;
import com.sp.selfsp.attendance.monthly.service.AttendanceMonthlyService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 第六阶段月次汇总与月结服务实现。
 *
 * <p>负责把第五阶段已经稳定的日次结果按月聚合成月次结果，并支持月结、反结与导出。</p>
 */
@Service
public class AttendanceMonthlyServiceImpl implements AttendanceMonthlyService {

    // 默认页码用于月次列表首次打开时展示第一页。
    private static final int DEFAULT_PAGE = 1;
    // 默认页大小用于和前端共享分页器保持一致。
    private static final int DEFAULT_PAGE_SIZE = 20;
    // 产品允许的页大小固定为四档，保持和打卡、日次、异常处理一致。
    private static final List<Integer> ALLOWED_PAGE_SIZES = List.of(20, 50, 100, 200);
    // 月次统计项顺序用于稳定右侧详情展示顺序。
    private static final List<String> METRIC_CODES = List.of(
        "SCHEDULED_DAYS",
        "ATTENDANCE_DAYS",
        "NORMAL_DAYS",
        "LATE_COUNT",
        "EARLY_LEAVE_COUNT",
        "MISSING_PUNCH_COUNT",
        "ABSENCE_COUNT",
        "EXCEPTION_DAYS",
        "PAID_LEAVE_DAYS",
        "REST_DAYS"
    );
    // 月结状态常量统一收口，避免在多处散写字符串。
    private static final String CLOSE_STATUS_OPEN = "OPEN";
    private static final String CLOSE_STATUS_CLOSABLE = "CLOSABLE";
    private static final String CLOSE_STATUS_CLOSED = "CLOSED";
    private static final String CLOSE_STATUS_REOPENED = "REOPENED";

    // 第六阶段读取和写入月次主表、明细和动作日志。
    private final AttendanceMonthlyDao attendanceMonthlyDao;
    // 第四阶段日次服务用于在月次汇总前补齐当月日次结果。
    private final AttendanceDailyService attendanceDailyService;
    // JSON 工具用于写入月次指标来源和动作日志快照。
    private final ObjectMapper objectMapper;

    // 注入第六阶段 DAO、第四阶段日次服务和 JSON 工具，统一编排月次闭环。
    public AttendanceMonthlyServiceImpl(
        AttendanceMonthlyDao attendanceMonthlyDao,
        AttendanceDailyService attendanceDailyService,
        ObjectMapper objectMapper
    ) {
        this.attendanceMonthlyDao = attendanceMonthlyDao;
        this.attendanceDailyService = attendanceDailyService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AttendanceMonthlyOut.MonthlyListOut listMonthly(AttendanceMonthlyIn.MonthlyQueryIn queryIn) {
        // 统一补齐月份和分页默认值，避免月次页面第一次进入时打全量查询。
        AttendanceMonthlyIn.MonthlyQueryIn normalizedQuery = normalizeQuery(queryIn);
        // 列表前先补齐当前月份月次结果，确保中间列表始终看到最新月汇总。
        ensureMonthlyResults(normalizedQuery, false, "LIST_VIEW", null, "");
        int offset = (normalizedQuery.getPage() - 1) * normalizedQuery.getPageSize();
        List<AttendanceMonthlyOut.MonthlyItemOut> items = attendanceMonthlyDao.selectMonthlyList(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            normalizedQuery,
            offset,
            normalizedQuery.getPageSize()
        );
        Integer total = attendanceMonthlyDao.countMonthlyList(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery);
        AttendanceMonthlyOut.MonthlyListOut listOut = new AttendanceMonthlyOut.MonthlyListOut();
        listOut.setItems(items);
        listOut.setTotal(total == null ? 0 : total);
        listOut.setPage(normalizedQuery.getPage());
        listOut.setPageSize(normalizedQuery.getPageSize());
        listOut.setTotalPages(calculateTotalPages(total == null ? 0 : total, normalizedQuery.getPageSize()));
        listOut.setSummary(buildSummary(
            attendanceMonthlyDao.countMonthlySummary(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery)
        ));
        return listOut;
    }

    @Override
    public AttendanceMonthlyOut.MonthlyDetailOut getMonthlyDetail(Long monthlyId) {
        // 先读取基础详情，再拼接统计项、阻塞原因、动作日志和日次快照。
        AttendanceMonthlyOut.MonthlyDetailOut detailOut = attendanceMonthlyDao.selectMonthlyDetailById(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            monthlyId
        );
        if (detailOut == null) {
            throw new IllegalArgumentException("月次记录不存在");
        }
        detailOut.setItems(attendanceMonthlyDao.selectMonthlyItems(AttendanceTenantContext.DEFAULT_TENANT_ID, monthlyId));
        detailOut.setBlockReasons(buildBlockReasons(
            attendanceMonthlyDao.selectMonthlyBlockRows(AttendanceTenantContext.DEFAULT_TENANT_ID, monthlyId)
        ));
        detailOut.setActionLogs(attendanceMonthlyDao.selectMonthlyActionLogs(AttendanceTenantContext.DEFAULT_TENANT_ID, monthlyId));
        detailOut.setDailySnapshots(attendanceMonthlyDao.selectMonthlyDailySnapshots(AttendanceTenantContext.DEFAULT_TENANT_ID, monthlyId));
        return detailOut;
    }

    @Override
    @Transactional
    public AttendanceMonthlyOut.MonthlyRecalculateResultOut recalculateMonthly(AttendanceMonthlyIn.MonthlyRecalculateIn recalculateIn) {
        // 校验重算关键参数，避免空月份直接进入聚合。
        if (recalculateIn == null || !StringUtils.hasText(recalculateIn.getYearMonth())) {
            throw new IllegalArgumentException("yearMonth 不能为空");
        }
        AttendanceMonthlyIn.MonthlyQueryIn queryIn = new AttendanceMonthlyIn.MonthlyQueryIn();
        queryIn.setYearMonth(recalculateIn.getYearMonth());
        queryIn.setWorkplaceId(recalculateIn.getWorkplaceId());
        queryIn.setDepartmentId(recalculateIn.getDepartmentId());
        queryIn.setEmployeeKeyword("");
        if ("ONE".equalsIgnoreCase(recalculateIn.getRecalcMode()) && recalculateIn.getEmployeeId() == null) {
            throw new IllegalArgumentException("单人重算必须指定 employeeId");
        }
        AttendanceMonthlyIn.MonthlyQueryIn normalizedQuery = normalizeQuery(queryIn);
        if ("ONE".equalsIgnoreCase(recalculateIn.getRecalcMode())) {
            normalizedQuery.setEmployeeKeyword(null);
        }
        int successCount = ensureMonthlyResults(
            normalizedQuery,
            Boolean.TRUE.equals(recalculateIn.getOverwriteClosed()),
            "MANUAL_RECALCULATE",
            recalculateIn.getEmployeeId(),
            ""
        );
        AttendanceMonthlyOut.MonthlyRecalculateResultOut resultOut = new AttendanceMonthlyOut.MonthlyRecalculateResultOut();
        resultOut.setRequestedCount(successCount);
        resultOut.setSuccessCount(successCount);
        resultOut.setFailedCount(0);
        return resultOut;
    }

    @Override
    @Transactional
    public AttendanceMonthlyOut.MonthlyCloseResultOut closeMonthly(AttendanceMonthlyIn.MonthlyCloseIn closeIn) {
        // 月结必须给出月份和操作人，否则无法形成有效留痕。
        if (closeIn == null || !StringUtils.hasText(closeIn.getYearMonth()) || closeIn.getOperatorId() == null) {
            throw new IllegalArgumentException("yearMonth 和 operatorId 不能为空");
        }
        AttendanceMonthlyIn.MonthlyQueryIn queryIn = new AttendanceMonthlyIn.MonthlyQueryIn();
        queryIn.setYearMonth(closeIn.getYearMonth());
        if ("WORKPLACE".equalsIgnoreCase(closeIn.getScopeType())) {
          queryIn.setWorkplaceId(closeIn.getScopeId());
        }
        if ("DEPARTMENT".equalsIgnoreCase(closeIn.getScopeType())) {
          queryIn.setDepartmentId(closeIn.getScopeId());
        }
        // 月结前先确保月次结果已是最新状态，再检查阻塞。
        ensureMonthlyResults(normalizeQuery(queryIn), false, "PRE_CLOSE", null, closeIn.getComment());
        List<Map<String, Object>> scopeRows = attendanceMonthlyDao.selectMonthlyScopeRows(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            closeIn.getYearMonth(),
            closeIn.getScopeType(),
            closeIn.getScopeId()
        );
        // 月结前先整体检查一次阻塞状态，避免前几条已结、后几条阻塞时出现部分成功部分失败的脏状态。
        int blockedCount = 0;
        for (Map<String, Object> scopeRow : scopeRows) {
            String closeStatus = stringValue(mapValue(scopeRow, "closeStatus"));
            if (!CLOSE_STATUS_CLOSABLE.equals(closeStatus) && !CLOSE_STATUS_REOPENED.equals(closeStatus)) {
                blockedCount += 1;
            }
        }
        if (blockedCount > 0) {
            throw new IllegalStateException("当前范围仍存在不可月结记录，请先处理阻塞项");
        }
        // 只有整批都可结时才批量落库，确保第六阶段月结动作要么全部成功，要么全部回滚。
        int closedCount = 0;
        for (Map<String, Object> scopeRow : scopeRows) {
            Long monthlyId = longValue(mapValue(scopeRow, "id"));
            attendanceMonthlyDao.updateMonthlyCloseState(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                monthlyId,
                CLOSE_STATUS_CLOSED,
                LocalDateTime.now(),
                closeIn.getOperatorId(),
                null,
                null,
                closeIn.getComment()
            );
            attendanceMonthlyDao.insertMonthlyActionLog(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                monthlyId,
                "CLOSE",
                closeIn.getOperatorId(),
                closeIn.getComment(),
                toJsonString(Map.of("yearMonth", closeIn.getYearMonth(), "scopeType", closeIn.getScopeType()))
            );
            closedCount += 1;
        }
        AttendanceMonthlyOut.MonthlyCloseResultOut resultOut = new AttendanceMonthlyOut.MonthlyCloseResultOut();
        resultOut.setClosedCount(closedCount);
        resultOut.setBlockedCount(0);
        return resultOut;
    }

    @Override
    @Transactional
    public void reopenMonthly(AttendanceMonthlyIn.MonthlyReopenIn reopenIn) {
        // 反结必须明确目标主键、操作人和原因，避免无痕重开月结。
        if (reopenIn == null || reopenIn.getMonthlyId() == null || reopenIn.getOperatorId() == null || !StringUtils.hasText(reopenIn.getReason())) {
            throw new IllegalArgumentException("monthlyId、operatorId 和 reason 不能为空");
        }
        attendanceMonthlyDao.updateMonthlyCloseState(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            reopenIn.getMonthlyId(),
            CLOSE_STATUS_REOPENED,
            null,
            null,
            LocalDateTime.now(),
            reopenIn.getOperatorId(),
            reopenIn.getReason()
        );
        attendanceMonthlyDao.insertMonthlyActionLog(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            reopenIn.getMonthlyId(),
            "REOPEN",
            reopenIn.getOperatorId(),
            reopenIn.getReason(),
            toJsonString(Map.of("monthlyId", reopenIn.getMonthlyId(), "reason", reopenIn.getReason()))
        );
    }

    @Override
    @Transactional
    public AttendanceMonthlyOut.MonthlyExportOut exportMonthly(AttendanceMonthlyIn.MonthlyQueryIn queryIn) {
        // 导出前先补齐当前筛选范围月次结果，避免导出老数据。
        AttendanceMonthlyIn.MonthlyQueryIn normalizedQuery = normalizeQuery(queryIn);
        ensureMonthlyResults(normalizedQuery, false, "EXPORT", null, "");
        List<AttendanceMonthlyOut.MonthlyItemOut> items = attendanceMonthlyDao.selectMonthlyList(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            normalizedQuery,
            0,
            10000
        );
        StringBuilder builder = new StringBuilder();
        builder.append('\uFEFF');
        builder.append("yearMonth,employeeCode,employeeName,workplaceName,departmentName,scheduledDays,attendanceDays,normalDays,lateCount,earlyLeaveCount,missingPunchCount,absenceCount,exceptionDays,paidLeaveDays,restDays,closeStatus,blockReasonCount\n");
        for (AttendanceMonthlyOut.MonthlyItemOut item : items) {
            builder.append(csvValue(item.getYearMonth())).append(',')
                .append(csvValue(item.getEmployeeCode())).append(',')
                .append(csvValue(item.getEmployeeName())).append(',')
                .append(csvValue(item.getWorkplaceName())).append(',')
                .append(csvValue(item.getDepartmentName())).append(',')
                .append(csvValue(item.getScheduledDays())).append(',')
                .append(csvValue(item.getAttendanceDays())).append(',')
                .append(csvValue(item.getNormalDays())).append(',')
                .append(csvValue(item.getLateCount())).append(',')
                .append(csvValue(item.getEarlyLeaveCount())).append(',')
                .append(csvValue(item.getMissingPunchCount())).append(',')
                .append(csvValue(item.getAbsenceCount())).append(',')
                .append(csvValue(item.getExceptionDays())).append(',')
                .append(csvValue(item.getPaidLeaveDays())).append(',')
                .append(csvValue(item.getRestDays())).append(',')
                .append(csvValue(item.getCloseStatus())).append(',')
                .append(csvValue(item.getBlockReasonCount()))
                .append('\n');
        }
        AttendanceMonthlyOut.MonthlyExportOut exportOut = new AttendanceMonthlyOut.MonthlyExportOut();
        exportOut.setFileName("attendance-monthly-" + normalizedQuery.getYearMonth() + ".csv");
        exportOut.setContent(builder.toString());
        return exportOut;
    }

    // 统一把查询入参补齐默认月份与分页，供列表、重算和导出共用同一口径。
    private AttendanceMonthlyIn.MonthlyQueryIn normalizeQuery(AttendanceMonthlyIn.MonthlyQueryIn queryIn) {
        AttendanceMonthlyIn.MonthlyQueryIn normalized = queryIn == null ? new AttendanceMonthlyIn.MonthlyQueryIn() : queryIn;
        if (!StringUtils.hasText(normalized.getYearMonth())) {
            normalized.setYearMonth(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        int page = normalized.getPage() == null || normalized.getPage() < 1 ? DEFAULT_PAGE : normalized.getPage();
        int pageSize = normalized.getPageSize() == null ? DEFAULT_PAGE_SIZE : normalized.getPageSize();
        if (!ALLOWED_PAGE_SIZES.contains(pageSize)) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        normalized.setPage(page);
        normalized.setPageSize(pageSize);
        return normalized;
    }

    // 在月次汇总前先确保对应月份的日次结果已经生成，再聚合成本阶段月结果。
    private int ensureMonthlyResults(
        AttendanceMonthlyIn.MonthlyQueryIn queryIn,
        boolean overwriteClosed,
        String triggerType,
        Long employeeId,
        String remark
    ) {
        YearMonth yearMonth = parseYearMonth(queryIn.getYearMonth());
        AttendanceDailyIn.DailyRecalculateRangeIn dailyRangeIn = new AttendanceDailyIn.DailyRecalculateRangeIn();
        // 月次总是先把当月日次补齐，避免导入打卡后月次列表还看不到最新日结果。
        dailyRangeIn.setStartDate(yearMonth.atDay(1).toString());
        dailyRangeIn.setEndDate(yearMonth.atEndOfMonth().toString());
        dailyRangeIn.setWorkplaceId(queryIn.getWorkplaceId());
        dailyRangeIn.setDepartmentId(queryIn.getDepartmentId());
        dailyRangeIn.setEmployeeKeyword(queryIn.getEmployeeKeyword());
        dailyRangeIn.setExceptionOnly(false);
        attendanceDailyService.recalculateRange(dailyRangeIn);

        // 日次结果准备完后按员工聚合，生成月次主表和统计项。
        List<Map<String, Object>> rows = attendanceMonthlyDao.selectMonthlyAggregationRows(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            buildMonthlyAggregationQuery(queryIn, employeeId)
        );
        Map<Long, List<Map<String, Object>>> groupedRows = rows.stream().collect(Collectors.groupingBy(
            row -> longValue(mapValue(row, "employeeId")),
            LinkedHashMap::new,
            Collectors.toList()
        ));
        int successCount = 0;
        for (List<Map<String, Object>> employeeRows : groupedRows.values()) {
            aggregateOneEmployeeMonth(yearMonth, employeeRows, overwriteClosed, triggerType, remark);
            successCount += 1;
        }
        return successCount;
    }

    // 单个员工单个月的聚合过程统一收口在这里，便于后续扩展工资和工时口径。
    private void aggregateOneEmployeeMonth(
        YearMonth yearMonth,
        List<Map<String, Object>> employeeRows,
        boolean overwriteClosed,
        String triggerType,
        String remark
    ) {
        Map<String, Object> firstRow = employeeRows.get(0);
        Long employeeId = longValue(mapValue(firstRow, "employeeId"));
        Map<String, Object> existing = attendanceMonthlyDao.selectMonthlyIdentity(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            yearMonth.toString(),
            employeeId
        );
        String existingCloseStatus = stringValue(mapValue(existing, "closeStatus"));
        if (CLOSE_STATUS_CLOSED.equals(existingCloseStatus) && !overwriteClosed) {
            return;
        }

        MonthlyAggregation aggregation = new MonthlyAggregation();
        aggregation.employeeId = employeeId;
        aggregation.workplaceId = longValue(mapValue(firstRow, "workplaceId"));
        aggregation.departmentId = longValue(mapValue(firstRow, "departmentId"));
        aggregation.employeeCode = stringValue(mapValue(firstRow, "employeeCode"));
        aggregation.employeeName = stringValue(mapValue(firstRow, "employeeName"));
        aggregation.workplaceName = stringValue(mapValue(firstRow, "workplaceName"));
        aggregation.departmentName = stringValue(mapValue(firstRow, "departmentName"));

        for (Map<String, Object> row : employeeRows) {
            LocalDate workDate = localDateValue(mapValue(row, "workDate"));
            String effectiveStatus = resolveEffectiveStatus(row);
            String handlingStatus = stringValue(mapValue(row, "handlingStatus"));
            boolean exceptionFlag = booleanValue(mapValue(row, "exceptionFlag"));
            boolean lockedFlag = booleanValue(mapValue(row, "lockedFlag"));
            String workDayType = stringValue(mapValue(row, "workDayType"));

            if (mapValue(row, "shiftScheduleId") != null) {
                aggregation.scheduledDays += 1;
                aggregation.metricSources.computeIfAbsent("SCHEDULED_DAYS", ignored -> new ArrayList<>()).add(workDate.toString());
            }
            if (isAttendanceStatus(effectiveStatus)) {
                aggregation.attendanceDays += 1;
                aggregation.metricSources.computeIfAbsent("ATTENDANCE_DAYS", ignored -> new ArrayList<>()).add(workDate + ":" + effectiveStatus);
            }
            if ("NORMAL".equals(effectiveStatus)) {
                aggregation.normalDays += 1;
                aggregation.metricSources.computeIfAbsent("NORMAL_DAYS", ignored -> new ArrayList<>()).add(workDate.toString());
            }
            if ("LATE".equals(effectiveStatus)) {
                aggregation.lateCount += 1;
                aggregation.metricSources.computeIfAbsent("LATE_COUNT", ignored -> new ArrayList<>()).add(workDate.toString());
            }
            if ("EARLY_LEAVE".equals(effectiveStatus)) {
                aggregation.earlyLeaveCount += 1;
                aggregation.metricSources.computeIfAbsent("EARLY_LEAVE_COUNT", ignored -> new ArrayList<>()).add(workDate.toString());
            }
            if ("MISSING_CLOCK_IN".equals(effectiveStatus) || "MISSING_CLOCK_OUT".equals(effectiveStatus)) {
                aggregation.missingPunchCount += 1;
                aggregation.metricSources.computeIfAbsent("MISSING_PUNCH_COUNT", ignored -> new ArrayList<>()).add(workDate + ":" + effectiveStatus);
            }
            if ("ABSENCE".equals(effectiveStatus)) {
                aggregation.absenceCount += 1;
                aggregation.metricSources.computeIfAbsent("ABSENCE_COUNT", ignored -> new ArrayList<>()).add(workDate.toString());
            }
            if (exceptionFlag || !"RESOLVED".equals(handlingStatus)) {
                aggregation.exceptionDays += 1;
                aggregation.metricSources.computeIfAbsent("EXCEPTION_DAYS", ignored -> new ArrayList<>()).add(workDate + ":" + handlingStatus);
            }
            if ("PAID_LEAVE".equals(workDayType) || "PAID_LEAVE".equals(effectiveStatus)) {
                aggregation.paidLeaveDays = aggregation.paidLeaveDays.add(BigDecimal.ONE);
                aggregation.metricSources.computeIfAbsent("PAID_LEAVE_DAYS", ignored -> new ArrayList<>()).add(workDate.toString());
            }
            if ("REST".equals(workDayType) || "REST".equals(effectiveStatus)) {
                aggregation.restDays = aggregation.restDays.add(BigDecimal.ONE);
                aggregation.metricSources.computeIfAbsent("REST_DAYS", ignored -> new ArrayList<>()).add(workDate.toString());
            }
            appendBlockReasons(aggregation.blockReasons, workDate, handlingStatus, exceptionFlag, lockedFlag);
        }

        String nextCloseStatus = resolveCloseStatus(existingCloseStatus, aggregation.blockReasons.size());
        LocalDateTime now = LocalDateTime.now();
        attendanceMonthlyDao.mergeMonthly(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            yearMonth.toString(),
            employeeId,
            aggregation.workplaceId,
            aggregation.departmentId,
            aggregation.scheduledDays,
            aggregation.attendanceDays,
            aggregation.normalDays,
            aggregation.lateCount,
            aggregation.earlyLeaveCount,
            aggregation.missingPunchCount,
            aggregation.absenceCount,
            aggregation.exceptionDays,
            aggregation.paidLeaveDays.setScale(2, RoundingMode.HALF_UP),
            aggregation.restDays.setScale(2, RoundingMode.HALF_UP),
            nextCloseStatus,
            aggregation.blockReasons.size(),
            StringUtils.hasText(remark) ? remark : stringValue(mapValue(existing, "remark")),
            localDateTimeValue(mapValue(existing, "closedAt")),
            longValue(mapValue(existing, "closedBy")),
            localDateTimeValue(mapValue(existing, "reopenedAt")),
            longValue(mapValue(existing, "reopenedBy")),
            now,
            now
        );
        Map<String, Object> identity = attendanceMonthlyDao.selectMonthlyIdentity(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            yearMonth.toString(),
            employeeId
        );
        Long monthlyId = longValue(mapValue(identity, "id"));
        attendanceMonthlyDao.deleteMonthlyItems(AttendanceTenantContext.DEFAULT_TENANT_ID, monthlyId);
        insertMetricItems(monthlyId, aggregation);
        attendanceMonthlyDao.insertMonthlyActionLog(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            monthlyId,
            triggerType,
            9001L,
            remark,
            toJsonString(Map.of(
                "employeeId", employeeId,
                "yearMonth", yearMonth.toString(),
                "blockReasonCount", aggregation.blockReasons.size(),
                "closeStatus", nextCloseStatus
            ))
        );
    }

    // 把本次聚合生成的指标快照写入月次明细表，供右侧详情直接解释统计来源。
    private void insertMetricItems(Long monthlyId, MonthlyAggregation aggregation) {
        Map<String, String> itemNameMap = Map.of(
            "SCHEDULED_DAYS", "排班天数",
            "ATTENDANCE_DAYS", "出勤天数",
            "NORMAL_DAYS", "正常天数",
            "LATE_COUNT", "迟到次数",
            "EARLY_LEAVE_COUNT", "早退次数",
            "MISSING_PUNCH_COUNT", "缺卡次数",
            "ABSENCE_COUNT", "缺勤次数",
            "EXCEPTION_DAYS", "异常天数",
            "PAID_LEAVE_DAYS", "有休日数",
            "REST_DAYS", "休息日数"
        );
        Map<String, String> itemValueMap = new LinkedHashMap<>();
        itemValueMap.put("SCHEDULED_DAYS", String.valueOf(aggregation.scheduledDays));
        itemValueMap.put("ATTENDANCE_DAYS", String.valueOf(aggregation.attendanceDays));
        itemValueMap.put("NORMAL_DAYS", String.valueOf(aggregation.normalDays));
        itemValueMap.put("LATE_COUNT", String.valueOf(aggregation.lateCount));
        itemValueMap.put("EARLY_LEAVE_COUNT", String.valueOf(aggregation.earlyLeaveCount));
        itemValueMap.put("MISSING_PUNCH_COUNT", String.valueOf(aggregation.missingPunchCount));
        itemValueMap.put("ABSENCE_COUNT", String.valueOf(aggregation.absenceCount));
        itemValueMap.put("EXCEPTION_DAYS", String.valueOf(aggregation.exceptionDays));
        itemValueMap.put("PAID_LEAVE_DAYS", aggregation.paidLeaveDays.setScale(2, RoundingMode.HALF_UP).toPlainString());
        itemValueMap.put("REST_DAYS", aggregation.restDays.setScale(2, RoundingMode.HALF_UP).toPlainString());
        int order = 1;
        for (String metricCode : METRIC_CODES) {
            attendanceMonthlyDao.insertMonthlyItem(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                monthlyId,
                metricCode,
                itemNameMap.get(metricCode),
                itemValueMap.get(metricCode),
                order,
                toJsonString(aggregation.metricSources.getOrDefault(metricCode, List.of()))
            );
            order += 1;
        }
    }

    // 月次汇总卡按状态分桶，供前端顶部统计卡复用。
    private AttendanceMonthlyOut.MonthlySummaryOut buildSummary(List<Map<String, Object>> summaryRows) {
        AttendanceMonthlyOut.MonthlySummaryOut summaryOut = new AttendanceMonthlyOut.MonthlySummaryOut();
        summaryOut.setOpenCount(0);
        summaryOut.setClosableCount(0);
        summaryOut.setClosedCount(0);
        summaryOut.setReopenedCount(0);
        for (Map<String, Object> summaryRow : summaryRows) {
            String closeStatus = stringValue(mapValue(summaryRow, "closeStatus"));
            int total = intValue(mapValue(summaryRow, "total"));
            if (CLOSE_STATUS_OPEN.equals(closeStatus)) {
                summaryOut.setOpenCount(total);
            } else if (CLOSE_STATUS_CLOSABLE.equals(closeStatus)) {
                summaryOut.setClosableCount(total);
            } else if (CLOSE_STATUS_CLOSED.equals(closeStatus)) {
                summaryOut.setClosedCount(total);
            } else if (CLOSE_STATUS_REOPENED.equals(closeStatus)) {
                summaryOut.setReopenedCount(total);
            }
        }
        return summaryOut;
    }

    // 阻塞原因从日次原始行重建成可读清单，保证前端能直接展示给用户。
    private List<AttendanceMonthlyOut.BlockReasonOut> buildBlockReasons(List<Map<String, Object>> blockRows) {
        List<AttendanceMonthlyOut.BlockReasonOut> reasons = new ArrayList<>();
        for (Map<String, Object> blockRow : blockRows) {
            appendBlockReasons(
                reasons,
                localDateValue(mapValue(blockRow, "workDate")),
                stringValue(mapValue(blockRow, "handlingStatus")),
                booleanValue(mapValue(blockRow, "exceptionFlag")),
                booleanValue(mapValue(blockRow, "lockedFlag"))
            );
        }
        return reasons;
    }

    // 统一根据日次状态追加阻塞原因，避免列表和详情出现两套口径。
    private void appendBlockReasons(
        List<AttendanceMonthlyOut.BlockReasonOut> reasons,
        LocalDate workDate,
        String handlingStatus,
        boolean exceptionFlag,
        boolean lockedFlag
    ) {
        if (!"RESOLVED".equals(handlingStatus)) {
            reasons.add(blockReason("UNRESOLVED_DAILY", workDate, "当日日次仍未处理完成"));
        }
        if (exceptionFlag) {
            reasons.add(blockReason("UNRESOLVED_EXCEPTION", workDate, "当日仍存在未闭环异常"));
        }
        if (exceptionFlag && !lockedFlag) {
            reasons.add(blockReason("UNLOCKED_EXCEPTION", workDate, "当日异常已处理但仍未锁定"));
        }
    }

    // 构造一条阻塞原因对象，统一收口 code/message/date。
    private AttendanceMonthlyOut.BlockReasonOut blockReason(String code, LocalDate workDate, String message) {
        AttendanceMonthlyOut.BlockReasonOut blockReasonOut = new AttendanceMonthlyOut.BlockReasonOut();
        blockReasonOut.setBlockCode(code);
        blockReasonOut.setWorkDate(workDate);
        blockReasonOut.setBlockMessage(message);
        return blockReasonOut;
    }

    // 有效出勤状态统一集中判断，避免 attendanceDays 在多处散开维护。
    private boolean isAttendanceStatus(String status) {
        return List.of("NORMAL", "LATE", "EARLY_LEAVE", "HOLIDAY_WORK").contains(status);
    }

    // 月次状态优先保留 CLOSED，其余按阻塞数量自动推导为可结或未结。
    private String resolveCloseStatus(String existingCloseStatus, int blockReasonCount) {
        if (CLOSE_STATUS_CLOSED.equals(existingCloseStatus)) {
            return CLOSE_STATUS_CLOSED;
        }
        if (CLOSE_STATUS_REOPENED.equals(existingCloseStatus)) {
            return blockReasonCount == 0 ? CLOSE_STATUS_REOPENED : CLOSE_STATUS_OPEN;
        }
        return blockReasonCount == 0 ? CLOSE_STATUS_CLOSABLE : CLOSE_STATUS_OPEN;
    }

    // 第六阶段统计优先使用 final_status，缺失时回退第四阶段原始 status。
    private String resolveEffectiveStatus(Map<String, Object> row) {
        String finalStatus = stringValue(mapValue(row, "finalStatus"));
        return StringUtils.hasText(finalStatus) ? finalStatus : stringValue(mapValue(row, "status"));
    }

    // 兼容 MyBatis HashMap 在不同数据库和驱动下返回驼峰、下划线或大写键名，避免服务层直接读空。
    private Object mapValue(Map<String, Object> row, String key) {
        if (row == null || key == null) {
            return null;
        }
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String snakeKey = key.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        if (row.containsKey(snakeKey)) {
            return row.get(snakeKey);
        }
        String upperCamelKey = key.toUpperCase(Locale.ROOT);
        if (row.containsKey(upperCamelKey)) {
            return row.get(upperCamelKey);
        }
        String upperSnakeKey = snakeKey.toUpperCase(Locale.ROOT);
        if (row.containsKey(upperSnakeKey)) {
            return row.get(upperSnakeKey);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    // 聚合查询在 employeeId 维度下重用同一 QueryIn，避免为了单人重算再造一套 SQL 参数对象。
    private AttendanceMonthlyIn.MonthlyQueryIn buildMonthlyAggregationQuery(AttendanceMonthlyIn.MonthlyQueryIn queryIn, Long employeeId) {
        AttendanceMonthlyIn.MonthlyQueryIn aggregationQuery = new AttendanceMonthlyIn.MonthlyQueryIn();
        aggregationQuery.setYearMonth(queryIn.getYearMonth());
        aggregationQuery.setWorkplaceId(queryIn.getWorkplaceId());
        aggregationQuery.setDepartmentId(queryIn.getDepartmentId());
        aggregationQuery.setEmployeeKeyword(queryIn.getEmployeeKeyword());
        // 单人月次重算时直接把 employeeId 带进 SQL 参数，避免多算当前范围外员工。
        aggregationQuery.setEmployeeId(employeeId);
        return aggregationQuery;
    }

    // 年月解析统一在这里处理，避免多处散写 try/catch。
    private YearMonth parseYearMonth(String value) {
        try {
            return YearMonth.parse(value, DateTimeFormatter.ofPattern("yyyy-MM", Locale.ROOT));
        } catch (DateTimeParseException error) {
            throw new IllegalArgumentException("yearMonth 格式必须为 YYYY-MM");
        }
    }

    // 总页数换算统一集中处理，保持各阶段分页口径一致。
    private int calculateTotalPages(int total, int pageSize) {
        return Math.max(1, (int) Math.ceil(total / (double) pageSize));
    }

    // 把对象安全转成 JSON，失败时退回字符串，避免动作日志因为序列化失败直接中断主流程。
    private String toJsonString(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException error) {
            return String.valueOf(payload);
        }
    }

    // CSV 字段统一做空值兜底和引号转义，保证导出内容可直接打开。
    private String csvValue(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    // 统一把对象转成 long，供服务层安全读取 MyBatis map 结果。
    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    // 统一把对象转成 int，供服务层安全读取数量结果。
    private int intValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    // 统一把对象转成字符串，避免多处手写 null 判断。
    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    // 统一把对象转成日期，兼容 JDBC LocalDate 和字符串两类来源。
    private LocalDate localDateValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        return LocalDate.parse(String.valueOf(value));
    }

    // 统一把对象转成日期时间，兼容 JDBC 时间字段和空值场景。
    private LocalDateTime localDateTimeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
    }

    // 统一把对象转成布尔值，兼容 H2 tinyint 和 Java boolean 两类来源。
    private boolean booleanValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Objects.equals("true", String.valueOf(value).toLowerCase(Locale.ROOT));
    }

    // 月次聚合中间态统一收在这里，避免在主流程里散落大量局部变量。
    private static final class MonthlyAggregation {
        private Long employeeId;
        private Long workplaceId;
        private Long departmentId;
        private String employeeCode;
        private String employeeName;
        private String workplaceName;
        private String departmentName;
        private int scheduledDays;
        private int attendanceDays;
        private int normalDays;
        private int lateCount;
        private int earlyLeaveCount;
        private int missingPunchCount;
        private int absenceCount;
        private int exceptionDays;
        private BigDecimal paidLeaveDays = BigDecimal.ZERO;
        private BigDecimal restDays = BigDecimal.ZERO;
        private Map<String, List<String>> metricSources = new LinkedHashMap<>();
        private List<AttendanceMonthlyOut.BlockReasonOut> blockReasons = new ArrayList<>();
    }
}
