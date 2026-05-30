package com.sp.selfsp.attendance.domain.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤第一阶段输出对象集合。
 */
public final class AttendanceOut {

    private AttendanceOut() {
    }

    /**
     * 初始化向导聚合输出。
     */
    public static class BootstrapSummaryOut {

        // 当前租户用于页面头部直接展示公司信息。
        private TenantOut tenant;
        // 向导步骤用于首屏提示当前准备进度。
        private List<BootstrapStepOut> steps;
        // 事业所列表用于前端下拉和主数据卡片。
        private List<WorkplaceOut> workplaces;
        // 部门列表用于前端下拉和部门区块。
        private List<DepartmentOut> departments;
        // 员工列表用于首屏展示最近主数据状态。
        private List<EmployeeOut> employees;
        // 班次模板列表用于首屏模板状态展示。
        private List<ShiftTemplateOut> shiftTemplates;
        // 下一步提示用于告诉管理员先处理什么。
        private String recommendedNextAction;

        public TenantOut getTenant() {
            return tenant;
        }

        public void setTenant(TenantOut tenant) {
            this.tenant = tenant;
        }

        public List<BootstrapStepOut> getSteps() {
            return steps;
        }

        public void setSteps(List<BootstrapStepOut> steps) {
            this.steps = steps;
        }

        public List<WorkplaceOut> getWorkplaces() {
            return workplaces;
        }

        public void setWorkplaces(List<WorkplaceOut> workplaces) {
            this.workplaces = workplaces;
        }

        public List<DepartmentOut> getDepartments() {
            return departments;
        }

        public void setDepartments(List<DepartmentOut> departments) {
            this.departments = departments;
        }

        public List<EmployeeOut> getEmployees() {
            return employees;
        }

        public void setEmployees(List<EmployeeOut> employees) {
            this.employees = employees;
        }

        public List<ShiftTemplateOut> getShiftTemplates() {
            return shiftTemplates;
        }

        public void setShiftTemplates(List<ShiftTemplateOut> shiftTemplates) {
            this.shiftTemplates = shiftTemplates;
        }

        public String getRecommendedNextAction() {
            return recommendedNextAction;
        }

        public void setRecommendedNextAction(String recommendedNextAction) {
            this.recommendedNextAction = recommendedNextAction;
        }
    }

    /**
     * 向导步骤输出。
     */
    public static class BootstrapStepOut {

        // 步骤编码用于前端稳定定位按钮动作。
        private String stepCode;
        // 步骤标题用于双语字典映射。
        private String titleKey;
        // 步骤状态用于渲染状态标签。
        private String status;
        // 当前统计数量用于补足完成依据。
        private Integer count;
        // 业务说明用于空状态提示。
        private String description;

        public String getStepCode() {
            return stepCode;
        }

        public void setStepCode(String stepCode) {
            this.stepCode = stepCode;
        }

        public String getTitleKey() {
            return titleKey;
        }

        public void setTitleKey(String titleKey) {
            this.titleKey = titleKey;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * 公司信息输出。
     */
    public static class TenantOut {

        private Long id;
        private String tenantCode;
        private String tenantName;
        private String status;
        private String contactName;
        private String contactPhone;
        private String contactEmail;
        private String timezone;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    /**
     * 事业所输出。
     */
    public static class WorkplaceOut {

        private Long id;
        private String workplaceCode;
        private String workplaceName;
        private String address;
        private String phone;
        private String status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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
     * 部门输出。
     */
    public static class DepartmentOut {

        private Long id;
        private Long workplaceId;
        private Long parentId;
        private String workplaceName;
        private String departmentCode;
        private String departmentName;
        private Integer sortOrder;
        private String status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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

        public String getWorkplaceName() {
            return workplaceName;
        }

        public void setWorkplaceName(String workplaceName) {
            this.workplaceName = workplaceName;
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
     * 员工输出。
     */
    public static class EmployeeOut {

        private Long id;
        private String employeeNo;
        private String employeeName;
        private String employeeNameKana;
        private String gender;
        private String employmentType;
        private LocalDate hireDate;
        private LocalDate resignDate;
        private String email;
        private String phone;
        private Long workplaceId;
        private Long departmentId;
        private String workplaceName;
        private String departmentName;
        private String status;
        private Boolean externalMappingBound;
        private String externalSourceSystem;
        private String externalEmployeeId;
        private String externalEmployeeNo;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Boolean getExternalMappingBound() {
            return externalMappingBound;
        }

        public void setExternalMappingBound(Boolean externalMappingBound) {
            this.externalMappingBound = externalMappingBound;
        }

        public String getExternalSourceSystem() {
            return externalSourceSystem;
        }

        public void setExternalSourceSystem(String externalSourceSystem) {
            this.externalSourceSystem = externalSourceSystem;
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
    }

    /**
     * 第七阶段规则工作台聚合输出。
     */
    public static class RuleWorkbenchOut {

        // 规则列表用于左侧规则清单。
        private List<RuleOut> rules;
        // 员工适用列表用于右侧查看谁正在使用哪条规则。
        private List<EmployeeRuleAssignmentOut> assignments;
        // 风险提醒列表用于第七阶段预警看板。
        private List<RuleAlertOut> alerts;
        // 汇总卡片用于规则区块头部快速展示当前风险数量。
        private RuleAlertSummaryOut summary;

        public List<RuleOut> getRules() {
            return rules;
        }

        public void setRules(List<RuleOut> rules) {
            this.rules = rules;
        }

        public List<EmployeeRuleAssignmentOut> getAssignments() {
            return assignments;
        }

        public void setAssignments(List<EmployeeRuleAssignmentOut> assignments) {
            this.assignments = assignments;
        }

        public List<RuleAlertOut> getAlerts() {
            return alerts;
        }

        public void setAlerts(List<RuleAlertOut> alerts) {
            this.alerts = alerts;
        }

        public RuleAlertSummaryOut getSummary() {
            return summary;
        }

        public void setSummary(RuleAlertSummaryOut summary) {
            this.summary = summary;
        }
    }

    /**
     * 第七阶段规则配置输出。
     */
    public static class RuleOut {

        // 规则主键用于前端编辑和员工适用指向。
        private Long id;
        // 规则编码用于主数据唯一识别。
        private String ruleCode;
        // 规则名称用于用户识别当前业务口径。
        private String ruleName;
        // 标准日工时用于解释残业基线。
        private Integer standardDailyMinutes;
        // 标准周工时用于解释周累计工时基线。
        private Integer standardWeeklyMinutes;
        // 自动休息开关用于决定页面是否展示自动扣休说明。
        private Boolean autoBreakEnabled;
        // 自动扣休起算阈值用于解释何时开始扣休。
        private Integer autoBreakThresholdMinutes;
        // 自动扣休分钟数用于规则详情展示。
        private Integer autoBreakDeductMinutes;
        // 深夜开始时间用于前端直接显示日本深夜时段。
        private String nightWorkStart;
        // 深夜结束时间用于前端直接显示跨日深夜时段。
        private String nightWorkEnd;
        // 取整粒度用于解释分钟口径。
        private Integer roundingUnitMinutes;
        // 取整方式用于解释规则是进位还是舍去。
        private String roundingMode;
        // 月残业预警阈值用于看板高风险判断。
        private Integer monthlyOvertimeAlertHours;
        // 年残业预警阈值用于累计风险判断。
        private Integer yearlyOvertimeAlertHours;
        // 有休提醒开关用于决定是否产生年度提醒。
        private Boolean paidLeaveReminderEnabled;
        // 是否启用决定该规则能否正式分配。
        private Boolean activeFlag;
        // 备注用于记录规则口径说明。
        private String note;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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
     * 第七阶段员工规则适用输出。
     */
    public static class EmployeeRuleAssignmentOut {

        // 员工主键用于回写适用规则。
        private Long employeeId;
        // 员工编号用于规则页快速检索。
        private String employeeNo;
        // 员工姓名用于管理员确认适用对象。
        private String employeeName;
        // 事业所名称用于规则适用范围解释。
        private String workplaceName;
        // 部门名称用于规则适用范围解释。
        private String departmentName;
        // 当前适用规则主键用于表单反填。
        private Long ruleId;
        // 当前适用规则名称用于列表展示。
        private String ruleName;
        // 规则生效开始日用于确认切换边界。
        private LocalDate effectiveStartDate;
        // 规则生效结束日用于确认临时规则结束边界。
        private LocalDate effectiveEndDate;
        // 适用备注用于管理员交接说明。
        private String note;
        // 月残业预警阈值用于按员工实际适用规则做月度判断。
        private Integer monthlyOvertimeAlertHours;
        // 年残业预警阈值用于按员工实际适用规则做年度判断。
        private Integer yearlyOvertimeAlertHours;
        // 有休提醒开关用于决定当前员工是否生成提醒。
        private Boolean paidLeaveReminderEnabled;
        // 当月残业分钟用于月度预警判断。
        private Integer monthlyOvertimeMinutes;
        // 年累计残业分钟用于年度预警判断。
        private Integer yearlyOvertimeMinutes;
        // 年累计有休日数用于提醒是否接近 5 日线。
        private Double yearlyPaidLeaveDays;

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

        public Long getRuleId() {
            return ruleId;
        }

        public void setRuleId(Long ruleId) {
            this.ruleId = ruleId;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
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

        public Integer getMonthlyOvertimeMinutes() {
            return monthlyOvertimeMinutes;
        }

        public void setMonthlyOvertimeMinutes(Integer monthlyOvertimeMinutes) {
            this.monthlyOvertimeMinutes = monthlyOvertimeMinutes;
        }

        public Integer getYearlyOvertimeMinutes() {
            return yearlyOvertimeMinutes;
        }

        public void setYearlyOvertimeMinutes(Integer yearlyOvertimeMinutes) {
            this.yearlyOvertimeMinutes = yearlyOvertimeMinutes;
        }

        public Double getYearlyPaidLeaveDays() {
            return yearlyPaidLeaveDays;
        }

        public void setYearlyPaidLeaveDays(Double yearlyPaidLeaveDays) {
            this.yearlyPaidLeaveDays = yearlyPaidLeaveDays;
        }
    }

    /**
     * 第七阶段规则预警输出。
     */
    public static class RuleAlertOut {

        // 员工信息用于提醒列表直接定位对象。
        private Long employeeId;
        private String employeeNo;
        private String employeeName;
        // 规则信息用于解释是按哪条规则触发风险。
        private Long ruleId;
        private String ruleName;
        // 告警类型用于前端区分月残业、年残业和有休提醒。
        private String alertType;
        // 告警级别用于列表排序和视觉强调。
        private String alertLevel;
        // 当前月份用于告诉用户这条提醒是按哪个月口径计算。
        private String yearMonth;
        // 当前分钟值用于分钟型预警统一展示。
        private Integer currentValueMinutes;
        // 阈值分钟用于分钟型预警统一比较。
        private Integer thresholdMinutes;
        // 当前天数用于有休提醒展示。
        private Double currentValueDays;
        // 阈值天数用于有休提醒展示。
        private Double thresholdDays;

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

        public Long getRuleId() {
            return ruleId;
        }

        public void setRuleId(Long ruleId) {
            this.ruleId = ruleId;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public String getAlertType() {
            return alertType;
        }

        public void setAlertType(String alertType) {
            this.alertType = alertType;
        }

        public String getAlertLevel() {
            return alertLevel;
        }

        public void setAlertLevel(String alertLevel) {
            this.alertLevel = alertLevel;
        }

        public String getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
        }

        public Integer getCurrentValueMinutes() {
            return currentValueMinutes;
        }

        public void setCurrentValueMinutes(Integer currentValueMinutes) {
            this.currentValueMinutes = currentValueMinutes;
        }

        public Integer getThresholdMinutes() {
            return thresholdMinutes;
        }

        public void setThresholdMinutes(Integer thresholdMinutes) {
            this.thresholdMinutes = thresholdMinutes;
        }

        public Double getCurrentValueDays() {
            return currentValueDays;
        }

        public void setCurrentValueDays(Double currentValueDays) {
            this.currentValueDays = currentValueDays;
        }

        public Double getThresholdDays() {
            return thresholdDays;
        }

        public void setThresholdDays(Double thresholdDays) {
            this.thresholdDays = thresholdDays;
        }
    }

    /**
     * 第七阶段规则预警汇总输出。
     */
    public static class RuleAlertSummaryOut {

        // 高风险数量用于月残业和年残业总览卡片。
        private Integer highRiskCount;
        // 提醒数量用于有休等轻提醒卡片。
        private Integer reminderCount;
        // 已绑定数量用于告诉管理员当前多少员工已挂正式规则。
        private Integer boundEmployeeCount;

        public Integer getHighRiskCount() {
            return highRiskCount;
        }

        public void setHighRiskCount(Integer highRiskCount) {
            this.highRiskCount = highRiskCount;
        }

        public Integer getReminderCount() {
            return reminderCount;
        }

        public void setReminderCount(Integer reminderCount) {
            this.reminderCount = reminderCount;
        }

        public Integer getBoundEmployeeCount() {
            return boundEmployeeCount;
        }

        public void setBoundEmployeeCount(Integer boundEmployeeCount) {
            this.boundEmployeeCount = boundEmployeeCount;
        }
    }

    /**
     * 班次模板输出。
     */
    public static class ShiftTemplateOut {

        private Long id;
        private String templateCode;
        private String templateName;
        private String shiftType;
        private String startTime;
        private String endTime;
        private Boolean crossDay;
        private Integer scheduledBreakMinutes;
        private String color;
        private Boolean active;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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

    /**
     * 员工导入结果输出。
     */
    public static class EmployeeImportResultOut {

        // 成功数用于前端展示本次导入净新增量。
        private Integer successCount;
        // 失败数用于提醒管理员处理脏数据。
        private Integer failedCount;
        // 失败明细用于定位哪一行导入失败。
        private List<String> errors;

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

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }

    /**
     * CSV 导出输出。
     */
    public static class CsvExportOut {

        // 文件名用于前端下载保存。
        private String fileName;
        // 文本内容用于前端直接组装 Blob 下载。
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
