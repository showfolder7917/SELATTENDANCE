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
