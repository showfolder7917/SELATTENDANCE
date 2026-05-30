package com.sp.selfsp.attendance.casefile.domain.out;

import com.sp.selfsp.attendance.daily.domain.out.AttendanceDailyOut;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 第五阶段异常处理与审批输出对象集合。
 */
public final class AttendanceCaseOut {

    private AttendanceCaseOut() {
    }

    /**
     * 处理单列表输出。
     */
    public static class CaseListOut {

        // 当前页处理单用于中间列表渲染。
        private List<CaseItemOut> items;
        // 总数用于分页栏展示整体规模。
        private Integer total;
        // 当前页码用于回显当前浏览页。
        private Integer page;
        // 当前页大小用于回显每页条数。
        private Integer pageSize;
        // 总页数用于控制上一页、下一页与末页禁用。
        private Integer totalPages;
        // 汇总用于顶部统计卡展示当前阶段流转情况。
        private CaseSummaryOut summary;

        public List<CaseItemOut> getItems() {
            return items;
        }

        public void setItems(List<CaseItemOut> items) {
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

        public CaseSummaryOut getSummary() {
            return summary;
        }

        public void setSummary(CaseSummaryOut summary) {
            this.summary = summary;
        }
    }

    /**
     * 处理单列表项输出。
     */
    public static class CaseItemOut {

        private Long caseId;
        private Long attendanceDailyId;
        private Long employeeId;
        private String employeeNo;
        private String employeeName;
        private String workplaceName;
        private String departmentName;
        private LocalDate workDate;
        private String currentException;
        private String caseType;
        private String caseStatus;
        private String handlingStatus;
        private Long applicantId;
        private String applicantRole;
        private Long currentApproverId;
        private LocalDateTime submittedAt;
        private LocalDateTime updatedAt;
        private Boolean lockedFlag;
        private Boolean pseudoCase;

        public Long getCaseId() {
            return caseId;
        }

        public void setCaseId(Long caseId) {
            this.caseId = caseId;
        }

        public Long getAttendanceDailyId() {
            return attendanceDailyId;
        }

        public void setAttendanceDailyId(Long attendanceDailyId) {
            this.attendanceDailyId = attendanceDailyId;
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

        public String getWorkplaceName() {
            return workplaceName;
        }

        public void setWorkplaceName(String workplaceName) {
            this.workplaceName = workplaceName;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public LocalDate getWorkDate() {
            return workDate;
        }

        public void setWorkDate(LocalDate workDate) {
            this.workDate = workDate;
        }

        public String getCurrentException() {
            return currentException;
        }

        public void setCurrentException(String currentException) {
            this.currentException = currentException;
        }

        public String getCaseType() {
            return caseType;
        }

        public void setCaseType(String caseType) {
            this.caseType = caseType;
        }

        public String getCaseStatus() {
            return caseStatus;
        }

        public void setCaseStatus(String caseStatus) {
            this.caseStatus = caseStatus;
        }

        public String getHandlingStatus() {
            return handlingStatus;
        }

        public void setHandlingStatus(String handlingStatus) {
            this.handlingStatus = handlingStatus;
        }

        public Long getApplicantId() {
            return applicantId;
        }

        public void setApplicantId(Long applicantId) {
            this.applicantId = applicantId;
        }

        public String getApplicantRole() {
            return applicantRole;
        }

        public void setApplicantRole(String applicantRole) {
            this.applicantRole = applicantRole;
        }

        public Long getCurrentApproverId() {
            return currentApproverId;
        }

        public void setCurrentApproverId(Long currentApproverId) {
            this.currentApproverId = currentApproverId;
        }

        public LocalDateTime getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(LocalDateTime submittedAt) {
            this.submittedAt = submittedAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public Boolean getLockedFlag() {
            return lockedFlag;
        }

        public void setLockedFlag(Boolean lockedFlag) {
            this.lockedFlag = lockedFlag;
        }

        public Boolean getPseudoCase() {
            return pseudoCase;
        }

        public void setPseudoCase(Boolean pseudoCase) {
            this.pseudoCase = pseudoCase;
        }
    }

    /**
     * 处理单详情输出。
     */
    public static class CaseDetailOut extends CaseItemOut {

        private String reasonCategory;
        private String reasonText;
        private String expectedResolution;
        private String finalStatus;
        private LocalDateTime finalClockIn;
        private LocalDateTime finalClockOut;
        private Integer finalBreakMinutes;
        private String finalRemark;
        private AttendanceDailyOut.DailyDetailOut dailyDetail;
        private List<CaseActionLogOut> actionLogs;
        private List<String> availableActions;

        public String getReasonCategory() {
            return reasonCategory;
        }

        public void setReasonCategory(String reasonCategory) {
            this.reasonCategory = reasonCategory;
        }

        public String getReasonText() {
            return reasonText;
        }

        public void setReasonText(String reasonText) {
            this.reasonText = reasonText;
        }

        public String getExpectedResolution() {
            return expectedResolution;
        }

        public void setExpectedResolution(String expectedResolution) {
            this.expectedResolution = expectedResolution;
        }

        public String getFinalStatus() {
            return finalStatus;
        }

        public void setFinalStatus(String finalStatus) {
            this.finalStatus = finalStatus;
        }

        public LocalDateTime getFinalClockIn() {
            return finalClockIn;
        }

        public void setFinalClockIn(LocalDateTime finalClockIn) {
            this.finalClockIn = finalClockIn;
        }

        public LocalDateTime getFinalClockOut() {
            return finalClockOut;
        }

        public void setFinalClockOut(LocalDateTime finalClockOut) {
            this.finalClockOut = finalClockOut;
        }

        public Integer getFinalBreakMinutes() {
            return finalBreakMinutes;
        }

        public void setFinalBreakMinutes(Integer finalBreakMinutes) {
            this.finalBreakMinutes = finalBreakMinutes;
        }

        public String getFinalRemark() {
            return finalRemark;
        }

        public void setFinalRemark(String finalRemark) {
            this.finalRemark = finalRemark;
        }

        public AttendanceDailyOut.DailyDetailOut getDailyDetail() {
            return dailyDetail;
        }

        public void setDailyDetail(AttendanceDailyOut.DailyDetailOut dailyDetail) {
            this.dailyDetail = dailyDetail;
        }

        public List<CaseActionLogOut> getActionLogs() {
            return actionLogs;
        }

        public void setActionLogs(List<CaseActionLogOut> actionLogs) {
            this.actionLogs = actionLogs;
        }

        public List<String> getAvailableActions() {
            return availableActions;
        }

        public void setAvailableActions(List<String> availableActions) {
            this.availableActions = availableActions;
        }
    }

    /**
     * 处理单动作日志输出。
     */
    public static class CaseActionLogOut {

        private Long id;
        private String actionType;
        private Long operatorId;
        private String operatorRole;
        private String actionComment;
        private LocalDateTime createdAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Long operatorId) {
            this.operatorId = operatorId;
        }

        public String getOperatorRole() {
            return operatorRole;
        }

        public void setOperatorRole(String operatorRole) {
            this.operatorRole = operatorRole;
        }

        public String getActionComment() {
            return actionComment;
        }

        public void setActionComment(String actionComment) {
            this.actionComment = actionComment;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    /**
     * 顶部统计卡输出。
     */
    public static class CaseSummaryOut {

        private Integer pendingCount;
        private Integer reviewingCount;
        private Integer approvedCount;
        private Integer rejectedCount;
        private Integer lockedCount;

        public Integer getPendingCount() {
            return pendingCount;
        }

        public void setPendingCount(Integer pendingCount) {
            this.pendingCount = pendingCount;
        }

        public Integer getReviewingCount() {
            return reviewingCount;
        }

        public void setReviewingCount(Integer reviewingCount) {
            this.reviewingCount = reviewingCount;
        }

        public Integer getApprovedCount() {
            return approvedCount;
        }

        public void setApprovedCount(Integer approvedCount) {
            this.approvedCount = approvedCount;
        }

        public Integer getRejectedCount() {
            return rejectedCount;
        }

        public void setRejectedCount(Integer rejectedCount) {
            this.rejectedCount = rejectedCount;
        }

        public Integer getLockedCount() {
            return lockedCount;
        }

        public void setLockedCount(Integer lockedCount) {
            this.lockedCount = lockedCount;
        }
    }

    /**
     * 创建、审批、批量动作统一结果。
     */
    public static class CaseMutationOut {

        private Long caseId;
        private Long attendanceDailyId;
        private String caseStatus;
        private String handlingStatus;

        public Long getCaseId() {
            return caseId;
        }

        public void setCaseId(Long caseId) {
            this.caseId = caseId;
        }

        public Long getAttendanceDailyId() {
            return attendanceDailyId;
        }

        public void setAttendanceDailyId(Long attendanceDailyId) {
            this.attendanceDailyId = attendanceDailyId;
        }

        public String getCaseStatus() {
            return caseStatus;
        }

        public void setCaseStatus(String caseStatus) {
            this.caseStatus = caseStatus;
        }

        public String getHandlingStatus() {
            return handlingStatus;
        }

        public void setHandlingStatus(String handlingStatus) {
            this.handlingStatus = handlingStatus;
        }
    }
}
