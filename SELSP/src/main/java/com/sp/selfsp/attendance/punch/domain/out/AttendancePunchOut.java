package com.sp.selfsp.attendance.punch.domain.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 第三阶段打卡模块输出对象集合。
 */
public final class AttendancePunchOut {

    private AttendancePunchOut() {
    }

    /**
     * 打卡列表输出。
     */
    public static class PunchLogListOut {

        // 列表项用于渲染当前页打卡记录。
        private List<PunchLogItemOut> items;
        // 总数用于前端显示当前过滤条件下的规模。
        private Integer total;
        // 当前页码用于前端分页栏回显目前处于哪一页。
        private Integer page;
        // 当前页大小用于前端回显每页条数选择。
        private Integer pageSize;
        // 总页数用于前端控制上一页、下一页和末页禁用状态。
        private Integer totalPages;
        // 汇总用于让页面先看到处理结果结构。
        private PunchSummaryOut summary;

        public List<PunchLogItemOut> getItems() {
            return items;
        }

        public void setItems(List<PunchLogItemOut> items) {
            this.items = items;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public PunchSummaryOut getSummary() {
            return summary;
        }

        public void setSummary(PunchSummaryOut summary) {
            this.summary = summary;
        }
    }

    /**
     * 打卡列表项输出。
     */
    public static class PunchLogItemOut {

        private Long id;
        private Long employeeId;
        private String employeeNo;
        private String employeeName;
        private String externalEmployeeId;
        private String sourceSystem;
        private String sourceEventId;
        private LocalDateTime punchTime;
        private String punchType;
        private String deviceName;
        private String processStatus;
        private String errorMessage;
        private Long importBatchId;
        private LocalDateTime createdAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeNo() {
            return employeeNo;
        }

        public void setEmployeeNo(String employeeNo) {
            this.employeeNo = employeeNo;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public String getExternalEmployeeId() {
            return externalEmployeeId;
        }

        public void setExternalEmployeeId(String externalEmployeeId) {
            this.externalEmployeeId = externalEmployeeId;
        }

        public String getSourceSystem() {
            return sourceSystem;
        }

        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }

        public String getSourceEventId() {
            return sourceEventId;
        }

        public void setSourceEventId(String sourceEventId) {
            this.sourceEventId = sourceEventId;
        }

        public LocalDateTime getPunchTime() {
            return punchTime;
        }

        public void setPunchTime(LocalDateTime punchTime) {
            this.punchTime = punchTime;
        }

        public String getPunchType() {
            return punchType;
        }

        public void setPunchType(String punchType) {
            this.punchType = punchType;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getProcessStatus() {
            return processStatus;
        }

        public void setProcessStatus(String processStatus) {
            this.processStatus = processStatus;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Long getImportBatchId() {
            return importBatchId;
        }

        public void setImportBatchId(Long importBatchId) {
            this.importBatchId = importBatchId;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    /**
     * 打卡详情输出。
     */
    public static class PunchLogDetailOut extends PunchLogItemOut {

        private String ignoredReason;
        private String rawPayload;
        private List<String> processLogs;
        private List<String> availableActions;

        public String getIgnoredReason() {
            return ignoredReason;
        }

        public void setIgnoredReason(String ignoredReason) {
            this.ignoredReason = ignoredReason;
        }

        public String getRawPayload() {
            return rawPayload;
        }

        public void setRawPayload(String rawPayload) {
            this.rawPayload = rawPayload;
        }

        public List<String> getProcessLogs() {
            return processLogs;
        }

        public void setProcessLogs(List<String> processLogs) {
            this.processLogs = processLogs;
        }

        public List<String> getAvailableActions() {
            return availableActions;
        }

        public void setAvailableActions(List<String> availableActions) {
            this.availableActions = availableActions;
        }
    }

    /**
     * 打卡汇总输出。
     */
    public static class PunchSummaryOut {

        private Integer processed;
        private Integer unmatched;
        private Integer error;
        private Integer duplicate;
        private Integer ignored;

        public Integer getProcessed() {
            return processed;
        }

        public void setProcessed(Integer processed) {
            this.processed = processed;
        }

        public Integer getUnmatched() {
            return unmatched;
        }

        public void setUnmatched(Integer unmatched) {
            this.unmatched = unmatched;
        }

        public Integer getError() {
            return error;
        }

        public void setError(Integer error) {
            this.error = error;
        }

        public Integer getDuplicate() {
            return duplicate;
        }

        public void setDuplicate(Integer duplicate) {
            this.duplicate = duplicate;
        }

        public Integer getIgnored() {
            return ignored;
        }

        public void setIgnored(Integer ignored) {
            this.ignored = ignored;
        }
    }

    /**
     * 手动补录返回。
     */
    public static class PunchManualResultOut {

        private Long id;
        private String processStatus;
        private String messageCode;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProcessStatus() {
            return processStatus;
        }

        public void setProcessStatus(String processStatus) {
            this.processStatus = processStatus;
        }

        public String getMessageCode() {
            return messageCode;
        }

        public void setMessageCode(String messageCode) {
            this.messageCode = messageCode;
        }
    }

    /**
     * CSV 预览返回。
     */
    public static class PunchImportPreviewOut {

        private List<PunchPreviewRowOut> previewRows;
        private PunchImportPreviewSummaryOut summary;

        public List<PunchPreviewRowOut> getPreviewRows() {
            return previewRows;
        }

        public void setPreviewRows(List<PunchPreviewRowOut> previewRows) {
            this.previewRows = previewRows;
        }

        public PunchImportPreviewSummaryOut getSummary() {
            return summary;
        }

        public void setSummary(PunchImportPreviewSummaryOut summary) {
            this.summary = summary;
        }
    }

    /**
     * CSV 预览行。
     */
    public static class PunchPreviewRowOut {

        private Integer rowNumber;
        private String externalEmployeeId;
        private String employeeName;
        private String punchTime;
        private String punchType;
        private String status;
        private String message;

        public Integer getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(Integer rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getExternalEmployeeId() {
            return externalEmployeeId;
        }

        public void setExternalEmployeeId(String externalEmployeeId) {
            this.externalEmployeeId = externalEmployeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public String getPunchTime() {
            return punchTime;
        }

        public void setPunchTime(String punchTime) {
            this.punchTime = punchTime;
        }

        public String getPunchType() {
            return punchType;
        }

        public void setPunchType(String punchType) {
            this.punchType = punchType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * CSV 预览汇总。
     */
    public static class PunchImportPreviewSummaryOut {

        private Integer totalCount;
        private Integer readyCount;
        private Integer unmatchedCount;
        private Integer errorCount;

        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getReadyCount() {
            return readyCount;
        }

        public void setReadyCount(Integer readyCount) {
            this.readyCount = readyCount;
        }

        public Integer getUnmatchedCount() {
            return unmatchedCount;
        }

        public void setUnmatchedCount(Integer unmatchedCount) {
            this.unmatchedCount = unmatchedCount;
        }

        public Integer getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(Integer errorCount) {
            this.errorCount = errorCount;
        }
    }

    /**
     * CSV 导入结果。
     */
    public static class PunchImportResultOut {

        private Long batchId;
        private String fileName;
        private Integer totalCount;
        private Integer successCount;
        private Integer duplicateCount;
        private Integer unmatchedCount;
        private Integer errorCount;
        private Integer ignoredCount;
        private String status;

        public Long getBatchId() {
            return batchId;
        }

        public void setBatchId(Long batchId) {
            this.batchId = batchId;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Integer successCount) {
            this.successCount = successCount;
        }

        public Integer getDuplicateCount() {
            return duplicateCount;
        }

        public void setDuplicateCount(Integer duplicateCount) {
            this.duplicateCount = duplicateCount;
        }

        public Integer getUnmatchedCount() {
            return unmatchedCount;
        }

        public void setUnmatchedCount(Integer unmatchedCount) {
            this.unmatchedCount = unmatchedCount;
        }

        public Integer getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(Integer errorCount) {
            this.errorCount = errorCount;
        }

        public Integer getIgnoredCount() {
            return ignoredCount;
        }

        public void setIgnoredCount(Integer ignoredCount) {
            this.ignoredCount = ignoredCount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
