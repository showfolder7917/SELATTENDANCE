package com.sp.selfsp.attendance.common.domain.in;

import java.time.LocalDate;

/**
 * 考勤第一阶段输入对象集合。
 */
public final class AttendanceIn {

    private AttendanceIn() {
    }

    /**
     * 公司 / 教室信息保存入参。
     */
    public static class TenantSaveIn {

        // 租户编码用于第一阶段唯一标识当前公司或教室。
        private String tenantCode;
        // 租户名称用于页面标题和导出抬头展示。
        private String tenantName;
        // 联系人用于初始化向导确认基础信息是否完整。
        private String contactName;
        // 联系电话用于后台运营联系。
        private String contactPhone;
        // 联系邮箱用于后续通知和资料追踪。
        private String contactEmail;
        // 时区用于后续日本勤怠计算边界。
        private String timezone;

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        public String getTenantName() {
            return tenantName;
        }

        public void setTenantName(String tenantName) {
            this.tenantName = tenantName;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }

        public String getContactEmail() {
            return contactEmail;
        }

        public void setContactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }
    }

    /**
     * 事业所保存入参。
     */
    public static class WorkplaceSaveIn {

        // 事业所编码用于员工导入按编码关联主数据。
        private String workplaceCode;
        // 事业所名称用于员工归属和向导展示。
        private String workplaceName;
        // 地址用于基础资料完整性展示。
        private String address;
        // 电话用于日常联系。
        private String phone;
        // 状态用于决定事业所是否参与后续业务。
        private String status;

        public String getWorkplaceCode() {
            return workplaceCode;
        }

        public void setWorkplaceCode(String workplaceCode) {
            this.workplaceCode = workplaceCode;
        }

        public String getWorkplaceName() {
            return workplaceName;
        }

        public void setWorkplaceName(String workplaceName) {
            this.workplaceName = workplaceName;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 部门保存入参。
     */
    public static class DepartmentSaveIn {

        // 事业所主键用于限定部门归属地点。
        private Long workplaceId;
        // 父部门主键用于预留层级组织结构。
        private Long parentId;
        // 部门编码用于员工导入时做部门匹配。
        private String departmentCode;
        // 部门名称用于列表展示和筛选。
        private String departmentName;
        // 排序号用于控制列表稳定顺序。
        private Integer sortOrder;
        // 状态用于标记启用或停用。
        private String status;

        public Long getWorkplaceId() {
            return workplaceId;
        }

        public void setWorkplaceId(Long workplaceId) {
            this.workplaceId = workplaceId;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public String getDepartmentCode() {
            return departmentCode;
        }

        public void setDepartmentCode(String departmentCode) {
            this.departmentCode = departmentCode;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 员工保存入参。
     */
    public static class EmployeeSaveIn {

        // 员工编号用于主数据唯一标识。
        private String employeeNo;
        // 员工姓名用于列表、导出和详情展示。
        private String employeeName;
        // 假名用于日本场景检索和展示。
        private String employeeNameKana;
        // 性别用于基础资料占位。
        private String gender;
        // 雇佣类型用于生成默认勤怠规则。
        private String employmentType;
        // 入社日用于后续排班可用性判断。
        private LocalDate hireDate;
        // 退社日用于员工状态说明。
        private LocalDate resignDate;
        // 邮箱用于联系方式展示。
        private String email;
        // 电话用于联系方式展示。
        private String phone;
        // 事业所归属用于后续排班地点范围。
        private Long workplaceId;
        // 部门归属用于管理筛选。
        private Long departmentId;
        // 状态用于启用或停用控制。
        private String status;

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

        public String getEmployeeNameKana() {
            return employeeNameKana;
        }

        public void setEmployeeNameKana(String employeeNameKana) {
            this.employeeNameKana = employeeNameKana;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getEmploymentType() {
            return employmentType;
        }

        public void setEmploymentType(String employmentType) {
            this.employmentType = employmentType;
        }

        public LocalDate getHireDate() {
            return hireDate;
        }

        public void setHireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
        }

        public LocalDate getResignDate() {
            return resignDate;
        }

        public void setResignDate(LocalDate resignDate) {
            this.resignDate = resignDate;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 员工列表查询入参。
     */
    public static class EmployeeQueryIn {

        // 关键字用于匹配姓名和员工编号。
        private String keyword;
        // 部门主键用于员工筛选。
        private Long departmentId;
        // 雇佣类型用于筛选不同工种。
        private String employmentType;
        // 状态用于筛选启停用员工。
        private String status;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getEmploymentType() {
            return employmentType;
        }

        public void setEmploymentType(String employmentType) {
            this.employmentType = employmentType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 第七阶段规则工作台查询入参。
     */
    public static class RuleWorkbenchQueryIn {

        // 月份用于按月读取残业预警和当月适用结果。
        private String yearMonth;
        // 关键字用于同时筛规则名称、员工编号和员工姓名。
        private String keyword;
        // 是否只看启用中的规则，供规则页快速聚焦正式生效数据。
        private Boolean activeOnly;

        public String getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public Boolean getActiveOnly() {
            return activeOnly;
        }

        public void setActiveOnly(Boolean activeOnly) {
            this.activeOnly = activeOnly;
        }
    }

    /**
     * 第七阶段规则配置保存入参。
     */
    public static class RuleSaveIn {

        // 规则编码用于规则主数据唯一定位和员工适用回写。
        private String ruleCode;
        // 规则名称用于列表、弹窗和员工适用页展示。
        private String ruleName;
        // 标准日工时用于残业和预警基线判断。
        private Integer standardDailyMinutes;
        // 标准周工时用于后续周残业扩展和规则解释。
        private Integer standardWeeklyMinutes;
        // 是否开启自动休息，用于第一版日本规则增强的基础口径。
        private Boolean autoBreakEnabled;
        // 超过多少分钟后自动扣休，避免短工时被误扣休息。
        private Integer autoBreakThresholdMinutes;
        // 自动扣休分钟数，供日次计算和规则说明展示。
        private Integer autoBreakDeductMinutes;
        // 深夜开始时间用于后续深夜分钟交集计算。
        private String nightWorkStart;
        // 深夜结束时间用于后续跨日深夜分钟交集计算。
        private String nightWorkEnd;
        // 取整粒度用于迟到、残业和总工时口径统一。
        private Integer roundingUnitMinutes;
        // 取整方式用于向用户解释是进位、舍去还是四舍五入。
        private String roundingMode;
        // 月残业预警阈值用于第七阶段看板高风险提示。
        private Integer monthlyOvertimeAlertHours;
        // 年残业预警阈值用于第七阶段累计风险提示。
        private Integer yearlyOvertimeAlertHours;
        // 是否开启有休提醒，用于年度 5 日提醒入口控制。
        private Boolean paidLeaveReminderEnabled;
        // 是否启用决定这条规则能否被正式分配给员工。
        private Boolean activeFlag;
        // 备注用于管理员记录特殊口径或适用说明。
        private String note;

        public String getRuleCode() {
            return ruleCode;
        }

        public void setRuleCode(String ruleCode) {
            this.ruleCode = ruleCode;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public Integer getStandardDailyMinutes() {
            return standardDailyMinutes;
        }

        public void setStandardDailyMinutes(Integer standardDailyMinutes) {
            this.standardDailyMinutes = standardDailyMinutes;
        }

        public Integer getStandardWeeklyMinutes() {
            return standardWeeklyMinutes;
        }

        public void setStandardWeeklyMinutes(Integer standardWeeklyMinutes) {
            this.standardWeeklyMinutes = standardWeeklyMinutes;
        }

        public Boolean getAutoBreakEnabled() {
            return autoBreakEnabled;
        }

        public void setAutoBreakEnabled(Boolean autoBreakEnabled) {
            this.autoBreakEnabled = autoBreakEnabled;
        }

        public Integer getAutoBreakThresholdMinutes() {
            return autoBreakThresholdMinutes;
        }

        public void setAutoBreakThresholdMinutes(Integer autoBreakThresholdMinutes) {
            this.autoBreakThresholdMinutes = autoBreakThresholdMinutes;
        }

        public Integer getAutoBreakDeductMinutes() {
            return autoBreakDeductMinutes;
        }

        public void setAutoBreakDeductMinutes(Integer autoBreakDeductMinutes) {
            this.autoBreakDeductMinutes = autoBreakDeductMinutes;
        }

        public String getNightWorkStart() {
            return nightWorkStart;
        }

        public void setNightWorkStart(String nightWorkStart) {
            this.nightWorkStart = nightWorkStart;
        }

        public String getNightWorkEnd() {
            return nightWorkEnd;
        }

        public void setNightWorkEnd(String nightWorkEnd) {
            this.nightWorkEnd = nightWorkEnd;
        }

        public Integer getRoundingUnitMinutes() {
            return roundingUnitMinutes;
        }

        public void setRoundingUnitMinutes(Integer roundingUnitMinutes) {
            this.roundingUnitMinutes = roundingUnitMinutes;
        }

        public String getRoundingMode() {
            return roundingMode;
        }

        public void setRoundingMode(String roundingMode) {
            this.roundingMode = roundingMode;
        }

        public Integer getMonthlyOvertimeAlertHours() {
            return monthlyOvertimeAlertHours;
        }

        public void setMonthlyOvertimeAlertHours(Integer monthlyOvertimeAlertHours) {
            this.monthlyOvertimeAlertHours = monthlyOvertimeAlertHours;
        }

        public Integer getYearlyOvertimeAlertHours() {
            return yearlyOvertimeAlertHours;
        }

        public void setYearlyOvertimeAlertHours(Integer yearlyOvertimeAlertHours) {
            this.yearlyOvertimeAlertHours = yearlyOvertimeAlertHours;
        }

        public Boolean getPaidLeaveReminderEnabled() {
            return paidLeaveReminderEnabled;
        }

        public void setPaidLeaveReminderEnabled(Boolean paidLeaveReminderEnabled) {
            this.paidLeaveReminderEnabled = paidLeaveReminderEnabled;
        }

        public Boolean getActiveFlag() {
            return activeFlag;
        }

        public void setActiveFlag(Boolean activeFlag) {
            this.activeFlag = activeFlag;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    /**
     * 第七阶段员工规则适用保存入参。
     */
    public static class RuleAssignmentSaveIn {

        // 规则主键决定员工本次要套用哪条正式规则。
        private Long ruleId;
        // 生效开始日用于月次和日次判断哪天开始改按新规则计算。
        private LocalDate effectiveStartDate;
        // 生效结束日用于保留阶段性规则或临时规则切换记录。
        private LocalDate effectiveEndDate;
        // 适用备注用于记录人工调整背景。
        private String note;

        public Long getRuleId() {
            return ruleId;
        }

        public void setRuleId(Long ruleId) {
            this.ruleId = ruleId;
        }

        public LocalDate getEffectiveStartDate() {
            return effectiveStartDate;
        }

        public void setEffectiveStartDate(LocalDate effectiveStartDate) {
            this.effectiveStartDate = effectiveStartDate;
        }

        public LocalDate getEffectiveEndDate() {
            return effectiveEndDate;
        }

        public void setEffectiveEndDate(LocalDate effectiveEndDate) {
            this.effectiveEndDate = effectiveEndDate;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    /**
     * 外部打卡绑定入参。
     */
    public static class ExternalMappingSaveIn {

        // 外部系统名称用于标识打卡来源。
        private String sourceSystem;
        // 外部员工 ID 用于后续对接真实打卡平台。
        private String externalEmployeeId;
        // 外部员工编号用于人工核对。
        private String externalEmployeeNo;
        // 状态用于记录当前绑定是否启用。
        private String status;

        public String getSourceSystem() {
            return sourceSystem;
        }

        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }

        public String getExternalEmployeeId() {
            return externalEmployeeId;
        }

        public void setExternalEmployeeId(String externalEmployeeId) {
            this.externalEmployeeId = externalEmployeeId;
        }

        public String getExternalEmployeeNo() {
            return externalEmployeeNo;
        }

        public void setExternalEmployeeNo(String externalEmployeeNo) {
            this.externalEmployeeNo = externalEmployeeNo;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 员工导入入参。
     */
    public static class EmployeeImportIn {

        // CSV 文本承接前端粘贴或文件读取后的内容。
        private String csvText;

        public String getCsvText() {
            return csvText;
        }

        public void setCsvText(String csvText) {
            this.csvText = csvText;
        }
    }

    /**
     * 班次模板保存入参。
     */
    public static class ShiftTemplateSaveIn {

        // 模板编码用于业务唯一识别。
        private String templateCode;
        // 模板名称用于页面和导出展示。
        private String templateName;
        // 班次类型用于区分工作班、休息、有休。
        private String shiftType;
        // 开始时间用于后续排班和工时计算。
        private String startTime;
        // 结束时间用于后续排班和工时计算。
        private String endTime;
        // 是否跨日用于夜班等场景。
        private Boolean crossDay;
        // 休息分钟数用于后续计算基线。
        private Integer scheduledBreakMinutes;
        // 颜色用于前端展示区分。
        private String color;
        // 是否启用用于保留历史模板。
        private Boolean active;

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public String getShiftType() {
            return shiftType;
        }

        public void setShiftType(String shiftType) {
            this.shiftType = shiftType;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public Boolean getCrossDay() {
            return crossDay;
        }

        public void setCrossDay(Boolean crossDay) {
            this.crossDay = crossDay;
        }

        public Integer getScheduledBreakMinutes() {
            return scheduledBreakMinutes;
        }

        public void setScheduledBreakMinutes(Integer scheduledBreakMinutes) {
            this.scheduledBreakMinutes = scheduledBreakMinutes;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }
}
