package com.sp.selfsp.attendance.monthly.domain.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 第六阶段月次汇总与月结输出对象集合。
 */
public final class AttendanceMonthlyOut {

    private AttendanceMonthlyOut() {
    }

    /**
     * 月次列表输出。
     */
    public static class MonthlyListOut {

        // 当前页月次结果用于中间列表渲染。
        private List<MonthlyItemOut> items;
        // 总数用于分页栏显示总规模。
        private Integer total;
        // 当前页码用于回显当前浏览位置。
        private Integer page;
        // 每页条数用于回显数据库分页口径。
        private Integer pageSize;
        // 总页数用于上一页、下一页与末页控制。
        private Integer totalPages;
        // 汇总用于顶部统计卡显示未结、可结、已结和反结数量。
        private MonthlySummaryOut summary;

        public List<MonthlyItemOut> getItems() {
            return items;
        }

        public void setItems(List<MonthlyItemOut> items) {
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

        public MonthlySummaryOut getSummary() {
            return summary;
        }

        public void setSummary(MonthlySummaryOut summary) {
            this.summary = summary;
        }
    }

    /**
     * 月次列表项输出。
     */
    public static class MonthlyItemOut {

        private Long monthlyId;
        private String yearMonth;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private String workplaceName;
        private String departmentName;
        private Integer scheduledDays;
        private Integer attendanceDays;
        private Integer normalDays;
        private Integer lateCount;
        private Integer earlyLeaveCount;
        private Integer missingPunchCount;
        private Integer absenceCount;
        private Integer exceptionDays;
        private BigDecimal paidLeaveDays;
        private BigDecimal restDays;
        private String closeStatus;
        private Integer blockReasonCount;
        private LocalDateTime updatedAt;

        public Long getMonthlyId() {
            return monthlyId;
        }

        public void setMonthlyId(Long monthlyId) {
            this.monthlyId = monthlyId;
        }

        public String getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public void setEmployeeCode(String employeeCode) {
            this.employeeCode = employeeCode;
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

        public Integer getScheduledDays() {
            return scheduledDays;
        }

        public void setScheduledDays(Integer scheduledDays) {
            this.scheduledDays = scheduledDays;
        }

        public Integer getAttendanceDays() {
            return attendanceDays;
        }

        public void setAttendanceDays(Integer attendanceDays) {
            this.attendanceDays = attendanceDays;
        }

        public Integer getNormalDays() {
            return normalDays;
        }

        public void setNormalDays(Integer normalDays) {
            this.normalDays = normalDays;
        }

        public Integer getLateCount() {
            return lateCount;
        }

        public void setLateCount(Integer lateCount) {
            this.lateCount = lateCount;
        }

        public Integer getEarlyLeaveCount() {
            return earlyLeaveCount;
        }

        public void setEarlyLeaveCount(Integer earlyLeaveCount) {
            this.earlyLeaveCount = earlyLeaveCount;
        }

        public Integer getMissingPunchCount() {
            return missingPunchCount;
        }

        public void setMissingPunchCount(Integer missingPunchCount) {
            this.missingPunchCount = missingPunchCount;
        }

        public Integer getAbsenceCount() {
            return absenceCount;
        }

        public void setAbsenceCount(Integer absenceCount) {
            this.absenceCount = absenceCount;
        }

        public Integer getExceptionDays() {
            return exceptionDays;
        }

        public void setExceptionDays(Integer exceptionDays) {
            this.exceptionDays = exceptionDays;
        }

        public BigDecimal getPaidLeaveDays() {
            return paidLeaveDays;
        }

        public void setPaidLeaveDays(BigDecimal paidLeaveDays) {
            this.paidLeaveDays = paidLeaveDays;
        }

        public BigDecimal getRestDays() {
            return restDays;
        }

        public void setRestDays(BigDecimal restDays) {
            this.restDays = restDays;
        }

        public String getCloseStatus() {
            return closeStatus;
        }

        public void setCloseStatus(String closeStatus) {
            this.closeStatus = closeStatus;
        }

        public Integer getBlockReasonCount() {
            return blockReasonCount;
        }

        public void setBlockReasonCount(Integer blockReasonCount) {
            this.blockReasonCount = blockReasonCount;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    /**
     * 月次汇总卡输出。
     */
    public static class MonthlySummaryOut {

        private Integer openCount;
        private Integer closableCount;
        private Integer closedCount;
        private Integer reopenedCount;

        public Integer getOpenCount() {
            return openCount;
        }

        public void setOpenCount(Integer openCount) {
            this.openCount = openCount;
        }

        public Integer getClosableCount() {
            return closableCount;
        }

        public void setClosableCount(Integer closableCount) {
            this.closableCount = closableCount;
        }

        public Integer getClosedCount() {
            return closedCount;
        }

        public void setClosedCount(Integer closedCount) {
            this.closedCount = closedCount;
        }

        public Integer getReopenedCount() {
            return reopenedCount;
        }

        public void setReopenedCount(Integer reopenedCount) {
            this.reopenedCount = reopenedCount;
        }
    }

    /**
     * 月次详情输出。
     */
    public static class MonthlyDetailOut extends MonthlyItemOut {

        // 月次统计项用于解释每个数字由什么构成。
        private List<MonthlyMetricItemOut> items;
        // 阻塞原因用于告诉用户为什么当前记录不能月结。
        private List<BlockReasonOut> blockReasons;
        // 最近动作日志用于展示重算、月结和反结历史。
        private List<ActionLogOut> actionLogs;
        // 月内日次快照用于右侧详情解释来源数据。
        private List<DailySnapshotOut> dailySnapshots;
        // 月结时间用于详情区展示月结完成时刻。
        private LocalDateTime closedAt;
        // 月结操作人用于详情区展示是谁完成了月结。
        private Long closedBy;
        // 反结时间用于详情区展示最近反结时间。
        private LocalDateTime reopenedAt;
        // 反结操作人用于详情区展示是谁发起了反结。
        private Long reopenedBy;
        // 月结备注用于右侧详情显示业务说明。
        private String remark;

        public List<MonthlyMetricItemOut> getItems() {
            return items;
        }

        public void setItems(List<MonthlyMetricItemOut> items) {
            this.items = items;
        }

        public List<BlockReasonOut> getBlockReasons() {
            return blockReasons;
        }

        public void setBlockReasons(List<BlockReasonOut> blockReasons) {
            this.blockReasons = blockReasons;
        }

        public List<ActionLogOut> getActionLogs() {
            return actionLogs;
        }

        public void setActionLogs(List<ActionLogOut> actionLogs) {
            this.actionLogs = actionLogs;
        }

        public List<DailySnapshotOut> getDailySnapshots() {
            return dailySnapshots;
        }

        public void setDailySnapshots(List<DailySnapshotOut> dailySnapshots) {
            this.dailySnapshots = dailySnapshots;
        }

        public LocalDateTime getClosedAt() {
            return closedAt;
        }

        public void setClosedAt(LocalDateTime closedAt) {
            this.closedAt = closedAt;
        }

        public Long getClosedBy() {
            return closedBy;
        }

        public void setClosedBy(Long closedBy) {
            this.closedBy = closedBy;
        }

        public LocalDateTime getReopenedAt() {
            return reopenedAt;
        }

        public void setReopenedAt(LocalDateTime reopenedAt) {
            this.reopenedAt = reopenedAt;
        }

        public Long getReopenedBy() {
            return reopenedBy;
        }

        public void setReopenedBy(Long reopenedBy) {
            this.reopenedBy = reopenedBy;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    /**
     * 月次统计项输出。
     */
    public static class MonthlyMetricItemOut {

        private String itemCode;
        private String itemName;
        private String itemValue;
        private Integer itemOrder;
        private String sourceJson;

        public String getItemCode() {
            return itemCode;
        }

        public void setItemCode(String itemCode) {
            this.itemCode = itemCode;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getItemValue() {
            return itemValue;
        }

        public void setItemValue(String itemValue) {
            this.itemValue = itemValue;
        }

        public Integer getItemOrder() {
            return itemOrder;
        }

        public void setItemOrder(Integer itemOrder) {
            this.itemOrder = itemOrder;
        }

        public String getSourceJson() {
            return sourceJson;
        }

        public void setSourceJson(String sourceJson) {
            this.sourceJson = sourceJson;
        }
    }

    /**
     * 月结阻塞原因输出。
     */
    public static class BlockReasonOut {

        private String blockCode;
        private String blockMessage;
        private LocalDate workDate;

        public String getBlockCode() {
            return blockCode;
        }

        public void setBlockCode(String blockCode) {
            this.blockCode = blockCode;
        }

        public String getBlockMessage() {
            return blockMessage;
        }

        public void setBlockMessage(String blockMessage) {
            this.blockMessage = blockMessage;
        }

        public LocalDate getWorkDate() {
            return workDate;
        }

        public void setWorkDate(LocalDate workDate) {
            this.workDate = workDate;
        }
    }

    /**
     * 月结动作日志输出。
     */
    public static class ActionLogOut {

        private String actionType;
        private Long operatorId;
        private String actionComment;
        private String snapshotJson;
        private LocalDateTime createdAt;

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

        public String getActionComment() {
            return actionComment;
        }

        public void setActionComment(String actionComment) {
            this.actionComment = actionComment;
        }

        public String getSnapshotJson() {
            return snapshotJson;
        }

        public void setSnapshotJson(String snapshotJson) {
            this.snapshotJson = snapshotJson;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    /**
     * 月内日次快照输出。
     */
    public static class DailySnapshotOut {

        private Long dailyId;
        private LocalDate workDate;
        private String workDayType;
        private String scheduleLabel;
        private String status;
        private String finalStatus;
        private String handlingStatus;
        private Boolean exceptionFlag;
        private Boolean lockedFlag;

        public Long getDailyId() {
            return dailyId;
        }

        public void setDailyId(Long dailyId) {
            this.dailyId = dailyId;
        }

        public LocalDate getWorkDate() {
            return workDate;
        }

        public void setWorkDate(LocalDate workDate) {
            this.workDate = workDate;
        }

        public String getWorkDayType() {
            return workDayType;
        }

        public void setWorkDayType(String workDayType) {
            this.workDayType = workDayType;
        }

        public String getScheduleLabel() {
            return scheduleLabel;
        }

        public void setScheduleLabel(String scheduleLabel) {
            this.scheduleLabel = scheduleLabel;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getFinalStatus() {
            return finalStatus;
        }

        public void setFinalStatus(String finalStatus) {
            this.finalStatus = finalStatus;
        }

        public String getHandlingStatus() {
            return handlingStatus;
        }

        public void setHandlingStatus(String handlingStatus) {
            this.handlingStatus = handlingStatus;
        }

        public Boolean getExceptionFlag() {
            return exceptionFlag;
        }

        public void setExceptionFlag(Boolean exceptionFlag) {
            this.exceptionFlag = exceptionFlag;
        }

        public Boolean getLockedFlag() {
            return lockedFlag;
        }

        public void setLockedFlag(Boolean lockedFlag) {
            this.lockedFlag = lockedFlag;
        }
    }

    /**
     * 重算结果输出。
     */
    public static class MonthlyRecalculateResultOut {

        private Integer requestedCount;
        private Integer successCount;
        private Integer failedCount;

        public Integer getRequestedCount() {
            return requestedCount;
        }

        public void setRequestedCount(Integer requestedCount) {
            this.requestedCount = requestedCount;
        }

        public Integer getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Integer successCount) {
            this.successCount = successCount;
        }

        public Integer getFailedCount() {
            return failedCount;
        }

        public void setFailedCount(Integer failedCount) {
            this.failedCount = failedCount;
        }
    }

    /**
     * 月结动作结果输出。
     */
    public static class MonthlyCloseResultOut {

        private Integer closedCount;
        private Integer blockedCount;

        public Integer getClosedCount() {
            return closedCount;
        }

        public void setClosedCount(Integer closedCount) {
            this.closedCount = closedCount;
        }

        public Integer getBlockedCount() {
            return blockedCount;
        }

        public void setBlockedCount(Integer blockedCount) {
            this.blockedCount = blockedCount;
        }
    }

    /**
     * 月次导出结果输出。
     */
    public static class MonthlyExportOut {

        private String fileName;
        private String content;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
