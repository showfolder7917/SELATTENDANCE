package com.sp.selfsp.attendance.domain.in;

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
