package com.sp.selfsp.attendance.schedule.domain.out;

import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 第二阶段排班模块输出对象集合。
 */
public final class AttendanceScheduleOut {

    private AttendanceScheduleOut() {
    }

    /**
     * 排班看板输出。
     */
    public static class ScheduleBoardOut {

        // 月份用于前端顶部标题和导出文件命名。
        private String month;
        // 开始日期用于前端绘制日历头部。
        private LocalDate startDate;
        // 结束日期用于前端绘制日历尾部。
        private LocalDate endDate;
        // 日期列表用于让前端稳定生成每一天的列。
        private List<LocalDate> dates;
        // 员工行列表用于绘制左侧固定员工区。
        private List<ScheduleEmployeeRowOut> employeeRows;
        // 排班明细列表用于填充每个员工日期格子。
        private List<ScheduleItemOut> scheduleItems;
        // 模板列表直接回传给右侧模板面板，避免前端重复拉接口。
        private List<AttendanceOut.ShiftTemplateOut> shiftTemplates;

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public List<LocalDate> getDates() { return dates; }
        public void setDates(List<LocalDate> dates) { this.dates = dates; }
        public List<ScheduleEmployeeRowOut> getEmployeeRows() { return employeeRows; }
        public void setEmployeeRows(List<ScheduleEmployeeRowOut> employeeRows) { this.employeeRows = employeeRows; }
        public List<ScheduleItemOut> getScheduleItems() { return scheduleItems; }
        public void setScheduleItems(List<ScheduleItemOut> scheduleItems) { this.scheduleItems = scheduleItems; }
        public List<AttendanceOut.ShiftTemplateOut> getShiftTemplates() { return shiftTemplates; }
        public void setShiftTemplates(List<AttendanceOut.ShiftTemplateOut> shiftTemplates) { this.shiftTemplates = shiftTemplates; }
    }

    /**
     * 左侧员工行输出。
     */
    public static class ScheduleEmployeeRowOut {

        // 员工主键用于前端定位整行。
        private Long employeeId;
        // 员工编号用于后台快速识别人。
        private String employeeNo;
        // 员工姓名用于主要展示。
        private String employeeName;
        // 假名用于日本场景识别。
        private String employeeNameKana;
        // 部门名称用于缩小排班上下文。
        private String departmentName;
        // 事业所名称用于跨地点排班时快速辨识。
        private String workplaceName;
        // 未排班天数用于左侧高亮提醒需要优先处理的员工。
        private Integer unassignedCount;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getEmployeeNo() { return employeeNo; }
        public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public String getEmployeeNameKana() { return employeeNameKana; }
        public void setEmployeeNameKana(String employeeNameKana) { this.employeeNameKana = employeeNameKana; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
        public String getWorkplaceName() { return workplaceName; }
        public void setWorkplaceName(String workplaceName) { this.workplaceName = workplaceName; }
        public Integer getUnassignedCount() { return unassignedCount; }
        public void setUnassignedCount(Integer unassignedCount) { this.unassignedCount = unassignedCount; }
    }

    /**
     * 单个排班格子输出。
     */
    public static class ScheduleItemOut {

        private Long id;
        private Long employeeId;
        private LocalDate workDate;
        private Long shiftTemplateId;
        private String templateCode;
        private String templateName;
        private String shiftType;
        private String startTime;
        private String endTime;
        private Boolean crossDay;
        private Integer scheduledBreakMinutes;
        private String color;
        private String workDayType;
        private String status;
        private Boolean locked;
        private String remark;
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public LocalDate getWorkDate() { return workDate; }
        public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
        public Long getShiftTemplateId() { return shiftTemplateId; }
        public void setShiftTemplateId(Long shiftTemplateId) { this.shiftTemplateId = shiftTemplateId; }
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public String getShiftType() { return shiftType; }
        public void setShiftType(String shiftType) { this.shiftType = shiftType; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public Boolean getCrossDay() { return crossDay; }
        public void setCrossDay(Boolean crossDay) { this.crossDay = crossDay; }
        public Integer getScheduledBreakMinutes() { return scheduledBreakMinutes; }
        public void setScheduledBreakMinutes(Integer scheduledBreakMinutes) { this.scheduledBreakMinutes = scheduledBreakMinutes; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getWorkDayType() { return workDayType; }
        public void setWorkDayType(String workDayType) { this.workDayType = workDayType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Boolean getLocked() { return locked; }
        public void setLocked(Boolean locked) { this.locked = locked; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public LocalDateTime getScheduledStartTime() { return scheduledStartTime; }
        public void setScheduledStartTime(LocalDateTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }
        public LocalDateTime getScheduledEndTime() { return scheduledEndTime; }
        public void setScheduledEndTime(LocalDateTime scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }
    }

    /**
     * 批量操作结果输出。
     */
    public static class ScheduleBatchResultOut {

        private Integer createdCount;
        private Integer updatedCount;
        private Integer skippedCount;
        private Integer affectedEmployeeCount;
        private Integer affectedDateCount;
        private String message;

        public Integer getCreatedCount() { return createdCount; }
        public void setCreatedCount(Integer createdCount) { this.createdCount = createdCount; }
        public Integer getUpdatedCount() { return updatedCount; }
        public void setUpdatedCount(Integer updatedCount) { this.updatedCount = updatedCount; }
        public Integer getSkippedCount() { return skippedCount; }
        public void setSkippedCount(Integer skippedCount) { this.skippedCount = skippedCount; }
        public Integer getAffectedEmployeeCount() { return affectedEmployeeCount; }
        public void setAffectedEmployeeCount(Integer affectedEmployeeCount) { this.affectedEmployeeCount = affectedEmployeeCount; }
        public Integer getAffectedDateCount() { return affectedDateCount; }
        public void setAffectedDateCount(Integer affectedDateCount) { this.affectedDateCount = affectedDateCount; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 未排班检查输出。
     */
    public static class ScheduleUnassignedOut {

        private Long employeeId;
        private String employeeNo;
        private String employeeName;
        private String departmentName;
        private String workplaceName;
        private Integer unassignedCount;
        private List<LocalDate> missingDates;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getEmployeeNo() { return employeeNo; }
        public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
        public String getWorkplaceName() { return workplaceName; }
        public void setWorkplaceName(String workplaceName) { this.workplaceName = workplaceName; }
        public Integer getUnassignedCount() { return unassignedCount; }
        public void setUnassignedCount(Integer unassignedCount) { this.unassignedCount = unassignedCount; }
        public List<LocalDate> getMissingDates() { return missingDates; }
        public void setMissingDates(List<LocalDate> missingDates) { this.missingDates = missingDates; }
    }
}
