package com.sp.selfsp.attendance.punch.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.employee.dao.AttendanceEmployeeDao;
import com.sp.selfsp.attendance.punch.dao.AttendancePunchDao;
import com.sp.selfsp.attendance.punch.domain.in.AttendancePunchIn;
import com.sp.selfsp.attendance.punch.domain.out.AttendancePunchOut;
import com.sp.selfsp.attendance.punch.service.AttendancePunchService;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 第三阶段打卡服务实现。
 *
 * <p>负责承接手工补录、CSV 导入、Webhook 接收、员工绑定和重处理这几条原始打卡接入链路。</p>
 */
@Service
public class AttendancePunchServiceImpl implements AttendancePunchService {

    // 默认页码用于页面第一次打开时先看最近一批打卡记录。
    private static final int DEFAULT_PAGE = 1;
    // 默认页大小用于控制第三阶段列表返回量，避免一次拉太多记录。
    private static final int DEFAULT_PAGE_SIZE = 20;
    // 可选页大小限制为产品要求的 20、50、100、200，避免任意数字冲击数据库分页稳定性。
    private static final List<Integer> ALLOWED_PAGE_SIZES = List.of(20, 50, 100, 200);
    // CSV 预览行数用于在正式导入前先给用户看前 10 行处理结果。
    private static final int CSV_PREVIEW_LIMIT = 10;

    // 读取和写入第三阶段原始打卡记录与导入批次。
    private final AttendancePunchDao attendancePunchDao;
    // 读取系统员工和外部映射关系，供自动匹配员工复用。
    private final AttendanceEmployeeDao attendanceEmployeeDao;
    // JSON 序列化器用于把 CSV 行、Webhook 和手动补录统一保存为原始 payload。
    private final ObjectMapper objectMapper;

    // 注入第三阶段所需 DAO 和 JSON 工具，统一处理打卡原始数据接入。
    public AttendancePunchServiceImpl(
        AttendancePunchDao attendancePunchDao,
        AttendanceEmployeeDao attendanceEmployeeDao,
        ObjectMapper objectMapper
    ) {
        this.attendancePunchDao = attendancePunchDao;
        this.attendanceEmployeeDao = attendanceEmployeeDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public AttendancePunchOut.PunchLogListOut listPunchLogs(AttendancePunchIn.PunchLogQueryIn queryIn) {
        // 统一补足默认分页，避免前端首次打开第三阶段页面时传空造成全表返回。
        AttendancePunchIn.PunchLogQueryIn normalizedQuery = normalizeQuery(queryIn);
        int offset = (normalizedQuery.getPage() - 1) * normalizedQuery.getPageSize();
        // 先读取当前页列表，供页面中心表格渲染。
        List<AttendancePunchOut.PunchLogItemOut> items = attendancePunchDao.selectLogs(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            normalizedQuery,
            offset,
            normalizedQuery.getPageSize()
        );
        // 统计当前过滤条件下总记录数，供页面显示规模信息。
        Integer total = attendancePunchDao.countLogs(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery);
        // 聚合各状态数量，供页面顶部先看处理结构。
        AttendancePunchOut.PunchSummaryOut summaryOut = buildSummary(
            attendancePunchDao.countSummaryByStatus(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery)
        );
        AttendancePunchOut.PunchLogListOut listOut = new AttendancePunchOut.PunchLogListOut();
        listOut.setItems(items);
        listOut.setTotal(total == null ? 0 : total);
        // 把当前页码回给前端，保证分页栏展示与本次数据库查询口径一致。
        listOut.setPage(normalizedQuery.getPage());
        // 把当前页大小回给前端，保证每页条数选择和数据库分页真实值一致。
        listOut.setPageSize(normalizedQuery.getPageSize());
        // 预先计算总页数，避免前端自己重复换算并出现 0 页或除零边界。
        listOut.setTotalPages(calculateTotalPages(total == null ? 0 : total, normalizedQuery.getPageSize()));
        listOut.setSummary(summaryOut);
        return listOut;
    }

    @Override
    public AttendancePunchOut.PunchLogDetailOut getPunchLogDetail(Long id) {
        // 详情读取前先确保主键合法，避免页面打开不存在记录时静默失败。
        AttendancePunchOut.PunchLogDetailOut detailOut = requireLog(id);
        // 根据当前状态生成用户下一步可操作动作，保持极度友好版“先看结论再看处理”。
        detailOut.setProcessLogs(buildProcessLogs(detailOut));
        detailOut.setAvailableActions(buildAvailableActions(detailOut));
        return detailOut;
    }

    @Override
    @Transactional
    public AttendancePunchOut.PunchManualResultOut createManualPunch(AttendancePunchIn.PunchManualSaveIn saveIn) {
        // 校验手动补录的必填字段，避免把不完整记录写成脏数据。
        validateManualSaveIn(saveIn);
        // 手动补录必须指向系统内已有员工，否则无法进入后续日次计算。
        AttendanceOut.EmployeeOut employeeOut = requireEmployee(saveIn.getEmployeeId());
        // 手动补录已明确员工归属，因此可以直接记为已处理。
        attendancePunchDao.insertLog(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeOut.getId(),
            employeeOut.getExternalEmployeeId(),
            normalizeSourceSystem(saveIn.getSourceSystem(), "MANUAL"),
            null,
            parsePunchTime(saveIn.getPunchTime()),
            normalizePunchType(saveIn.getPunchType()),
            null,
            defaultText(saveIn.getDeviceName(), "管理员手动补录"),
            toJsonString(Map.of(
                "employeeId", employeeOut.getId(),
                "employeeNo", employeeOut.getEmployeeNo(),
                "employeeName", employeeOut.getEmployeeName(),
                "note", defaultText(saveIn.getNote(), "")
            )),
            null,
            "PROCESSED",
            null,
            null
        );
        AttendancePunchOut.PunchLogDetailOut latestLog = attendancePunchDao.selectLatestLog(AttendanceTenantContext.DEFAULT_TENANT_ID);
        AttendancePunchOut.PunchManualResultOut resultOut = new AttendancePunchOut.PunchManualResultOut();
        resultOut.setId(latestLog == null ? null : latestLog.getId());
        resultOut.setProcessStatus("PROCESSED");
        resultOut.setMessageCode("punch.manual.created");
        return resultOut;
    }

    @Override
    public AttendancePunchOut.PunchImportPreviewOut previewImport(AttendancePunchIn.PunchImportIn saveIn) {
        // 预览和正式导入共用同一套 CSV 解析逻辑，先产出结构化行结果。
        List<CsvRowRuntime> rows = parseCsvRows(saveIn);
        List<AttendancePunchOut.PunchPreviewRowOut> previewRows = new ArrayList<>();
        int readyCount = 0;
        int unmatchedCount = 0;
        int errorCount = 0;
        for (CsvRowRuntime row : rows) {
            if (previewRows.size() < CSV_PREVIEW_LIMIT) {
                previewRows.add(toPreviewRow(row));
            }
            if (Objects.equals("READY", row.status)) {
                readyCount += 1;
            } else if (Objects.equals("UNMATCHED", row.status)) {
                unmatchedCount += 1;
            } else {
                errorCount += 1;
            }
        }
        AttendancePunchOut.PunchImportPreviewSummaryOut summaryOut = new AttendancePunchOut.PunchImportPreviewSummaryOut();
        summaryOut.setTotalCount(rows.size());
        summaryOut.setReadyCount(readyCount);
        summaryOut.setUnmatchedCount(unmatchedCount);
        summaryOut.setErrorCount(errorCount);
        AttendancePunchOut.PunchImportPreviewOut previewOut = new AttendancePunchOut.PunchImportPreviewOut();
        previewOut.setPreviewRows(previewRows);
        previewOut.setSummary(summaryOut);
        return previewOut;
    }

    @Override
    @Transactional
    public AttendancePunchOut.PunchImportResultOut importCsv(AttendancePunchIn.PunchImportIn saveIn) {
        // 先把批次建出来，让正式导入的每条原始记录都能挂在统一批次上。
        attendancePunchDao.insertImportBatch(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            "CSV_IMPORT",
            "CSV_TEXT",
            normalizeFileName(saveIn == null ? null : saveIn.getFileName()),
            "PROCESSING"
        );
        Map<String, Object> latestBatch = attendancePunchDao.selectLatestImportBatch(AttendanceTenantContext.DEFAULT_TENANT_ID);
        Long batchId = latestBatch == null ? null : longValue(latestBatch.get("id"));
        List<CsvRowRuntime> rows = parseCsvRows(saveIn);
        int successCount = 0;
        int duplicateCount = 0;
        int unmatchedCount = 0;
        int errorCount = 0;
        for (CsvRowRuntime row : rows) {
            PersistDecision decision = persistCsvRow(batchId, row);
            if (Objects.equals("PROCESSED", decision.processStatus)) {
                successCount += 1;
            } else if (Objects.equals("DUPLICATE", decision.processStatus)) {
                duplicateCount += 1;
            } else if (Objects.equals("UNMATCHED", decision.processStatus)) {
                unmatchedCount += 1;
            } else {
                errorCount += 1;
            }
        }
        String batchStatus = errorCount > 0 || unmatchedCount > 0 || duplicateCount > 0 ? "PARTIAL" : "COMPLETED";
        attendancePunchDao.updateImportBatchResult(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            batchId,
            rows.size(),
            successCount,
            duplicateCount,
            unmatchedCount,
            errorCount,
            0,
            batchStatus
        );
        AttendancePunchOut.PunchImportResultOut resultOut = new AttendancePunchOut.PunchImportResultOut();
        resultOut.setBatchId(batchId);
        resultOut.setFileName(normalizeFileName(saveIn == null ? null : saveIn.getFileName()));
        resultOut.setTotalCount(rows.size());
        resultOut.setSuccessCount(successCount);
        resultOut.setDuplicateCount(duplicateCount);
        resultOut.setUnmatchedCount(unmatchedCount);
        resultOut.setErrorCount(errorCount);
        resultOut.setIgnoredCount(0);
        resultOut.setStatus(batchStatus);
        return resultOut;
    }

    @Override
    @Transactional
    public AttendancePunchOut.PunchManualResultOut receiveWebhook(AttendancePunchIn.PunchWebhookIn saveIn) {
        // Webhook 第一版也复用统一原始记录落库和自动匹配逻辑，避免每个来源各自分叉。
        validateWebhookSaveIn(saveIn);
        PersistDecision decision = evaluatePersistDecision(
            normalizeSourceSystem(saveIn.getSourceSystem(), "CUSTOM"),
            normalizeText(saveIn.getSourceEventId()),
            normalizeText(saveIn.getExternalEmployeeId()),
            parsePunchTime(saveIn.getPunchTime()),
            normalizePunchType(saveIn.getPunchType()),
            normalizeText(saveIn.getDeviceId()),
            normalizeText(saveIn.getDeviceName()),
            toJsonString(Map.of(
                "tenantCode", defaultText(saveIn.getTenantCode(), ""),
                "rawData", saveIn.getRawData() == null ? Collections.emptyMap() : saveIn.getRawData()
            )),
            null
        );
        attendancePunchDao.insertLog(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            decision.employeeId,
            decision.externalEmployeeId,
            decision.sourceSystem,
            decision.sourceEventId,
            decision.punchTime,
            decision.punchType,
            decision.deviceId,
            decision.deviceName,
            decision.rawPayload,
            null,
            decision.processStatus,
            decision.errorMessage,
            null
        );
        AttendancePunchOut.PunchLogDetailOut latestLog = attendancePunchDao.selectLatestLog(AttendanceTenantContext.DEFAULT_TENANT_ID);
        AttendancePunchOut.PunchManualResultOut resultOut = new AttendancePunchOut.PunchManualResultOut();
        resultOut.setId(latestLog == null ? null : latestLog.getId());
        resultOut.setProcessStatus(decision.processStatus);
        resultOut.setMessageCode("punch.webhook.received");
        return resultOut;
    }

    @Override
    @Transactional
    public AttendancePunchOut.PunchLogDetailOut bindEmployee(Long id, AttendancePunchIn.PunchBindEmployeeIn saveIn) {
        // 绑定前先确认当前记录存在，避免对不存在的打卡主键操作。
        AttendancePunchOut.PunchLogDetailOut detailOut = requireLog(id);
        // 再确认用户选择的目标员工存在，确保绑定动作指向有效员工。
        AttendanceOut.EmployeeOut employeeOut = requireEmployee(saveIn == null ? null : saveIn.getEmployeeId());
        // 把外部编号同步写回员工映射不是本阶段必要动作，这里只修复当前记录归属。
        attendancePunchDao.updateEmployeeAndStatus(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            detailOut.getId(),
            employeeOut.getId(),
            "PROCESSED",
            null
        );
        return getPunchLogDetail(id);
    }

    @Override
    @Transactional
    public AttendancePunchOut.PunchLogDetailOut ignoreLog(Long id, AttendancePunchIn.PunchIgnoreIn saveIn) {
        // 忽略前先确保目标记录存在，避免误操作静默成功。
        requireLog(id);
        attendancePunchDao.updateIgnored(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            id,
            "IGNORED",
            defaultText(saveIn == null ? null : saveIn.getReason(), "管理员手动忽略")
        );
        return getPunchLogDetail(id);
    }

    @Override
    @Transactional
    public AttendancePunchOut.PunchLogDetailOut reprocessLog(Long id) {
        // 重处理必须基于既有记录重新跑匹配逻辑，而不是盲目改成成功。
        AttendancePunchOut.PunchLogDetailOut detailOut = requireLog(id);
        PersistDecision decision = evaluatePersistDecision(
            detailOut.getSourceSystem(),
            detailOut.getSourceEventId(),
            detailOut.getExternalEmployeeId(),
            detailOut.getPunchTime(),
            detailOut.getPunchType(),
            null,
            detailOut.getDeviceName(),
            detailOut.getRawPayload(),
            detailOut.getId()
        );
        if (decision.employeeId != null) {
            attendancePunchDao.updateEmployeeAndStatus(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                id,
                decision.employeeId,
                decision.processStatus,
                decision.errorMessage
            );
        } else {
            attendancePunchDao.updateStatus(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                id,
                decision.processStatus,
                decision.errorMessage
            );
        }
        return getPunchLogDetail(id);
    }

    // 统一补齐分页默认值和空白筛选清理，避免 DAO 层处理空值分支过多。
    private AttendancePunchIn.PunchLogQueryIn normalizeQuery(AttendancePunchIn.PunchLogQueryIn queryIn) {
        AttendancePunchIn.PunchLogQueryIn normalized = queryIn == null ? new AttendancePunchIn.PunchLogQueryIn() : queryIn;
        normalized.setDateFrom(normalizeText(normalized.getDateFrom()));
        normalized.setDateTo(normalizeText(normalized.getDateTo()));
        normalized.setEmployeeKeyword(normalizeText(normalized.getEmployeeKeyword()));
        normalized.setSourceSystem(normalizeText(normalized.getSourceSystem()));
        normalized.setProcessStatus(normalizeText(normalized.getProcessStatus()));
        normalized.setPunchType(normalizeText(normalized.getPunchType()));
        normalized.setPage(normalized.getPage() == null || normalized.getPage() <= 0 ? DEFAULT_PAGE : normalized.getPage());
        // 页大小只允许进入产品定义的固定档位，避免异常输入绕过数据库分页边界。
        normalized.setPageSize(normalizePageSize(normalized.getPageSize()));
        return normalized;
    }

    // 把前端页大小输入收敛到允许档位，未命中时回退到默认 20 条。
    private Integer normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return ALLOWED_PAGE_SIZES.contains(pageSize) ? pageSize : DEFAULT_PAGE_SIZE;
    }

    // 统一换算总页数，保证前端分页栏拿到的是至少 1 页的稳定结果。
    private Integer calculateTotalPages(int total, int pageSize) {
        if (total <= 0) {
            return 1;
        }
        return (total + pageSize - 1) / pageSize;
    }

    // 用状态统计结果构建摘要对象，供页面顶部先显示待处理结构。
    private AttendancePunchOut.PunchSummaryOut buildSummary(List<Map<String, Object>> rows) {
        AttendancePunchOut.PunchSummaryOut summaryOut = new AttendancePunchOut.PunchSummaryOut();
        summaryOut.setProcessed(0);
        summaryOut.setUnmatched(0);
        summaryOut.setError(0);
        summaryOut.setDuplicate(0);
        summaryOut.setIgnored(0);
        for (Map<String, Object> row : rows) {
            String status = stringValue(readMapValue(row, "processStatus"));
            int total = intValue(readMapValue(row, "totalCount"));
            if (Objects.equals("PROCESSED", status)) {
                summaryOut.setProcessed(total);
            } else if (Objects.equals("UNMATCHED", status)) {
                summaryOut.setUnmatched(total);
            } else if (Objects.equals("ERROR", status)) {
                summaryOut.setError(total);
            } else if (Objects.equals("DUPLICATE", status)) {
                summaryOut.setDuplicate(total);
            } else if (Objects.equals("IGNORED", status)) {
                summaryOut.setIgnored(total);
            }
        }
        return summaryOut;
    }

    // 校验手动补录必填字段，避免把空员工、空时间或空类型写入原始表。
    private void validateManualSaveIn(AttendancePunchIn.PunchManualSaveIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("manual saveIn 不能为空");
        }
        if (saveIn.getEmployeeId() == null || saveIn.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("employeeId 不能为空");
        }
        parsePunchTime(saveIn.getPunchTime());
        normalizePunchType(saveIn.getPunchType());
    }

    // 校验 Webhook 必填字段，避免把缺少事件主信息的请求写入原始表。
    private void validateWebhookSaveIn(AttendancePunchIn.PunchWebhookIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("webhook saveIn 不能为空");
        }
        if (!StringUtils.hasText(saveIn.getExternalEmployeeId())) {
            throw new IllegalArgumentException("externalEmployeeId 不能为空");
        }
        parsePunchTime(saveIn.getPunchTime());
        normalizePunchType(saveIn.getPunchType());
    }

    // 把 CSV 文本解析成结构化运行时对象，供预览和正式导入共用。
    private List<CsvRowRuntime> parseCsvRows(AttendancePunchIn.PunchImportIn saveIn) {
        if (saveIn == null || !StringUtils.hasText(saveIn.getCsvText())) {
            throw new IllegalArgumentException("csvText 不能为空");
        }
        List<String> lines = saveIn.getCsvText().lines().map(String::trim).filter(StringUtils::hasText).toList();
        if (lines.size() <= 1) {
            throw new IllegalArgumentException("CSV 至少需要包含表头和一行数据");
        }
        String[] headers = splitCsvLine(lines.get(0));
        Map<String, Integer> headerIndexMap = buildHeaderIndex(headers);
        List<CsvRowRuntime> rows = new ArrayList<>();
        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            String[] cells = splitCsvLine(lines.get(lineIndex));
            CsvRowRuntime row = new CsvRowRuntime();
            row.rowNumber = lineIndex;
            row.externalEmployeeId = readCell(cells, headerIndexMap.get("externalEmployeeId"));
            row.sourceSystem = normalizeSourceSystem(readCell(cells, headerIndexMap.get("sourceSystem")), "CSV_IMPORT");
            row.sourceEventId = normalizeText(readCell(cells, headerIndexMap.get("sourceEventId")));
            row.deviceId = normalizeText(readCell(cells, headerIndexMap.get("deviceId")));
            row.deviceName = normalizeText(readCell(cells, headerIndexMap.get("deviceName")));
            row.punchType = normalizePunchTypeOrNull(readCell(cells, headerIndexMap.get("punchType")));
            row.rawPunchTime = readCell(cells, headerIndexMap.get("punchTime"));
            row.rawPayload = toJsonString(new LinkedHashMap<>(Map.of(
                "externalEmployeeId", defaultText(row.externalEmployeeId, ""),
                "sourceSystem", defaultText(row.sourceSystem, ""),
                "sourceEventId", defaultText(row.sourceEventId, ""),
                "deviceId", defaultText(row.deviceId, ""),
                "deviceName", defaultText(row.deviceName, ""),
                "punchTime", defaultText(row.rawPunchTime, ""),
                "punchType", defaultText(row.punchType, "")
            )));
            if (!StringUtils.hasText(row.externalEmployeeId)) {
                row.status = "ERROR";
                row.message = "externalEmployeeId 不能为空";
            } else if (!StringUtils.hasText(row.rawPunchTime)) {
                row.status = "ERROR";
                row.message = "punchTime 不能为空";
            } else if (!StringUtils.hasText(row.punchType)) {
                row.status = "ERROR";
                row.message = "punchType 不合法";
            } else {
                try {
                    row.punchTime = parsePunchTime(row.rawPunchTime);
                    AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectByExternalEmployeeId(
                        AttendanceTenantContext.DEFAULT_TENANT_ID,
                        row.sourceSystem,
                        row.externalEmployeeId
                    );
                    row.employeeName = employeeOut == null ? "" : employeeOut.getEmployeeName();
                    row.employeeId = employeeOut == null ? null : employeeOut.getId();
                    row.status = employeeOut == null ? "UNMATCHED" : "READY";
                    row.message = employeeOut == null ? "未找到对应员工映射" : "";
                } catch (IllegalArgumentException error) {
                    row.status = "ERROR";
                    row.message = error.getMessage();
                }
            }
            rows.add(row);
        }
        return rows;
    }

    // 把单条预览运行时对象转换成前端预览行结构。
    private AttendancePunchOut.PunchPreviewRowOut toPreviewRow(CsvRowRuntime row) {
        AttendancePunchOut.PunchPreviewRowOut previewRowOut = new AttendancePunchOut.PunchPreviewRowOut();
        previewRowOut.setRowNumber(row.rowNumber);
        previewRowOut.setExternalEmployeeId(row.externalEmployeeId);
        previewRowOut.setEmployeeName(defaultText(row.employeeName, ""));
        previewRowOut.setPunchTime(defaultText(row.rawPunchTime, ""));
        previewRowOut.setPunchType(defaultText(row.punchType, ""));
        previewRowOut.setStatus(row.status);
        previewRowOut.setMessage(defaultText(row.message, ""));
        return previewRowOut;
    }

    // 正式持久化一条 CSV 行，并返回本次持久化后的状态分类。
    private PersistDecision persistCsvRow(Long batchId, CsvRowRuntime row) {
        if (Objects.equals("ERROR", row.status)) {
            attendancePunchDao.insertLog(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                null,
                row.externalEmployeeId,
                row.sourceSystem,
                row.sourceEventId,
                fallbackPunchTime(row.rawPunchTime),
                defaultText(row.punchType, "CLOCK_IN"),
                row.deviceId,
                row.deviceName,
                row.rawPayload,
                batchId,
                "ERROR",
                row.message,
                null
            );
            return new PersistDecision(null, row.externalEmployeeId, row.sourceSystem, row.sourceEventId, fallbackPunchTime(row.rawPunchTime), defaultText(row.punchType, "CLOCK_IN"), row.deviceId, row.deviceName, row.rawPayload, "ERROR", row.message);
        }
        PersistDecision decision = evaluatePersistDecision(
            row.sourceSystem,
            row.sourceEventId,
            row.externalEmployeeId,
            row.punchTime,
            row.punchType,
            row.deviceId,
            row.deviceName,
            row.rawPayload,
            null
        );
        attendancePunchDao.insertLog(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            decision.employeeId,
            decision.externalEmployeeId,
            decision.sourceSystem,
            decision.sourceEventId,
            decision.punchTime,
            decision.punchType,
            decision.deviceId,
            decision.deviceName,
            decision.rawPayload,
            batchId,
            decision.processStatus,
            decision.errorMessage,
            null
        );
        return decision;
    }

    // 对一条记录统一做去重、员工匹配和状态判定，供 CSV、Webhook 和重处理共用。
    private PersistDecision evaluatePersistDecision(
        String sourceSystem,
        String sourceEventId,
        String externalEmployeeId,
        LocalDateTime punchTime,
        String punchType,
        String deviceId,
        String deviceName,
        String rawPayload,
        Long selfId
    ) {
        PersistDecision decision = new PersistDecision(null, externalEmployeeId, sourceSystem, sourceEventId, punchTime, punchType, deviceId, deviceName, rawPayload, "PENDING", null);
        if (StringUtils.hasText(sourceEventId)) {
            AttendancePunchOut.PunchLogDetailOut duplicate = attendancePunchDao.selectBySourceEvent(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                sourceSystem,
                sourceEventId
            );
            if (duplicate != null && !Objects.equals(duplicate.getId(), selfId)) {
                decision.processStatus = "DUPLICATE";
                decision.errorMessage = "sourceEventId 已存在";
                return decision;
            }
        }
        if (!StringUtils.hasText(externalEmployeeId)) {
            decision.processStatus = "ERROR";
            decision.errorMessage = "externalEmployeeId 不能为空";
            return decision;
        }
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectByExternalEmployeeId(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            sourceSystem,
            externalEmployeeId
        );
        if (employeeOut == null) {
            employeeOut = attendanceEmployeeDao.selectByExternalEmployeeId(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                null,
                externalEmployeeId
            );
        }
        if (employeeOut == null) {
            decision.processStatus = "UNMATCHED";
            decision.errorMessage = "未找到对应员工映射";
            return decision;
        }
        decision.employeeId = employeeOut.getId();
        decision.processStatus = "PROCESSED";
        decision.errorMessage = null;
        return decision;
    }

    // 强制读取一条存在的打卡记录，避免详情、忽略和重处理操作落到空对象上。
    private AttendancePunchOut.PunchLogDetailOut requireLog(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id 不能为空");
        }
        AttendancePunchOut.PunchLogDetailOut detailOut = attendancePunchDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        if (detailOut == null) {
            throw new IllegalArgumentException("未找到打卡记录：" + id);
        }
        return detailOut;
    }

    // 强制读取一个存在的系统员工，避免补录和绑定动作指向无效员工。
    private AttendanceOut.EmployeeOut requireEmployee(Long employeeId) {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("employeeId 不能为空");
        }
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeId);
        if (employeeOut == null) {
            throw new IllegalArgumentException("未找到员工：" + employeeId);
        }
        return employeeOut;
    }

    // 根据当前记录状态生成处理日志摘要，让详情先给出结论再给技术细节。
    private List<String> buildProcessLogs(AttendancePunchOut.PunchLogDetailOut detailOut) {
        List<String> processLogs = new ArrayList<>();
        processLogs.add("来源系统：" + defaultText(detailOut.getSourceSystem(), "-"));
        if (StringUtils.hasText(detailOut.getExternalEmployeeId())) {
            processLogs.add("外部员工编号：" + detailOut.getExternalEmployeeId());
        }
        if (detailOut.getEmployeeId() != null) {
            processLogs.add("已匹配员工：" + defaultText(detailOut.getEmployeeName(), "-"));
        }
        if (StringUtils.hasText(detailOut.getErrorMessage())) {
            processLogs.add("处理说明：" + detailOut.getErrorMessage());
        }
        if (StringUtils.hasText(detailOut.getIgnoredReason())) {
            processLogs.add("忽略原因：" + detailOut.getIgnoredReason());
        }
        processLogs.add("当前状态：" + defaultText(detailOut.getProcessStatus(), "-"));
        return processLogs;
    }

    // 根据记录状态计算可执行动作，避免前端自己猜哪些按钮该显示。
    private List<String> buildAvailableActions(AttendancePunchOut.PunchLogDetailOut detailOut) {
        List<String> actions = new ArrayList<>();
        if (Objects.equals("UNMATCHED", detailOut.getProcessStatus())) {
            actions.add("BIND_EMPLOYEE");
            actions.add("IGNORE");
            actions.add("REPROCESS");
        } else if (Objects.equals("ERROR", detailOut.getProcessStatus())) {
            actions.add("REPROCESS");
            actions.add("IGNORE");
        } else if (Objects.equals("DUPLICATE", detailOut.getProcessStatus())) {
            actions.add("IGNORE");
        } else if (Objects.equals("PROCESSED", detailOut.getProcessStatus())) {
            actions.add("IGNORE");
            actions.add("REPROCESS");
        }
        return actions;
    }

    // 兼容 ISO 带时区和普通 yyyy-MM-dd HH:mm:ss 两种输入格式。
    private LocalDateTime parsePunchTime(String text) {
        String normalized = normalizeText(text);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("punchTime 不能为空");
        }
        try {
            if (normalized.contains("T") && (normalized.endsWith("Z") || normalized.contains("+"))) {
                return OffsetDateTime.parse(normalized).toLocalDateTime();
            }
            return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT));
        } catch (DateTimeParseException error) {
            try {
                return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT));
            } catch (DateTimeParseException ignored) {
                throw new IllegalArgumentException("punchTime 格式不正确");
            }
        }
    }

    // 允许 CSV 错误行仍然入库，因此这里提供一个失败时兜底的时间值。
    private LocalDateTime fallbackPunchTime(String text) {
        try {
            return parsePunchTime(text);
        } catch (IllegalArgumentException error) {
            return LocalDateTime.of(2026, 5, 1, 0, 0);
        }
    }

    // 统一归一化打卡类型，确保 CSV、Webhook 和手动补录只进入固定枚举。
    private String normalizePunchType(String punchType) {
        String normalized = normalizeText(punchType);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("punchType 不能为空");
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        if (List.of("CLOCK_IN", "CLOCK_OUT", "BREAK_START", "BREAK_END").contains(upper)) {
            return upper;
        }
        throw new IllegalArgumentException("punchType 不合法");
    }

    // CSV 预览场景不抛异常时使用的安全版打卡类型归一化。
    private String normalizePunchTypeOrNull(String punchType) {
        try {
            return normalizePunchType(punchType);
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

    // 统一归一化来源系统，没有时回退到当前入口定义的默认来源。
    private String normalizeSourceSystem(String sourceSystem, String defaultValue) {
        String normalized = normalizeText(sourceSystem);
        return StringUtils.hasText(normalized) ? normalized.toUpperCase(Locale.ROOT) : defaultValue;
    }

    // 统一处理可空文本字段，避免空白字符串直接落库。
    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // 给需要直接显示的字段提供默认值，避免详情文案出现 null。
    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    // 统一给导入文件名兜底，避免页面未传时批次结果显示空名。
    private String normalizeFileName(String fileName) {
        return defaultText(normalizeText(fileName), "attendance-punch-import.csv");
    }

    // 安全序列化原始 payload，确保所有来源都能把原始信息保存为 UTF-8 JSON 字符串。
    private String toJsonString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException error) {
            throw new IllegalArgumentException("rawPayload 序列化失败");
        }
    }

    // 把 CSV 表头转成列索引映射，便于按字段名而不是固定列号取值。
    private Map<String, Integer> buildHeaderIndex(String[] headers) {
        Map<String, Integer> indexMap = new LinkedHashMap<>();
        for (int index = 0; index < headers.length; index++) {
            indexMap.put(normalizeText(headers[index]), index);
        }
        return indexMap;
    }

    // 读取指定列号的 CSV 单元格，并做空白归一化。
    private String readCell(String[] cells, Integer index) {
        if (index == null || index < 0 || index >= cells.length) {
            return null;
        }
        return normalizeText(cells[index]);
    }

    // 第一版 CSV 仅支持简单逗号分隔，不做复杂引号嵌套解析。
    private String[] splitCsvLine(String line) {
        return line.split(",", -1);
    }

    // 把 Object 安全转成整数，兼容 H2 和 MyBatis 统计结果类型差异。
    private int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    // 把 Object 安全转成 long，兼容导入批次主键读取。
    private long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    // 把 Object 安全转成字符串，兼容 summary 分组字段读取。
    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    // 兼容 H2 和不同驱动返回的 key 大小写差异，避免摘要统计因为别名大小写丢值。
    private Object readMapValue(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String upper = key.toUpperCase(Locale.ROOT);
        if (row.containsKey(upper)) {
            return row.get(upper);
        }
        String lower = key.toLowerCase(Locale.ROOT);
        if (row.containsKey(lower)) {
            return row.get(lower);
        }
        return null;
    }

    // 承载单条 CSV 行在预览和导入过程中的运行时结果。
    private static final class CsvRowRuntime {
        private Integer rowNumber;
        private Long employeeId;
        private String employeeName;
        private String externalEmployeeId;
        private String sourceSystem;
        private String sourceEventId;
        private String rawPunchTime;
        private LocalDateTime punchTime;
        private String punchType;
        private String deviceId;
        private String deviceName;
        private String rawPayload;
        private String status;
        private String message;
    }

    // 承载单条原始记录在正式落库前的统一判定结果。
    private static final class PersistDecision {
        private Long employeeId;
        private String externalEmployeeId;
        private String sourceSystem;
        private String sourceEventId;
        private LocalDateTime punchTime;
        private String punchType;
        private String deviceId;
        private String deviceName;
        private String rawPayload;
        private String processStatus;
        private String errorMessage;

        private PersistDecision(
            Long employeeId,
            String externalEmployeeId,
            String sourceSystem,
            String sourceEventId,
            LocalDateTime punchTime,
            String punchType,
            String deviceId,
            String deviceName,
            String rawPayload,
            String processStatus,
            String errorMessage
        ) {
            this.employeeId = employeeId;
            this.externalEmployeeId = externalEmployeeId;
            this.sourceSystem = sourceSystem;
            this.sourceEventId = sourceEventId;
            this.punchTime = punchTime;
            this.punchType = punchType;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.rawPayload = rawPayload;
            this.processStatus = processStatus;
            this.errorMessage = errorMessage;
        }
    }
}
