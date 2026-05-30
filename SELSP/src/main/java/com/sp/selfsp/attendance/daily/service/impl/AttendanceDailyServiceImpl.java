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
    // 计算版本用于记录当前日次结果已经切换到第七阶段日本规则增强口径。
    private static final String CALC_VERSION = "phase7-v1";
    // 法定休日使用统一常量，避免数据库状态值和日志说明出现多种拼写。
    private static final String HOLIDAY_TYPE_LEGAL = "LEGAL_HOLIDAY";
    // 所定休日使用统一常量，供日次和月次层复用同一口径。
    private static final String HOLIDAY_TYPE_SCHEDULED = "SCHEDULED_HOLIDAY";

    // 读取和写入日次、异常、计算日志以及排班和打卡快照。
    private final AttendanceDailyDao attendanceDailyDao;
    // JSON 序列化器用于把计算过程上下文写入日志。
    private final ObjectMapper objectMapper;
    // 共享计算器统一承接原始打卡初算和审批后重算的规则算法，避免两套链路分叉维护。
    private final AttendanceDailyMetricCalculator dailyMetricCalculator;

    // 注入第四阶段所需 DAO 与 JSON 工具，统一处理日次计算编排。
    public AttendanceDailyServiceImpl(
        AttendanceDailyDao attendanceDailyDao,
        ObjectMapper objectMapper
    ) {
        this.attendanceDailyDao = attendanceDailyDao;
        this.objectMapper = objectMapper;
        this.dailyMetricCalculator = new AttendanceDailyMetricCalculator();
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
        // 列表主体直接承接当前页结果，供第四阶段中间表格渲染日次行。
        listOut.setItems(items);
        // 总条数始终回填成非空整数，避免分页栏对 null 做二次兜底。
        listOut.setTotal(total == null ? 0 : total);
        // 当前页码沿用规范化后的查询口径，保证回翻页时前后端一致。
        listOut.setPage(normalizedQuery.getPage());
        // 每页条数同样沿用规范化结果，供分页条稳定显示当前档位。
        listOut.setPageSize(normalizedQuery.getPageSize());
        // 总页数在服务层统一换算完成，避免前端针对空数据重复实现同一算法。
        listOut.setTotalPages(calculateTotalPages(total == null ? 0 : total, normalizedQuery.getPageSize()));
        // 顶部摘要在返回前同步聚合，确保列表与统计卡读取的是同一批筛选结果。
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
                // 每个候选员工日期都走完整单天重算，保证批量重算与单日重算共用同一算法入口。
                recalculateOneDay(
                    longValue(readMapValue(target, "employeeId")),
                    localDateValue(readMapValue(target, "workDate")),
                    "BATCH_RECALCULATE"
                );
                // 单条重算成功后立即累计成功数，供前端汇总本次批量刷新结果。
                successCount += 1;
            } catch (RuntimeException error) {
                // 任一目标失败只增加失败计数，不打断整批流程，避免一条坏数据拖垮整个筛选范围。
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

    @Override
    public Map<String, Object> getDailyMeta(Long dailyId) {
        // 第五阶段审批流统一通过服务层读取日次元数据，避免上层直接依赖 DAO 细节。
        return requireDailyMeta(dailyId);
    }

    @Override
    @Transactional
    public void markDailyInReview(Long dailyId) {
        // 建单后把日次推进到审核中，保持异常中心列表和审批流状态同步。
        attendanceDailyDao.updateDailyHandlingState(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyId,
            "IN_REVIEW",
            "SUBMITTED"
        );
    }

    @Override
    @Transactional
    public void markDailyReturned(Long dailyId) {
        // 退回补充时仍属于审核链路内部，日次处理状态继续保持审核中。
        attendanceDailyDao.updateDailyHandlingState(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyId,
            "IN_REVIEW",
            "RETURNED"
        );
    }

    @Override
    @Transactional
    public void markDailyRejected(Long dailyId) {
        // 驳回后重新回到待处理状态，方便申请方重新发起处理单。
        attendanceDailyDao.updateDailyHandlingState(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyId,
            "UNHANDLED",
            "REJECTED"
        );
    }

    @Override
    @Transactional
    public void applyApprovedResolution(
        Long dailyId,
        Long approvedCaseId,
        LocalDateTime finalClockIn,
        LocalDateTime finalClockOut,
        Integer finalBreakMinutes,
        String finalStatus,
        String finalRemark,
        Boolean finalExceptionFlag
    ) {
        // 审批通过后的最终结果回写和增强分钟刷新统一由日次服务收口，避免审批服务直接掌握持久化细节。
        attendanceDailyDao.updateDailyFinalResult(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyId,
            "RESOLVED",
            "APPROVED",
            finalStatus,
            finalClockIn,
            finalClockOut,
            finalBreakMinutes,
            finalRemark,
            approvedCaseId,
            finalExceptionFlag
        );
        refreshResolvedDailyMetrics(
            dailyId,
            finalClockIn,
            finalClockOut,
            finalBreakMinutes,
            finalStatus,
            finalExceptionFlag
        );
    }

    @Override
    @Transactional
    public void refreshResolvedDailyMetrics(
        Long dailyId,
        LocalDateTime finalClockIn,
        LocalDateTime finalClockOut,
        Integer finalBreakMinutes,
        String finalStatus,
        Boolean finalExceptionFlag
    ) {
        // 审批改完最终业务时间后，统一按最终结果重算第七阶段增强分钟，避免月次和规则页继续读取旧值。
        AttendanceDailyOut.DailyDetailOut dailyDetail = requireDailyDetail(dailyId);
        AttendanceDailyOut.ScheduleSnapshotOut schedule = attendanceDailyDao.selectScheduleSnapshot(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyDetail.getEmployeeId(),
            dailyDetail.getWorkDate()
        );
        Map<String, Object> applicableRule = attendanceDailyDao.selectApplicableRule(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyDetail.getEmployeeId(),
            dailyDetail.getWorkDate()
        );
        AttendanceDailyMetricCalculator.CalculationResult resolvedResult = dailyMetricCalculator.calculateFromResolvedFinal(
            schedule,
            applicableRule,
            finalClockIn,
            finalClockOut,
            finalBreakMinutes
        );
        // 审批补丁如果显式给了最终状态，就以审批结论为准；否则沿用规则重算后的状态。
        if (StringUtils.hasText(finalStatus)) {
            resolvedResult.status = finalStatus.trim();
        }
        // 审批是否保留异常标记由处理单补丁决定，不能让自动重算把审批结论再覆盖掉。
        resolvedResult.exceptionFlag = Boolean.TRUE.equals(finalExceptionFlag);
        resolvedResult.calcMessage = "审批修改最终时间后已联动刷新增强分钟。";
        attendanceDailyDao.updateDailyResolvedMetrics(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyId,
            // 最终休息分钟写回 resolved 结果，保证审批结论成为后续月次和规则页唯一口径。
            resolvedResult.finalBreakMinutes,
            // 实际工时和迟到早退分钟同步回写，避免审批后详情仍显示旧日次结论。
            resolvedResult.actualWorkMinutes,
            resolvedResult.lateMinutes,
            resolvedResult.earlyLeaveMinutes,
            // 正常、残业、法定外残业、深夜和休日分钟一起回写，保证第六阶段月次聚合能直接复用。
            resolvedResult.normalWorkMinutes,
            resolvedResult.overtimeMinutes,
            resolvedResult.legalOvertimeMinutes,
            resolvedResult.nightWorkMinutes,
            resolvedResult.holidayWorkMinutes,
            // 休日类型和命中规则同批持久化，避免规则页与日次详情出现字段错位。
            resolvedResult.holidayType,
            resolvedResult.appliedRuleId,
            resolvedResult.status,
            resolvedResult.exceptionFlag,
            CALC_VERSION,
            resolvedResult.calcMessage
        );
        // 额外留一条审批后重算日志，方便第六阶段和第七阶段联查最终业务时间为何变化。
        attendanceDailyDao.insertCalcLog(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            dailyDetail.getEmployeeId(),
            dailyDetail.getWorkDate(),
            dailyId,
            "APPROVED_PATCH",
            null,
            "SUCCESS",
            "FINAL_PATCH_RECALC",
            "审批修改最终时间后已刷新增强分钟：正常 " + resolvedResult.normalWorkMinutes + "，残业 " + resolvedResult.overtimeMinutes + "，深夜 " + resolvedResult.nightWorkMinutes + "。",
            toJsonString(
                Map.of(
                    "dailyId", dailyId,
                    "finalClockIn", stringValue(finalClockIn),
                    "finalClockOut", stringValue(finalClockOut),
                    "finalBreakMinutes", resolvedResult.finalBreakMinutes,
                    "status", resolvedResult.status
                )
            )
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
        // 第七阶段按员工和业务日期读取生效规则，把自动休息、深夜时段和取整口径带入日次算法。
        Map<String, Object> applicableRule = attendanceDailyDao.selectApplicableRule(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeId,
            workDate
        );
        List<AttendanceDailyOut.PunchSnapshotOut> punches = attendanceDailyDao.selectProcessedPunches(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeId,
            workDate
        );

        AttendanceDailyMetricCalculator.CalculationResult calculationResult = dailyMetricCalculator.calculateFromPunches(
            schedule,
            punches,
            applicableRule
        );
        // 初算结果统一落入日次主表，确保排班、打卡、规则和增强分钟在同一条日次上收口。
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
            calculationResult.finalBreakMinutes,
            calculationResult.actualWorkMinutes,
            calculationResult.lateMinutes,
            calculationResult.earlyLeaveMinutes,
            calculationResult.absenceMinutes,
            calculationResult.normalWorkMinutes,
            calculationResult.overtimeMinutes,
            calculationResult.legalOvertimeMinutes,
            calculationResult.nightWorkMinutes,
            calculationResult.holidayWorkMinutes,
            calculationResult.holidayType,
            calculationResult.appliedRuleId,
            calculationResult.status,
            "NONE",
            calculationResult.exceptionFlag ? "UNHANDLED" : "RESOLVED",
            calculationResult.exceptionFlag,
            false,
            LocalDateTime.now(),
            CALC_VERSION,
            calculationResult.calcMessage
        );

        // 合并后立即回读主键，供异常表和计算日志表继续挂接到同一条日次记录。
        Map<String, Object> identity = attendanceDailyDao.selectDailyIdentity(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeId,
            workDate
        );
        Long dailyId = longValue(readMapValue(identity, "id"));
        // 每次重算前先清掉旧异常，避免上一次判定结果残留到本轮新口径。
        attendanceDailyDao.deleteExceptionsByDailyId(AttendanceTenantContext.DEFAULT_TENANT_ID, dailyId);
        for (AttendanceDailyMetricCalculator.ExceptionPlan exceptionPlan : calculationResult.exceptions) {
            // 本轮算法产出的异常计划逐条落库，供第五阶段异常中心直接建单或继续处理。
            attendanceDailyDao.insertException(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                employeeId,
                workDate,
                dailyId,
                exceptionPlan.exceptionType(),
                exceptionPlan.exceptionLevel(),
                "OPEN",
                exceptionPlan.message(),
                exceptionPlan.suggestedAction()
            );
        }
        // 计算日志也先全量替换，保证详情页展示的每一步都对应本轮最新算法。
        attendanceDailyDao.deleteCalcLogsByEmployeeDate(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeId, workDate);
        int stepIndex = 1;
        for (String logLine : calculationResult.logs) {
            // 逐条写入计算步骤日志，方便第四阶段详情和后续排障追溯每个业务判断。
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
            // 每行先还原数据库状态码和对应数量，后续统一映射到顶部四张摘要卡。
            String status = stringValue(readMapValue(row, "status"));
            int total = intValue(readMapValue(row, "totalCount"));
            if (Objects.equals("NORMAL", status)) {
                // 正常类状态直接累计到正常卡。
                summaryOut.setNormalCount(summaryOut.getNormalCount() + total);
            } else if (Objects.equals("LATE", status) || Objects.equals("EARLY_LEAVE", status)) {
                // 迟到与早退都属于“出勤异常但有人上班”的范畴，因此汇总到同一张异常卡。
                summaryOut.setLateCount(summaryOut.getLateCount() + total);
            } else if (Objects.equals("MISSING_CLOCK_IN", status) || Objects.equals("MISSING_CLOCK_OUT", status)) {
                // 缺上班卡和缺下班卡同属缺卡场景，统一累计到缺卡卡片。
                summaryOut.setMissingClockCount(summaryOut.getMissingClockCount() + total);
            } else if (Objects.equals("ABSENCE", status)) {
                // 缺勤单独统计，避免与缺卡或迟到混淆。
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

}
