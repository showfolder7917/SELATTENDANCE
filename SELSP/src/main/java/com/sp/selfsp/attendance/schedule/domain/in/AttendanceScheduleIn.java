package com.sp.selfsp.attendance.schedule.domain.in;

import java.time.LocalDate;
import java.util.List;

/**
 * 第二阶段排班模块输入对象集合。
 */
// 定义 排班输入集合，承接当前文件对应的业务职责。
public final class AttendanceScheduleIn {

    // 定义 排班输入集合 处理入口，承接当前业务动作。
    private AttendanceScheduleIn() {
    }

    /**
     * 排班看板查询入参。
     */
    // 定义 排班看板查询In，承接当前文件对应的业务职责。
    public static class ScheduleBoardQueryIn {

        // 查询月份用于决定当前日历区间和未排班计算范围。
        // 声明 月份 字段，用来承载当前业务对象的传输信息。
        private String month;
        // 事业所筛选用于只查看某个地点的员工排班。
        // 声明 事业所Id 字段，用来承载当前业务对象的传输信息。
        private Long workplaceId;
        // 部门筛选用于把排班焦点收敛到具体组织单元。
        // 声明 部门Id 字段，用来承载当前业务对象的传输信息。
        private Long departmentId;
        // 关键字筛选用于按员工编号或姓名快速定位目标员工。
        // 声明 员工关键字 字段，用来承载当前业务对象的传输信息。
        private String employeeKeyword;
        // 只看未排班用于帮助管理员优先处理仍有缺口的员工。
        // 声明 只看未排班 字段，用来承载当前业务对象的传输信息。
        private Boolean onlyUnassigned;

        // 对外返回 月份，供上下游继续读取当前业务字段。
        public String getMonth() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return month;
        }

        // 回填 月份，让请求绑定或结果组装保存当前字段值。
        public void setMonth(String month) {
            // 把外部传入结果写入 月份 字段，供后续流程继续使用。
            this.month = month;
        }

        // 对外返回 事业所Id，供上下游继续读取当前业务字段。
        public Long getWorkplaceId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return workplaceId;
        }

        // 回填 事业所Id，让请求绑定或结果组装保存当前字段值。
        public void setWorkplaceId(Long workplaceId) {
            // 把外部传入结果写入 事业所Id 字段，供后续流程继续使用。
            this.workplaceId = workplaceId;
        }

        // 对外返回 部门Id，供上下游继续读取当前业务字段。
        public Long getDepartmentId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return departmentId;
        }

        // 回填 部门Id，让请求绑定或结果组装保存当前字段值。
        public void setDepartmentId(Long departmentId) {
            // 把外部传入结果写入 部门Id 字段，供后续流程继续使用。
            this.departmentId = departmentId;
        }

        // 对外返回 员工关键字，供上下游继续读取当前业务字段。
        public String getEmployeeKeyword() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return employeeKeyword;
        }

        // 回填 员工关键字，让请求绑定或结果组装保存当前字段值。
        public void setEmployeeKeyword(String employeeKeyword) {
            // 把外部传入结果写入 员工关键字 字段，供后续流程继续使用。
            this.employeeKeyword = employeeKeyword;
        }

        // 对外返回 只看未排班，供上下游继续读取当前业务字段。
        public Boolean getOnlyUnassigned() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return onlyUnassigned;
        }

        // 回填 只看未排班，让请求绑定或结果组装保存当前字段值。
        public void setOnlyUnassigned(Boolean onlyUnassigned) {
            // 把外部传入结果写入 只看未排班 字段，供后续流程继续使用。
            this.onlyUnassigned = onlyUnassigned;
        }
    }

    /**
     * 单日排班保存入参。
     */
    // 定义 单日排班保存In，承接当前文件对应的业务职责。
    public static class ScheduleSaveIn {

        // 员工主键用于把班次绑定到具体人。
        // 声明 员工Id 字段，用来承载当前业务对象的传输信息。
        private Long employeeId;
        // 工作日期用于确定排班落在哪一天。
        // 声明 工作日期 字段，用来承载当前业务对象的传输信息。
        private LocalDate workDate;
        // 班次模板主键用于复用第一阶段准备好的班次配置。
        // 声明 班次模板Id 字段，用来承载当前业务对象的传输信息。
        private Long shiftTemplateId;
        // 备注用于记录临时说明或特殊交接要求。
        // 声明 备注 字段，用来承载当前业务对象的传输信息。
        private String remark;

        // 对外返回 员工Id，供上下游继续读取当前业务字段。
        public Long getEmployeeId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return employeeId;
        }

        // 回填 员工Id，让请求绑定或结果组装保存当前字段值。
        public void setEmployeeId(Long employeeId) {
            // 把外部传入结果写入 员工Id 字段，供后续流程继续使用。
            this.employeeId = employeeId;
        }

        // 对外返回 工作日期，供上下游继续读取当前业务字段。
        public LocalDate getWorkDate() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return workDate;
        }

        // 回填 工作日期，让请求绑定或结果组装保存当前字段值。
        public void setWorkDate(LocalDate workDate) {
            // 把外部传入结果写入 工作日期 字段，供后续流程继续使用。
            this.workDate = workDate;
        }

        // 对外返回 班次模板Id，供上下游继续读取当前业务字段。
        public Long getShiftTemplateId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return shiftTemplateId;
        }

        // 回填 班次模板Id，让请求绑定或结果组装保存当前字段值。
        public void setShiftTemplateId(Long shiftTemplateId) {
            // 把外部传入结果写入 班次模板Id 字段，供后续流程继续使用。
            this.shiftTemplateId = shiftTemplateId;
        }

        // 对外返回 备注，供上下游继续读取当前业务字段。
        public String getRemark() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return remark;
        }

        // 回填 备注，让请求绑定或结果组装保存当前字段值。
        public void setRemark(String remark) {
            // 把外部传入结果写入 备注 字段，供后续流程继续使用。
            this.remark = remark;
        }
    }

    /**
     * 批量排班入参。
     */
    // 定义 批量排班In，承接当前文件对应的业务职责。
    public static class ScheduleBatchAssignIn {

        // 员工列表用于让同一组人一次性应用同一模板。
        // 声明 员工Ids 字段，用来承载当前业务对象的传输信息。
        private List<Long> employeeIds;
        // 开始日期用于确定批量作用起点。
        // 声明 开始日期 字段，用来承载当前业务对象的传输信息。
        private LocalDate startDate;
        // 结束日期用于确定批量作用终点。
        // 声明 结束日期 字段，用来承载当前业务对象的传输信息。
        private LocalDate endDate;
        // 班次模板主键用于决定这一批日期使用什么班次。
        // 声明 班次模板Id 字段，用来承载当前业务对象的传输信息。
        private Long shiftTemplateId;
        // 跳过已有排班用于只补空白格子，不动现有安排。
        // 声明 跳过已有排班 字段，用来承载当前业务对象的传输信息。
        private Boolean skipExisting;
        // 覆盖已有排班用于明确允许改写现有排班。
        // 声明 覆盖已有排班 字段，用来承载当前业务对象的传输信息。
        private Boolean overwriteExisting;
        // 批量备注用于记录本次批量操作的统一说明。
        // 声明 备注 字段，用来承载当前业务对象的传输信息。
        private String remark;

        // 对外返回 员工Ids，供上下游继续读取当前业务字段。
        public List<Long> getEmployeeIds() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return employeeIds;
        }

        // 回填 员工Ids，让请求绑定或结果组装保存当前字段值。
        public void setEmployeeIds(List<Long> employeeIds) {
            // 把外部传入结果写入 员工Ids 字段，供后续流程继续使用。
            this.employeeIds = employeeIds;
        }

        // 对外返回 开始日期，供上下游继续读取当前业务字段。
        public LocalDate getStartDate() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return startDate;
        }

        // 回填 开始日期，让请求绑定或结果组装保存当前字段值。
        public void setStartDate(LocalDate startDate) {
            // 把外部传入结果写入 开始日期 字段，供后续流程继续使用。
            this.startDate = startDate;
        }

        // 对外返回 结束日期，供上下游继续读取当前业务字段。
        public LocalDate getEndDate() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return endDate;
        }

        // 回填 结束日期，让请求绑定或结果组装保存当前字段值。
        public void setEndDate(LocalDate endDate) {
            // 把外部传入结果写入 结束日期 字段，供后续流程继续使用。
            this.endDate = endDate;
        }

        // 对外返回 班次模板Id，供上下游继续读取当前业务字段。
        public Long getShiftTemplateId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return shiftTemplateId;
        }

        // 回填 班次模板Id，让请求绑定或结果组装保存当前字段值。
        public void setShiftTemplateId(Long shiftTemplateId) {
            // 把外部传入结果写入 班次模板Id 字段，供后续流程继续使用。
            this.shiftTemplateId = shiftTemplateId;
        }

        // 对外返回 跳过已有排班，供上下游继续读取当前业务字段。
        public Boolean getSkipExisting() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return skipExisting;
        }

        // 回填 跳过已有排班，让请求绑定或结果组装保存当前字段值。
        public void setSkipExisting(Boolean skipExisting) {
            // 把外部传入结果写入 跳过已有排班 字段，供后续流程继续使用。
            this.skipExisting = skipExisting;
        }

        // 对外返回 覆盖已有排班，供上下游继续读取当前业务字段。
        public Boolean getOverwriteExisting() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return overwriteExisting;
        }

        // 回填 覆盖已有排班，让请求绑定或结果组装保存当前字段值。
        public void setOverwriteExisting(Boolean overwriteExisting) {
            // 把外部传入结果写入 覆盖已有排班 字段，供后续流程继续使用。
            this.overwriteExisting = overwriteExisting;
        }

        // 对外返回 备注，供上下游继续读取当前业务字段。
        public String getRemark() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return remark;
        }

        // 回填 备注，让请求绑定或结果组装保存当前字段值。
        public void setRemark(String remark) {
            // 把外部传入结果写入 备注 字段，供后续流程继续使用。
            this.remark = remark;
        }
    }

    /**
     * 复制排班入参。
     */
    // 定义 复制排班In，承接当前文件对应的业务职责。
    public static class ScheduleCopyIn {

        // 当前视图开始日期用于确定需要复制到哪一段目标区间。
        // 声明 开始日期 字段，用来承载当前业务对象的传输信息。
        private LocalDate startDate;
        // 当前视图结束日期用于确定复制覆盖范围。
        // 声明 结束日期 字段，用来承载当前业务对象的传输信息。
        private LocalDate endDate;
        // 员工列表用于只复制当前筛选到的对象。
        // 声明 员工Ids 字段，用来承载当前业务对象的传输信息。
        private List<Long> employeeIds;
        // 覆盖已有排班用于决定复制时是否允许改写现有记录。
        // 声明 覆盖已有排班 字段，用来承载当前业务对象的传输信息。
        private Boolean overwriteExisting;

        // 对外返回 开始日期，供上下游继续读取当前业务字段。
        public LocalDate getStartDate() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return startDate;
        }

        // 回填 开始日期，让请求绑定或结果组装保存当前字段值。
        public void setStartDate(LocalDate startDate) {
            // 把外部传入结果写入 开始日期 字段，供后续流程继续使用。
            this.startDate = startDate;
        }

        // 对外返回 结束日期，供上下游继续读取当前业务字段。
        public LocalDate getEndDate() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return endDate;
        }

        // 回填 结束日期，让请求绑定或结果组装保存当前字段值。
        public void setEndDate(LocalDate endDate) {
            // 把外部传入结果写入 结束日期 字段，供后续流程继续使用。
            this.endDate = endDate;
        }

        // 对外返回 员工Ids，供上下游继续读取当前业务字段。
        public List<Long> getEmployeeIds() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return employeeIds;
        }

        // 回填 员工Ids，让请求绑定或结果组装保存当前字段值。
        public void setEmployeeIds(List<Long> employeeIds) {
            // 把外部传入结果写入 员工Ids 字段，供后续流程继续使用。
            this.employeeIds = employeeIds;
        }

        // 对外返回 覆盖已有排班，供上下游继续读取当前业务字段。
        public Boolean getOverwriteExisting() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return overwriteExisting;
        }

        // 回填 覆盖已有排班，让请求绑定或结果组装保存当前字段值。
        public void setOverwriteExisting(Boolean overwriteExisting) {
            // 把外部传入结果写入 覆盖已有排班 字段，供后续流程继续使用。
            this.overwriteExisting = overwriteExisting;
        }
    }

    /**
     * 清空排班入参。
     */
    // 定义 清空排班In，承接当前文件对应的业务职责。
    public static class ScheduleClearRangeIn {

        // 员工列表用于决定要清掉哪些人的排班。
        // 声明 员工Ids 字段，用来承载当前业务对象的传输信息。
        private List<Long> employeeIds;
        // 起始日期用于确定清空范围。
        // 声明 开始日期 字段，用来承载当前业务对象的传输信息。
        private LocalDate startDate;
        // 结束日期用于确定清空范围。
        // 声明 结束日期 字段，用来承载当前业务对象的传输信息。
        private LocalDate endDate;

        // 对外返回 员工Ids，供上下游继续读取当前业务字段。
        public List<Long> getEmployeeIds() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return employeeIds;
        }

        // 回填 员工Ids，让请求绑定或结果组装保存当前字段值。
        public void setEmployeeIds(List<Long> employeeIds) {
            // 把外部传入结果写入 员工Ids 字段，供后续流程继续使用。
            this.employeeIds = employeeIds;
        }

        // 对外返回 开始日期，供上下游继续读取当前业务字段。
        public LocalDate getStartDate() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return startDate;
        }

        // 回填 开始日期，让请求绑定或结果组装保存当前字段值。
        public void setStartDate(LocalDate startDate) {
            // 把外部传入结果写入 开始日期 字段，供后续流程继续使用。
            this.startDate = startDate;
        }

        // 对外返回 结束日期，供上下游继续读取当前业务字段。
        public LocalDate getEndDate() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return endDate;
        }

        // 回填 结束日期，让请求绑定或结果组装保存当前字段值。
        public void setEndDate(LocalDate endDate) {
            // 把外部传入结果写入 结束日期 字段，供后续流程继续使用。
            this.endDate = endDate;
        }
    }
}
