package com.sp.selfsp.attendance.daily.domain.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 第四阶段日次勤怠输出对象集合。
 */
public final class AttendanceDailyOut {

    private AttendanceDailyOut() {
    }

    /**
     * 日次列表输出。
     */
    public static class DailyListOut {

        // 列表项用于渲染当前页日次结果。
        private List<DailyItemOut> items;
        // 总数用于前端分页与摘要回显。
        private Integer total;
        // 当前页码用于前端回显分页状态。
        private Integer page;
        // 当前页大小用于前端回显每页条数。
        private Integer pageSize;
        // 总页数用于前端上一页下一页控制。
        private Integer totalPages;
        // 汇总用于把正常、迟到、缺卡、缺勤优先展示在顶部。
        private DailySummaryOut summary;

        public List<DailyItemOut> getItems() {
            return items;
        }

        public void setItems(List<DailyItemOut> items) {
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

        public DailySummaryOut getSummary() {
            return summary;
        }

        public void setSummary(DailySummaryOut summary) {
            this.summary = summary;
        }
    }

    /**
     * 日次列表项输出。
     */
    public static class DailyItemOut {

        private Long id;
        private Long employeeId;
        private String employeeNo;
        private String employeeName;
        private String departmentName;
        private String workplaceName;
        private LocalDate workDate;
        private String scheduleLabel;
        private LocalDateTime actualClockIn;
        private LocalDateTime actualClockOut;
        private Integer actualBreakMinutes;
        private Integer actualWorkMinutes;
        private Integer normalWorkMinutes;
        private Integer overtimeMinutes;
        private Integer legalOvertimeMinutes;
        private Integer nightWorkMinutes;
        private Integer holidayWorkMinutes;
        private Integer lateMinutes;
        private Integer earlyLeaveMinutes;
        private String holidayType;
        private Long appliedRuleId;
        private String appliedRuleName;
        private String status;
        private Boolean exceptionFlag;

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

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public String getWorkplaceName() {
            return workplaceName;
        }

        public void setWorkplaceName(String workplaceName) {
            this.workplaceName = workplaceName;
        }

        public LocalDate getWorkDate() {
            return workDate;
        }

        public void setWorkDate(LocalDate workDate) {
            this.workDate = workDate;
        }

        public String getScheduleLabel() {
            return scheduleLabel;
        }

        public void setScheduleLabel(String scheduleLabel) {
            this.scheduleLabel = scheduleLabel;
        }

        public LocalDateTime getActualClockIn() {
            return actualClockIn;
        }

        public void setActualClockIn(LocalDateTime actualClockIn) {
            this.actualClockIn = actualClockIn;
        }

        public LocalDateTime getActualClockOut() {
            return actualClockOut;
        }

        public void setActualClockOut(LocalDateTime actualClockOut) {
            this.actualClockOut = actualClockOut;
        }

        public Integer getActualBreakMinutes() {
            return actualBreakMinutes;
        }

        public void setActualBreakMinutes(Integer actualBreakMinutes) {
            this.actualBreakMinutes = actualBreakMinutes;
        }

        public Integer getActualWorkMinutes() {
            return actualWorkMinutes;
        }

        public void setActualWorkMinutes(Integer actualWorkMinutes) {
            this.actualWorkMinutes = actualWorkMinutes;
        }

        public Integer getNormalWorkMinutes() {
            return normalWorkMinutes;
        }

        public void setNormalWorkMinutes(Integer normalWorkMinutes) {
            this.normalWorkMinutes = normalWorkMinutes;
        }

        public Integer getOvertimeMinutes() {
            return overtimeMinutes;
        }

        public void setOvertimeMinutes(Integer overtimeMinutes) {
            this.overtimeMinutes = overtimeMinutes;
        }

        public Integer getLegalOvertimeMinutes() {
            return legalOvertimeMinutes;
        }

        public void setLegalOvertimeMinutes(Integer legalOvertimeMinutes) {
            this.legalOvertimeMinutes = legalOvertimeMinutes;
        }

        public Integer getNightWorkMinutes() {
            return nightWorkMinutes;
        }

        public void setNightWorkMinutes(Integer nightWorkMinutes) {
            this.nightWorkMinutes = nightWorkMinutes;
        }

        public Integer getHolidayWorkMinutes() {
            return holidayWorkMinutes;
        }

        public void setHolidayWorkMinutes(Integer holidayWorkMinutes) {
            this.holidayWorkMinutes = holidayWorkMinutes;
        }

        public Integer getLateMinutes() {
            return lateMinutes;
        }

        public void setLateMinutes(Integer lateMinutes) {
            this.lateMinutes = lateMinutes;
        }

        public Integer getEarlyLeaveMinutes() {
            return earlyLeaveMinutes;
        }

        public void setEarlyLeaveMinutes(Integer earlyLeaveMinutes) {
            this.earlyLeaveMinutes = earlyLeaveMinutes;
        }

        public String getHolidayType() {
            return holidayType;
        }

        public void setHolidayType(String holidayType) {
            this.holidayType = holidayType;
        }

        public Long getAppliedRuleId() {
            return appliedRuleId;
        }

        public void setAppliedRuleId(Long appliedRuleId) {
            this.appliedRuleId = appliedRuleId;
        }

        public String getAppliedRuleName() {
            return appliedRuleName;
        }

        public void setAppliedRuleName(String appliedRuleName) {
            this.appliedRuleName = appliedRuleName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Boolean getExceptionFlag() {
            return exceptionFlag;
        }

        public void setExceptionFlag(Boolean exceptionFlag) {
            this.exceptionFlag = exceptionFlag;
        }
    }

    /**
     * 日次详情输出。
     */
    public static class DailyDetailOut extends DailyItemOut {

        private ScheduleSnapshotOut schedule;
        private List<PunchSnapshotOut> punches;
        private List<CalcStepOut> calcSteps;
        private List<ExceptionOut> exceptions;

        public ScheduleSnapshotOut getSchedule() {
            return schedule;
        }

        public void setSchedule(ScheduleSnapshotOut schedule) {
            this.schedule = schedule;
        }

        public List<PunchSnapshotOut> getPunches() {
            return punches;
        }

        public void setPunches(List<PunchSnapshotOut> punches) {
            this.punches = punches;
        }

        public List<CalcStepOut> getCalcSteps() {
            return calcSteps;
        }

        public void setCalcSteps(List<CalcStepOut> calcSteps) {
            this.calcSteps = calcSteps;
        }

        public List<ExceptionOut> getExceptions() {
            return exceptions;
        }

        public void setExceptions(List<ExceptionOut> exceptions) {
            this.exceptions = exceptions;
        }
    }

    /**
     * 日次汇总输出。
     */
    public static class DailySummaryOut {

        private Integer normalCount;
        private Integer lateCount;
        private Integer missingClockCount;
        private Integer absenceCount;

        public Integer getNormalCount() {
            return normalCount;
        }

        public void setNormalCount(Integer normalCount) {
            this.normalCount = normalCount;
        }

        public Integer getLateCount() {
            return lateCount;
        }

        public void setLateCount(Integer lateCount) {
            this.lateCount = lateCount;
        }

        public Integer getMissingClockCount() {
            return missingClockCount;
        }

        public void setMissingClockCount(Integer missingClockCount) {
            this.missingClockCount = missingClockCount;
        }

        public Integer getAbsenceCount() {
            return absenceCount;
        }

        public void setAbsenceCount(Integer absenceCount) {
            this.absenceCount = absenceCount;
        }
    }

    /**
     * 排班快照输出。
     */
    public static class ScheduleSnapshotOut {

        private Long shiftScheduleId;
        private String label;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer breakMinutes;
        private String workDayType;

        public Long getShiftScheduleId() {
            return shiftScheduleId;
        }

        public void setShiftScheduleId(Long shiftScheduleId) {
            this.shiftScheduleId = shiftScheduleId;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public Integer getBreakMinutes() {
            return breakMinutes;
        }

        public void setBreakMinutes(Integer breakMinutes) {
            this.breakMinutes = breakMinutes;
        }

        public String getWorkDayType() {
            return workDayType;
        }

        public void setWorkDayType(String workDayType) {
            this.workDayType = workDayType;
        }
    }

    /**
     * 打卡快照输出。
     */
    public static class PunchSnapshotOut {

        private Long id;
        private LocalDateTime punchTime;
        private String punchType;
        private String sourceSystem;
        private String deviceName;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public String getSourceSystem() {
            return sourceSystem;
        }

        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }
    }

    /**
     * 计算步骤输出。
     */
    public static class CalcStepOut {

        private String stepName;
        private String stepMessage;

        public String getStepName() {
            return stepName;
        }

        public void setStepName(String stepName) {
            this.stepName = stepName;
        }

        public String getStepMessage() {
            return stepMessage;
        }

        public void setStepMessage(String stepMessage) {
            this.stepMessage = stepMessage;
        }
    }

    /**
     * 异常输出。
     */
    public static class ExceptionOut {

        private String exceptionType;
        private String exceptionLevel;
        private String message;
        private String suggestedAction;

        public String getExceptionType() {
            return exceptionType;
        }

        public void setExceptionType(String exceptionType) {
            this.exceptionType = exceptionType;
        }

        public String getExceptionLevel() {
            return exceptionLevel;
        }

        public void setExceptionLevel(String exceptionLevel) {
            this.exceptionLevel = exceptionLevel;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSuggestedAction() {
            return suggestedAction;
        }

        public void setSuggestedAction(String suggestedAction) {
            this.suggestedAction = suggestedAction;
        }
    }

    /**
     * 重算结果输出。
     */
    public static class RecalculateResultOut {

        private Integer requestedCount;
        private Integer successCount;
        private Integer failedCount;
        private DailyDetailOut detail;

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

        public DailyDetailOut getDetail() {
            return detail;
        }

        public void setDetail(DailyDetailOut detail) {
            this.detail = detail;
        }
    }
}
