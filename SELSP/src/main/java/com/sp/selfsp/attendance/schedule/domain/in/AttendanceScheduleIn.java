package com.sp.selfsp.attendance.schedule.domain.in;

import java.time.LocalDate;
import java.util.List;

/**
 * 第二阶段排班模块输入对象集合。
 */
public final class AttendanceScheduleIn {

    private AttendanceScheduleIn() {
    }

    /**
     * 排班看板查询入参。
     */
    public static class ScheduleBoardQueryIn {

        // 查询月份用于决定当前日历区间和未排班计算范围。
        private String month;
        // 事业所筛选用于只查看某个地点的员工排班。
        private Long workplaceId;
        // 部门筛选用于把排班焦点收敛到具体组织单元。
        private Long departmentId;
        // 关键字筛选用于按员工编号或姓名快速定位目标员工。
        private String employeeKeyword;
        // 只看未排班用于帮助管理员优先处理仍有缺口的员工。
        private Boolean onlyUnassigned;

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public Long getWorkplaceId() {
            return workplaceId;
        }

        public void setWorkplaceId(Long workplaceId) {
            this.workplaceId = workplaceId;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getEmployeeKeyword() {
            return employeeKeyword;
        }

        public void setEmployeeKeyword(String employeeKeyword) {
            this.employeeKeyword = employeeKeyword;
        }

        public Boolean getOnlyUnassigned() {
            return onlyUnassigned;
        }

        public void setOnlyUnassigned(Boolean onlyUnassigned) {
            this.onlyUnassigned = onlyUnassigned;
        }
    }

    /**
     * 单日排班保存入参。
     */
    public static class ScheduleSaveIn {

        // 员工主键用于把班次绑定到具体人。
        private Long employeeId;
        // 工作日期用于确定排班落在哪一天。
        private LocalDate workDate;
        // 班次模板主键用于复用第一阶段准备好的班次配置。
        private Long shiftTemplateId;
        // 备注用于记录临时说明或特殊交接要求。
        private String remark;

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public LocalDate getWorkDate() {
            return workDate;
        }

        public void setWorkDate(LocalDate workDate) {
            this.workDate = workDate;
        }

        public Long getShiftTemplateId() {
            return shiftTemplateId;
        }

        public void setShiftTemplateId(Long shiftTemplateId) {
            this.shiftTemplateId = shiftTemplateId;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    /**
     * 批量排班入参。
     */
    public static class ScheduleBatchAssignIn {

        // 员工列表用于让同一组人一次性应用同一模板。
        private List<Long> employeeIds;
        // 开始日期用于确定批量作用起点。
        private LocalDate startDate;
        // 结束日期用于确定批量作用终点。
        private LocalDate endDate;
        // 班次模板主键用于决定这一批日期使用什么班次。
        private Long shiftTemplateId;
        // 跳过已有排班用于只补空白格子，不动现有安排。
        private Boolean skipExisting;
        // 覆盖已有排班用于明确允许改写现有排班。
        private Boolean overwriteExisting;
        // 批量备注用于记录本次批量操作的统一说明。
        private String remark;

        public List<Long> getEmployeeIds() {
            return employeeIds;
        }

        public void setEmployeeIds(List<Long> employeeIds) {
            this.employeeIds = employeeIds;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public Long getShiftTemplateId() {
            return shiftTemplateId;
        }

        public void setShiftTemplateId(Long shiftTemplateId) {
            this.shiftTemplateId = shiftTemplateId;
        }

        public Boolean getSkipExisting() {
            return skipExisting;
        }

        public void setSkipExisting(Boolean skipExisting) {
            this.skipExisting = skipExisting;
        }

        public Boolean getOverwriteExisting() {
            return overwriteExisting;
        }

        public void setOverwriteExisting(Boolean overwriteExisting) {
            this.overwriteExisting = overwriteExisting;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    /**
     * 复制排班入参。
     */
    public static class ScheduleCopyIn {

        // 当前视图开始日期用于确定需要复制到哪一段目标区间。
        private LocalDate startDate;
        // 当前视图结束日期用于确定复制覆盖范围。
        private LocalDate endDate;
        // 员工列表用于只复制当前筛选到的对象。
        private List<Long> employeeIds;
        // 覆盖已有排班用于决定复制时是否允许改写现有记录。
        private Boolean overwriteExisting;

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public List<Long> getEmployeeIds() {
            return employeeIds;
        }

        public void setEmployeeIds(List<Long> employeeIds) {
            this.employeeIds = employeeIds;
        }

        public Boolean getOverwriteExisting() {
            return overwriteExisting;
        }

        public void setOverwriteExisting(Boolean overwriteExisting) {
            this.overwriteExisting = overwriteExisting;
        }
    }

    /**
     * 清空排班入参。
     */
    public static class ScheduleClearRangeIn {

        // 员工列表用于决定要清掉哪些人的排班。
        private List<Long> employeeIds;
        // 起始日期用于确定清空范围。
        private LocalDate startDate;
        // 结束日期用于确定清空范围。
        private LocalDate endDate;

        public List<Long> getEmployeeIds() {
            return employeeIds;
        }

        public void setEmployeeIds(List<Long> employeeIds) {
            this.employeeIds = employeeIds;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }
    }
}
