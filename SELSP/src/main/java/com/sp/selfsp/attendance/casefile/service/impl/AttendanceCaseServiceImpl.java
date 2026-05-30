package com.sp.selfsp.attendance.casefile.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.selfsp.attendance.casefile.dao.AttendanceCaseDao;
import com.sp.selfsp.attendance.casefile.domain.in.AttendanceCaseIn;
import com.sp.selfsp.attendance.casefile.domain.out.AttendanceCaseOut;
import com.sp.selfsp.attendance.casefile.service.AttendanceCaseService;
import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.daily.domain.in.AttendanceDailyIn;
import com.sp.selfsp.attendance.daily.domain.out.AttendanceDailyOut;
import com.sp.selfsp.attendance.daily.service.AttendanceDailyService;
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
 * 第五阶段异常处理与审批服务实现。
 *
 * <p>负责把第四阶段的异常日次包装成处理单、审批流和锁定后的最终业务结论。</p>
 */
@Service
public class AttendanceCaseServiceImpl implements AttendanceCaseService {

    // 默认页码用于异常中心首次进入时先看第一页待处理列表。
    private static final int DEFAULT_PAGE = 1;
    // 默认页大小用于控制第五阶段列表返回量。
    private static final int DEFAULT_PAGE_SIZE = 20;
    // 第五阶段分页沿用固定档位，避免前端传任意数字。
    private static final List<Integer> ALLOWED_PAGE_SIZES = List.of(20, 50, 100, 200);

    // 读取和写入第五阶段处理单主表与动作日志。
    private final AttendanceCaseDao attendanceCaseDao;
    // 读取第四阶段日次详情，供第五阶段详情页复用。
    private final AttendanceDailyService attendanceDailyService;
    // JSON 序列化器用于记录审批前后快照和补丁。
    private final ObjectMapper objectMapper;

    // 注入第五阶段所需 DAO、第四阶段服务和 JSON 工具。
    public AttendanceCaseServiceImpl(
        AttendanceCaseDao attendanceCaseDao,
        AttendanceDailyService attendanceDailyService,
        ObjectMapper objectMapper
    ) {
        this.attendanceCaseDao = attendanceCaseDao;
        this.attendanceDailyService = attendanceDailyService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AttendanceCaseOut.CaseListOut listCases(AttendanceCaseIn.CaseQueryIn queryIn) {
        // 统一补齐默认日期范围与分页配置，让列表、统计和详情入口共用同一口径。
        AttendanceCaseIn.CaseQueryIn normalizedQuery = normalizeQuery(queryIn);
        // 第五阶段入口也要先补齐缺失日次，否则用户不先点第四阶段页面就会看到空列表。
        AttendanceDailyIn.DailyQueryIn dailyQueryIn = new AttendanceDailyIn.DailyQueryIn();
        dailyQueryIn.setStartDate(normalizedQuery.getStartDate());
        dailyQueryIn.setEndDate(normalizedQuery.getEndDate());
        dailyQueryIn.setWorkplaceId(normalizedQuery.getWorkplaceId());
        dailyQueryIn.setDepartmentId(normalizedQuery.getDepartmentId());
        dailyQueryIn.setEmployeeKeyword(normalizedQuery.getEmployeeKeyword());
        dailyQueryIn.setPage(1);
        dailyQueryIn.setPageSize(DEFAULT_PAGE_SIZE);
        attendanceDailyService.listDailyResults(dailyQueryIn);
        int offset = (normalizedQuery.getPage() - 1) * normalizedQuery.getPageSize();
        List<AttendanceCaseOut.CaseItemOut> items = attendanceCaseDao.selectCaseList(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            normalizedQuery,
            offset,
            normalizedQuery.getPageSize()
        );
        Integer total = attendanceCaseDao.countCaseList(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery);
        AttendanceCaseOut.CaseListOut listOut = new AttendanceCaseOut.CaseListOut();
        listOut.setItems(items);
        listOut.setTotal(total == null ? 0 : total);
        listOut.setPage(normalizedQuery.getPage());
        listOut.setPageSize(normalizedQuery.getPageSize());
        listOut.setTotalPages(calculateTotalPages(total == null ? 0 : total, normalizedQuery.getPageSize()));
        listOut.setSummary(buildSummary(attendanceCaseDao.countCaseSummary(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery)));
        return listOut;
    }

    @Override
    public AttendanceCaseOut.CaseDetailOut getCaseDetail(Long caseId) {
        // 第五阶段详情必须建立在真实处理单之上，未建单异常先在列表里引导用户建单。
        AttendanceCaseOut.CaseDetailOut detailOut = requireCaseDetail(caseId);
        detailOut.setDailyDetail(attendanceDailyService.getDailyDetail(detailOut.getAttendanceDailyId()));
        detailOut.setActionLogs(attendanceCaseDao.selectCaseActionLogs(AttendanceTenantContext.DEFAULT_TENANT_ID, caseId));
        detailOut.setAvailableActions(buildAvailableActions(detailOut));
        return detailOut;
    }

    @Override
    @Transactional
    public AttendanceCaseOut.CaseMutationOut createCase(AttendanceCaseIn.CaseCreateIn createIn) {
        // 建单前先校验必填字段，避免把没有日次归属的说明写成孤儿单据。
        validateCreateIn(createIn);
        Map<String, Object> dailyMeta = requireDailyMeta(createIn.getAttendanceDailyId());
        Map<String, Object> latestCase = attendanceCaseDao.selectLatestCaseIdentityByDailyId(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            createIn.getAttendanceDailyId()
        );
        // 如果当前日次已经存在活动处理单，就不允许重复创建，避免审批流并发分叉。
        if (hasActiveCase(latestCase)) {
            throw new IllegalStateException("当前异常已存在处理中单据，请直接进入审批流");
        }
        attendanceCaseDao.insertCase(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            createIn.getAttendanceDailyId(),
            longValue(readMapValue(dailyMeta, "employeeId")),
            longValue(readMapValue(dailyMeta, "workplaceId")),
            longValue(readMapValue(dailyMeta, "departmentId")),
            normalizeText(createIn.getCaseType()),
            "SUBMITTED",
            createIn.getApplicantId(),
            defaultText(createIn.getApplicantRole(), "MANAGER"),
            9001L,
            normalizeText(createIn.getReasonCategory()),
            normalizeText(createIn.getReasonText()),
            normalizeText(createIn.getExpectedResolution()),
            toJsonString(Map.of()),
            LocalDateTime.now()
        );
        AttendanceCaseOut.CaseDetailOut caseDetail = attendanceCaseDao.selectLatestCaseDetailByDailyId(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            createIn.getAttendanceDailyId()
        );
        // 建单后立刻把日次状态推进入审核中，列表第一眼就能看出已提交审批。
        attendanceDailyService.markDailyInReview(createIn.getAttendanceDailyId());
        attendanceCaseDao.insertActionLog(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            caseDetail.getCaseId(),
            "SUBMIT",
            createIn.getApplicantId(),
            defaultText(createIn.getApplicantRole(), "MANAGER"),
            normalizeText(createIn.getReasonText()),
            toJsonString(dailyMeta),
            toJsonString(Map.of("caseStatus", "SUBMITTED"))
        );
        return buildMutationOut(caseDetail.getCaseId(), createIn.getAttendanceDailyId(), "SUBMITTED", "IN_REVIEW");
    }

    @Override
    @Transactional
    public AttendanceCaseOut.CaseMutationOut applyAction(Long caseId, AttendanceCaseIn.CaseActionIn actionIn) {
        // 单条审批动作统一走一个入口，保持通过、退回、驳回的状态回写规则一致。
        validateActionIn(actionIn);
        Map<String, Object> caseIdentity = requireCaseIdentity(caseId);
        Long attendanceDailyId = longValue(readMapValue(caseIdentity, "attendanceDailyId"));
        Map<String, Object> dailyMeta = requireDailyMeta(attendanceDailyId);
        String actionType = normalizeActionType(actionIn.getActionType());
        String beforeSnapshot = toJsonString(new LinkedHashMap<>(caseIdentity));
        if (Objects.equals("APPROVE", actionType)) {
            applyApprove(caseId, actionIn, dailyMeta);
        } else if (Objects.equals("RETURN", actionType)) {
            applyReturn(caseId, actionIn, attendanceDailyId);
        } else if (Objects.equals("REJECT", actionType)) {
            applyReject(caseId, actionIn, attendanceDailyId);
        } else {
            throw new IllegalArgumentException("不支持的动作类型：" + actionType);
        }
        Map<String, Object> latestCaseIdentity = requireCaseIdentity(caseId);
        attendanceCaseDao.insertActionLog(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            caseId,
            actionType,
            actionIn.getApproverId(),
            "APPROVER",
            normalizeText(actionIn.getComment()),
            beforeSnapshot,
            toJsonString(new LinkedHashMap<>(latestCaseIdentity))
        );
        return buildMutationOut(
            caseId,
            attendanceDailyId,
            stringValue(readMapValue(latestCaseIdentity, "caseStatus")),
            stringValue(readMapValue(requireDailyMeta(attendanceDailyId), "handlingStatus"))
        );
    }

    @Override
    @Transactional
    public AttendanceCaseOut.CaseMutationOut batchApplyAction(AttendanceCaseIn.CaseBatchActionIn actionIn) {
        // 批量动作按单条逻辑循环复用，先保证语义一致，再追求性能。
        validateBatchActionIn(actionIn);
        AttendanceCaseOut.CaseMutationOut lastResult = null;
        for (Long caseId : actionIn.getCaseIds()) {
            AttendanceCaseIn.CaseActionIn singleAction = new AttendanceCaseIn.CaseActionIn();
            singleAction.setActionType(actionIn.getActionType());
            singleAction.setApproverId(actionIn.getApproverId());
            singleAction.setComment(actionIn.getComment());
            singleAction.setPatchPayload(actionIn.getPatchPayload());
            lastResult = applyAction(caseId, singleAction);
        }
        return lastResult;
    }

    // 统一补齐默认日期和分页，保持异常列表与统计卡口径一致。
    private AttendanceCaseIn.CaseQueryIn normalizeQuery(AttendanceCaseIn.CaseQueryIn queryIn) {
        AttendanceCaseIn.CaseQueryIn normalized = queryIn == null ? new AttendanceCaseIn.CaseQueryIn() : queryIn;
        normalized.setStartDate(defaultText(normalized.getStartDate(), "2026-05-01"));
        normalized.setEndDate(defaultText(normalized.getEndDate(), "2026-05-31"));
        normalized.setEmployeeKeyword(normalizeText(normalized.getEmployeeKeyword()));
        normalized.setCaseStatus(normalizeText(normalized.getCaseStatus()));
        normalized.setHandlingStatus(normalizeText(normalized.getHandlingStatus()));
        normalized.setMineOnly(Boolean.TRUE.equals(normalized.getMineOnly()));
        normalized.setPage(normalized.getPage() == null || normalized.getPage() <= 0 ? DEFAULT_PAGE : normalized.getPage());
        normalized.setPageSize(normalizePageSize(normalized.getPageSize()));
        return normalized;
    }

    // 固定页大小档位，避免任意数字扰动数据库分页。
    private Integer normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return ALLOWED_PAGE_SIZES.contains(pageSize) ? pageSize : DEFAULT_PAGE_SIZE;
    }

    // 预先换算总页数，减少前端重复处理边界。
    private Integer calculateTotalPages(int total, int pageSize) {
        if (total <= 0) {
            return 1;
        }
        return (total + pageSize - 1) / pageSize;
    }

    // 构建第五阶段顶部统计卡，让用户先看到异常卡在待处理还是审批中。
    private AttendanceCaseOut.CaseSummaryOut buildSummary(List<Map<String, Object>> rows) {
        AttendanceCaseOut.CaseSummaryOut summaryOut = new AttendanceCaseOut.CaseSummaryOut();
        summaryOut.setPendingCount(0);
        summaryOut.setReviewingCount(0);
        summaryOut.setApprovedCount(0);
        summaryOut.setRejectedCount(0);
        summaryOut.setLockedCount(0);
        for (Map<String, Object> row : rows) {
            String bucket = stringValue(readMapValue(row, "bucket"));
            int total = intValue(readMapValue(row, "totalCount"));
            if (Objects.equals("UNHANDLED", bucket)) {
                summaryOut.setPendingCount(summaryOut.getPendingCount() + total);
            } else if (Objects.equals("SUBMITTED", bucket) || Objects.equals("RETURNED", bucket) || Objects.equals("IN_REVIEW", bucket)) {
                summaryOut.setReviewingCount(summaryOut.getReviewingCount() + total);
            } else if (Objects.equals("APPROVED", bucket)) {
                summaryOut.setApprovedCount(summaryOut.getApprovedCount() + total);
            } else if (Objects.equals("REJECTED", bucket)) {
                summaryOut.setRejectedCount(summaryOut.getRejectedCount() + total);
            } else if (Objects.equals("LOCKED", bucket)) {
                summaryOut.setLockedCount(summaryOut.getLockedCount() + total);
            }
        }
        return summaryOut;
    }

    // 读取真实处理单详情，不允许对伪行直接走详情接口。
    private AttendanceCaseOut.CaseDetailOut requireCaseDetail(Long caseId) {
        if (caseId == null || caseId <= 0) {
            throw new IllegalArgumentException("caseId 不能为空");
        }
        AttendanceCaseOut.CaseDetailOut detailOut = attendanceCaseDao.selectCaseDetail(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            caseId
        );
        if (detailOut == null) {
            throw new IllegalArgumentException("未找到对应处理单");
        }
        return detailOut;
    }

    // 建单时要求至少有日次主键、处理单类型和申请人。
    private void validateCreateIn(AttendanceCaseIn.CaseCreateIn createIn) {
        if (
            createIn == null
                || createIn.getAttendanceDailyId() == null
                || !StringUtils.hasText(createIn.getCaseType())
                || createIn.getApplicantId() == null
        ) {
            throw new IllegalArgumentException("attendanceDailyId、caseType、applicantId 不能为空");
        }
    }

    // 审批动作必须明确动作类型和审批人，否则无法追溯是谁改了什么。
    private void validateActionIn(AttendanceCaseIn.CaseActionIn actionIn) {
        if (actionIn == null || !StringUtils.hasText(actionIn.getActionType()) || actionIn.getApproverId() == null) {
            throw new IllegalArgumentException("actionType 和 approverId 不能为空");
        }
    }

    // 批量动作至少要有处理单集合和动作类型。
    private void validateBatchActionIn(AttendanceCaseIn.CaseBatchActionIn actionIn) {
        if (
            actionIn == null
                || actionIn.getCaseIds() == null
                || actionIn.getCaseIds().isEmpty()
                || !StringUtils.hasText(actionIn.getActionType())
                || actionIn.getApproverId() == null
        ) {
            throw new IllegalArgumentException("caseIds、actionType 和 approverId 不能为空");
        }
    }

    // 只要最新处理单还在处理中或已完成锁定，就不允许重复建单。
    private boolean hasActiveCase(Map<String, Object> latestCase) {
        if (latestCase == null || latestCase.isEmpty()) {
            return false;
        }
        String caseStatus = stringValue(readMapValue(latestCase, "caseStatus"));
        return !Objects.equals("REJECTED", caseStatus) && !Objects.equals("CANCELLED", caseStatus);
    }

    // 审批通过会把最终业务结论回写到日次表，供后续月结读取稳定结果。
    private void applyApprove(Long caseId, AttendanceCaseIn.CaseActionIn actionIn, Map<String, Object> dailyMeta) {
        Long attendanceDailyId = longValue(readMapValue(dailyMeta, "id"));
        AttendanceDailyOut.DailyDetailOut dailyDetail = attendanceDailyService.getDailyDetail(attendanceDailyId);
        Map<String, Object> patchPayload = actionIn.getPatchPayload() == null ? Map.of() : actionIn.getPatchPayload();
        LocalDateTime finalClockIn = parseOptionalDateTime(readMapValue(patchPayload, "actualClockIn"), dailyDetail.getActualClockIn());
        LocalDateTime finalClockOut = parseOptionalDateTime(readMapValue(patchPayload, "actualClockOut"), dailyDetail.getActualClockOut());
        Integer finalBreakMinutes = parseOptionalInt(readMapValue(patchPayload, "finalBreakMinutes"), dailyDetail.getActualBreakMinutes());
        String finalStatus = defaultText(stringValue(readMapValue(patchPayload, "finalStatus")), defaultText(dailyDetail.getStatus(), "NORMAL"));
        boolean finalExceptionFlag = parseOptionalBoolean(readMapValue(patchPayload, "finalExceptionFlag"), false);
        attendanceCaseDao.updateCaseStatus(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            caseId,
            "APPROVED",
            actionIn.getApproverId(),
            toJsonString(patchPayload),
            LocalDateTime.now(),
            null,
            null
        );
        attendanceDailyService.applyApprovedResolution(
            attendanceDailyId,
            caseId,
            finalClockIn,
            finalClockOut,
            finalBreakMinutes,
            finalStatus,
            normalizeText(actionIn.getComment()),
            finalExceptionFlag
        );
    }

    // 退回补充会保留处理单，但继续停留在审核中，等待申请方补充说明。
    private void applyReturn(Long caseId, AttendanceCaseIn.CaseActionIn actionIn, Long attendanceDailyId) {
        attendanceCaseDao.updateCaseStatus(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            caseId,
            "RETURNED",
            actionIn.getApproverId(),
            toJsonString(actionIn.getPatchPayload() == null ? Map.of() : actionIn.getPatchPayload()),
            null,
            null,
            null
        );
        attendanceDailyService.markDailyReturned(attendanceDailyId);
    }

    // 驳回后把日次重新放回待处理，保留异常继续等待重新申请。
    private void applyReject(Long caseId, AttendanceCaseIn.CaseActionIn actionIn, Long attendanceDailyId) {
        attendanceCaseDao.updateCaseStatus(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            caseId,
            "REJECTED",
            actionIn.getApproverId(),
            toJsonString(actionIn.getPatchPayload() == null ? Map.of() : actionIn.getPatchPayload()),
            null,
            LocalDateTime.now(),
            null
        );
        attendanceDailyService.markDailyRejected(attendanceDailyId);
    }

    // 第五阶段详情页根据当前状态显示下一步可点动作，避免用户自己猜。
    private List<String> buildAvailableActions(AttendanceCaseOut.CaseDetailOut detailOut) {
        List<String> actions = new ArrayList<>();
        if (Objects.equals("SUBMITTED", detailOut.getCaseStatus()) || Objects.equals("RETURNED", detailOut.getCaseStatus())) {
            actions.add("APPROVE");
            actions.add("RETURN");
            actions.add("REJECT");
        }
        return actions;
    }

    // 统一构建动作结果，供前端刷新列表和详情状态。
    private AttendanceCaseOut.CaseMutationOut buildMutationOut(Long caseId, Long attendanceDailyId, String caseStatus, String handlingStatus) {
        AttendanceCaseOut.CaseMutationOut resultOut = new AttendanceCaseOut.CaseMutationOut();
        resultOut.setCaseId(caseId);
        resultOut.setAttendanceDailyId(attendanceDailyId);
        resultOut.setCaseStatus(caseStatus);
        resultOut.setHandlingStatus(handlingStatus);
        return resultOut;
    }

    // 第五阶段所有动作都建立在真实日次记录存在的前提上。
    private Map<String, Object> requireDailyMeta(Long dailyId) {
        if (dailyId == null || dailyId <= 0) {
            throw new IllegalArgumentException("attendanceDailyId 不能为空");
        }
        Map<String, Object> dailyMeta = attendanceDailyService.getDailyMeta(dailyId);
        if (dailyMeta == null || dailyMeta.isEmpty()) {
            throw new IllegalArgumentException("未找到对应日次结果");
        }
        return dailyMeta;
    }

    // 动作前先确认处理单存在，避免前端用旧数据提交已失效案件。
    private Map<String, Object> requireCaseIdentity(Long caseId) {
        if (caseId == null || caseId <= 0) {
            throw new IllegalArgumentException("caseId 不能为空");
        }
        Map<String, Object> caseIdentity = attendanceCaseDao.selectCaseIdentityById(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            caseId
        );
        if (caseIdentity == null || caseIdentity.isEmpty()) {
            throw new IllegalArgumentException("未找到对应处理单");
        }
        return caseIdentity;
    }

    // 兼容补丁里可能不传时间的场景，默认沿用当前日次实际结果。
    private LocalDateTime parseOptionalDateTime(Object value, LocalDateTime fallbackValue) {
        if (value == null || !StringUtils.hasText(stringValue(value))) {
            return fallbackValue;
        }
        try {
            return LocalDateTime.parse(stringValue(value));
        } catch (DateTimeParseException error) {
            throw new IllegalArgumentException("时间格式不正确：" + value);
        }
    }

    // 兼容补丁里可选的最终休息分钟。
    private Integer parseOptionalInt(Object value, Integer fallbackValue) {
        if (value == null || !StringUtils.hasText(stringValue(value))) {
            return fallbackValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(stringValue(value));
    }

    // 兼容补丁里可选的最终异常标记。
    private boolean parseOptionalBoolean(Object value, boolean fallbackValue) {
        if (value == null || !StringUtils.hasText(stringValue(value))) {
            return fallbackValue;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        String text = stringValue(value).toLowerCase(Locale.ROOT);
        return Objects.equals("true", text) || Objects.equals("1", text);
    }

    // 对查询字符串统一 trim，减少空白字符干扰过滤。
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    // 文本为空时回填默认值，避免状态和角色出现空串。
    private String defaultText(String value, String defaultValue) {
        String normalized = normalizeText(value);
        return StringUtils.hasText(normalized) ? normalized : defaultValue;
    }

    // 动作类型统一转成大写，保持接口大小写容错。
    private String normalizeActionType(String value) {
        return defaultText(value, "").toUpperCase(Locale.ROOT);
    }

    // 兼容数据库 Map 结果键名大小写差异。
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

    // 统一读取数值主键，供 DAO Map 结果回读。
    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    // 统一读取整数字段，供统计卡累加。
    private int intValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    // 统一把对象安全转换成字符串。
    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    // 审批前后快照统一序列化成 JSON 落日志。
    private String toJsonString(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException error) {
            throw new IllegalArgumentException("审批快照序列化失败", error);
        }
    }
}
