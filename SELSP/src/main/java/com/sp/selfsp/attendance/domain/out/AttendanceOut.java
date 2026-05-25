package com.sp.selfsp.attendance.domain.out;

// LocalDate 用于返回员工日期字段和规则生效日。
import java.time.LocalDate;
// LocalDateTime 用于返回创建和更新时间。
import java.time.LocalDateTime;
// List 用于返回列表和批量处理结果。
import java.util.List;

/**
 * 考勤第一阶段输出对象集合。
 */
// 定义 考勤Out，承接当前文件对应的业务职责。
public final class AttendanceOut {

    // 定义 考勤Out 处理入口，承接当前业务动作。
    private AttendanceOut() {
    }

    /**
     * 初始化向导聚合输出。
     */
    // 定义 初始化聚合汇总Out，承接当前文件对应的业务职责。
    public static class BootstrapSummaryOut {

        // 当前租户用于页面头部直接展示公司信息。
        // 声明 租户 字段，用来承载当前业务对象的传输信息。
        private TenantOut tenant;
        // 向导步骤用于首屏提示当前准备进度。
        // 声明 steps 字段，用来承载当前业务对象的传输信息。
        private List<BootstrapStepOut> steps;
        // 事业所列表用于前端下拉和主数据卡片。
        // 声明 workplaces 字段，用来承载当前业务对象的传输信息。
        private List<WorkplaceOut> workplaces;
        // 部门列表用于前端下拉和部门区块。
        // 声明 departments 字段，用来承载当前业务对象的传输信息。
        private List<DepartmentOut> departments;
        // 员工列表用于首屏展示最近主数据状态。
        // 声明 employees 字段，用来承载当前业务对象的传输信息。
        private List<EmployeeOut> employees;
        // 班次模板列表用于首屏模板状态展示。
        // 声明 班次Templates 字段，用来承载当前业务对象的传输信息。
        private List<ShiftTemplateOut> shiftTemplates;
        // 下一步提示用于告诉管理员先处理什么。
        // 声明 推荐NextAction 字段，用来承载当前业务对象的传输信息。
        private String recommendedNextAction;

        // 对外返回 租户，供上下游继续读取当前业务字段。
        public TenantOut getTenant() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return tenant;
        }

        // 回填 租户，让请求绑定或结果组装保存当前字段值。
        public void setTenant(TenantOut tenant) {
            // 把外部传入结果写入 租户 字段，供后续流程继续使用。
            this.tenant = tenant;
        }

        // 对外返回 Steps，供上下游继续读取当前业务字段。
        public List<BootstrapStepOut> getSteps() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return steps;
        }

        // 回填 Steps，让请求绑定或结果组装保存当前字段值。
        public void setSteps(List<BootstrapStepOut> steps) {
            // 把外部传入结果写入 steps 字段，供后续流程继续使用。
            this.steps = steps;
        }

        // 对外返回 Workplaces，供上下游继续读取当前业务字段。
        public List<WorkplaceOut> getWorkplaces() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return workplaces;
        }

        // 回填 Workplaces，让请求绑定或结果组装保存当前字段值。
        public void setWorkplaces(List<WorkplaceOut> workplaces) {
            // 把外部传入结果写入 workplaces 字段，供后续流程继续使用。
            this.workplaces = workplaces;
        }

        // 对外返回 Departments，供上下游继续读取当前业务字段。
        public List<DepartmentOut> getDepartments() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return departments;
        }

        // 回填 Departments，让请求绑定或结果组装保存当前字段值。
        public void setDepartments(List<DepartmentOut> departments) {
            // 把外部传入结果写入 departments 字段，供后续流程继续使用。
            this.departments = departments;
        }

        // 对外返回 Employees，供上下游继续读取当前业务字段。
        public List<EmployeeOut> getEmployees() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return employees;
        }

        // 回填 Employees，让请求绑定或结果组装保存当前字段值。
        public void setEmployees(List<EmployeeOut> employees) {
            // 把外部传入结果写入 employees 字段，供后续流程继续使用。
            this.employees = employees;
        }

        // 对外返回 班次Templates，供上下游继续读取当前业务字段。
        public List<ShiftTemplateOut> getShiftTemplates() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return shiftTemplates;
        }

        // 回填 班次Templates，让请求绑定或结果组装保存当前字段值。
        public void setShiftTemplates(List<ShiftTemplateOut> shiftTemplates) {
            // 把外部传入结果写入 班次Templates 字段，供后续流程继续使用。
            this.shiftTemplates = shiftTemplates;
        }

        // 对外返回 推荐NextAction，供上下游继续读取当前业务字段。
        public String getRecommendedNextAction() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return recommendedNextAction;
        }

        // 回填 推荐NextAction，让请求绑定或结果组装保存当前字段值。
        public void setRecommendedNextAction(String recommendedNextAction) {
            // 把外部传入结果写入 推荐NextAction 字段，供后续流程继续使用。
            this.recommendedNextAction = recommendedNextAction;
        }
    }

    /**
     * 向导步骤输出。
     */
    // 定义 初始化聚合步骤Out，承接当前文件对应的业务职责。
    public static class BootstrapStepOut {

        // 步骤编码用于前端稳定定位按钮动作。
        // 声明 步骤编码 字段，用来承载当前业务对象的传输信息。
        private String stepCode;
        // 步骤标题用于双语字典映射。
        // 声明 titleKey 字段，用来承载当前业务对象的传输信息。
        private String titleKey;
        // 步骤状态用于渲染状态标签。
        // 声明 状态 字段，用来承载当前业务对象的传输信息。
        private String status;
        // 当前统计数量用于补足完成依据。
        // 声明 数量 字段，用来承载当前业务对象的传输信息。
        private Integer count;
        // 业务说明用于空状态提示。
        // 声明 description 字段，用来承载当前业务对象的传输信息。
        private String description;

        // 对外返回 步骤编码，供上下游继续读取当前业务字段。
        public String getStepCode() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return stepCode;
        }

        // 回填 步骤编码，让请求绑定或结果组装保存当前字段值。
        public void setStepCode(String stepCode) {
            // 把外部传入结果写入 步骤编码 字段，供后续流程继续使用。
            this.stepCode = stepCode;
        }

        // 对外返回 TitleKey，供上下游继续读取当前业务字段。
        public String getTitleKey() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return titleKey;
        }

        // 回填 TitleKey，让请求绑定或结果组装保存当前字段值。
        public void setTitleKey(String titleKey) {
            // 把外部传入结果写入 titleKey 字段，供后续流程继续使用。
            this.titleKey = titleKey;
        }

        // 对外返回 状态，供上下游继续读取当前业务字段。
        public String getStatus() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return status;
        }

        // 回填 状态，让请求绑定或结果组装保存当前字段值。
        public void setStatus(String status) {
            // 把外部传入结果写入 状态 字段，供后续流程继续使用。
            this.status = status;
        }

        // 对外返回 数量，供上下游继续读取当前业务字段。
        public Integer getCount() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return count;
        }

        // 回填 数量，让请求绑定或结果组装保存当前字段值。
        public void setCount(Integer count) {
            // 把外部传入结果写入 数量 字段，供后续流程继续使用。
            this.count = count;
        }

        // 对外返回 Description，供上下游继续读取当前业务字段。
        public String getDescription() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return description;
        }

        // 回填 Description，让请求绑定或结果组装保存当前字段值。
        public void setDescription(String description) {
            // 把外部传入结果写入 description 字段，供后续流程继续使用。
            this.description = description;
        }
    }

    /**
     * 公司信息输出。
     */
    // 定义 租户Out，承接当前文件对应的业务职责。
    public static class TenantOut {

        // 声明 id 字段，用来承载当前业务对象的传输信息。
        private Long id;
        // 声明 租户编码 字段，用来承载当前业务对象的传输信息。
        private String tenantCode;
        // 声明 租户名称 字段，用来承载当前业务对象的传输信息。
        private String tenantName;
        // 声明 状态 字段，用来承载当前业务对象的传输信息。
        private String status;
        // 声明 contact名称 字段，用来承载当前业务对象的传输信息。
        private String contactName;
        // 声明 contact电话 字段，用来承载当前业务对象的传输信息。
        private String contactPhone;
        // 声明 contact邮箱 字段，用来承载当前业务对象的传输信息。
        private String contactEmail;
        // 声明 时区 字段，用来承载当前业务对象的传输信息。
        private String timezone;
        // 声明 createdAt 字段，用来承载当前业务对象的传输信息。
        private LocalDateTime createdAt;
        // 声明 updatedAt 字段，用来承载当前业务对象的传输信息。
        private LocalDateTime updatedAt;

        // 对外返回 Id，供上下游继续读取当前业务字段。
        public Long getId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return id;
        }

        // 回填 Id，让请求绑定或结果组装保存当前字段值。
        public void setId(Long id) {
            // 把外部传入结果写入 id 字段，供后续流程继续使用。
            this.id = id;
        }

        // 对外返回 租户编码，供上下游继续读取当前业务字段。
        public String getTenantCode() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return tenantCode;
        }

        // 回填 租户编码，让请求绑定或结果组装保存当前字段值。
        public void setTenantCode(String tenantCode) {
            // 把外部传入结果写入 租户编码 字段，供后续流程继续使用。
            this.tenantCode = tenantCode;
        }

        // 对外返回 租户名称，供上下游继续读取当前业务字段。
        public String getTenantName() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return tenantName;
        }

        // 回填 租户名称，让请求绑定或结果组装保存当前字段值。
        public void setTenantName(String tenantName) {
            // 把外部传入结果写入 租户名称 字段，供后续流程继续使用。
            this.tenantName = tenantName;
        }

        // 对外返回 状态，供上下游继续读取当前业务字段。
        public String getStatus() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return status;
        }

        // 回填 状态，让请求绑定或结果组装保存当前字段值。
        public void setStatus(String status) {
            // 把外部传入结果写入 状态 字段，供后续流程继续使用。
            this.status = status;
        }

        // 对外返回 Contact名称，供上下游继续读取当前业务字段。
        public String getContactName() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return contactName;
        }

        // 回填 Contact名称，让请求绑定或结果组装保存当前字段值。
        public void setContactName(String contactName) {
            // 把外部传入结果写入 contact名称 字段，供后续流程继续使用。
            this.contactName = contactName;
        }

        // 对外返回 Contact电话，供上下游继续读取当前业务字段。
        public String getContactPhone() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return contactPhone;
        }

        // 回填 Contact电话，让请求绑定或结果组装保存当前字段值。
        public void setContactPhone(String contactPhone) {
            // 把外部传入结果写入 contact电话 字段，供后续流程继续使用。
            this.contactPhone = contactPhone;
        }

        // 对外返回 Contact邮箱，供上下游继续读取当前业务字段。
        public String getContactEmail() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return contactEmail;
        }

        // 回填 Contact邮箱，让请求绑定或结果组装保存当前字段值。
        public void setContactEmail(String contactEmail) {
            // 把外部传入结果写入 contact邮箱 字段，供后续流程继续使用。
            this.contactEmail = contactEmail;
        }

        // 对外返回 时区，供上下游继续读取当前业务字段。
        public String getTimezone() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return timezone;
        }

        // 回填 时区，让请求绑定或结果组装保存当前字段值。
        public void setTimezone(String timezone) {
            // 把外部传入结果写入 时区 字段，供后续流程继续使用。
            this.timezone = timezone;
        }

        // 对外返回 CreatedAt，供上下游继续读取当前业务字段。
        public LocalDateTime getCreatedAt() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return createdAt;
        }

        // 回填 CreatedAt，让请求绑定或结果组装保存当前字段值。
        public void setCreatedAt(LocalDateTime createdAt) {
            // 把外部传入结果写入 createdAt 字段，供后续流程继续使用。
            this.createdAt = createdAt;
        }

        // 对外返回 UpdatedAt，供上下游继续读取当前业务字段。
        public LocalDateTime getUpdatedAt() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return updatedAt;
        }

        // 回填 UpdatedAt，让请求绑定或结果组装保存当前字段值。
        public void setUpdatedAt(LocalDateTime updatedAt) {
            // 把外部传入结果写入 updatedAt 字段，供后续流程继续使用。
            this.updatedAt = updatedAt;
        }
    }

    /**
     * 事业所输出。
     */
    // 定义 事业所Out，承接当前文件对应的业务职责。
    public static class WorkplaceOut {

        // 声明 id 字段，用来承载当前业务对象的传输信息。
        private Long id;
        // 声明 事业所编码 字段，用来承载当前业务对象的传输信息。
        private String workplaceCode;
        // 声明 事业所名称 字段，用来承载当前业务对象的传输信息。
        private String workplaceName;
        // 声明 地址 字段，用来承载当前业务对象的传输信息。
        private String address;
        // 声明 电话 字段，用来承载当前业务对象的传输信息。
        private String phone;
        // 声明 状态 字段，用来承载当前业务对象的传输信息。
        private String status;

        // 对外返回 Id，供上下游继续读取当前业务字段。
        public Long getId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return id;
        }

        // 回填 Id，让请求绑定或结果组装保存当前字段值。
        public void setId(Long id) {
            // 把外部传入结果写入 id 字段，供后续流程继续使用。
            this.id = id;
        }

        // 对外返回 事业所编码，供上下游继续读取当前业务字段。
        public String getWorkplaceCode() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return workplaceCode;
        }

        // 回填 事业所编码，让请求绑定或结果组装保存当前字段值。
        public void setWorkplaceCode(String workplaceCode) {
            // 把外部传入结果写入 事业所编码 字段，供后续流程继续使用。
            this.workplaceCode = workplaceCode;
        }

        // 对外返回 事业所名称，供上下游继续读取当前业务字段。
        public String getWorkplaceName() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return workplaceName;
        }

        // 回填 事业所名称，让请求绑定或结果组装保存当前字段值。
        public void setWorkplaceName(String workplaceName) {
            // 把外部传入结果写入 事业所名称 字段，供后续流程继续使用。
            this.workplaceName = workplaceName;
        }

        // 对外返回 地址，供上下游继续读取当前业务字段。
        public String getAddress() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return address;
        }

        // 回填 地址，让请求绑定或结果组装保存当前字段值。
        public void setAddress(String address) {
            // 把外部传入结果写入 地址 字段，供后续流程继续使用。
            this.address = address;
        }

        // 对外返回 电话，供上下游继续读取当前业务字段。
        public String getPhone() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return phone;
        }

        // 回填 电话，让请求绑定或结果组装保存当前字段值。
        public void setPhone(String phone) {
            // 把外部传入结果写入 电话 字段，供后续流程继续使用。
            this.phone = phone;
        }

        // 对外返回 状态，供上下游继续读取当前业务字段。
        public String getStatus() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return status;
        }

        // 回填 状态，让请求绑定或结果组装保存当前字段值。
        public void setStatus(String status) {
            // 把外部传入结果写入 状态 字段，供后续流程继续使用。
            this.status = status;
        }
    }

    /**
     * 部门输出。
     */
    // 定义 部门Out，承接当前文件对应的业务职责。
    public static class DepartmentOut {

        // 声明 id 字段，用来承载当前业务对象的传输信息。
        private Long id;
        // 声明 事业所Id 字段，用来承载当前业务对象的传输信息。
        private Long workplaceId;
        // 声明 parentId 字段，用来承载当前业务对象的传输信息。
        private Long parentId;
        // 声明 事业所名称 字段，用来承载当前业务对象的传输信息。
        private String workplaceName;
        // 声明 部门编码 字段，用来承载当前业务对象的传输信息。
        private String departmentCode;
        // 声明 部门名称 字段，用来承载当前业务对象的传输信息。
        private String departmentName;
        // 声明 sortOrder 字段，用来承载当前业务对象的传输信息。
        private Integer sortOrder;
        // 声明 状态 字段，用来承载当前业务对象的传输信息。
        private String status;

        // 对外返回 Id，供上下游继续读取当前业务字段。
        public Long getId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return id;
        }

        // 回填 Id，让请求绑定或结果组装保存当前字段值。
        public void setId(Long id) {
            // 把外部传入结果写入 id 字段，供后续流程继续使用。
            this.id = id;
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

        // 对外返回 ParentId，供上下游继续读取当前业务字段。
        public Long getParentId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return parentId;
        }

        // 回填 ParentId，让请求绑定或结果组装保存当前字段值。
        public void setParentId(Long parentId) {
            // 把外部传入结果写入 parentId 字段，供后续流程继续使用。
            this.parentId = parentId;
        }

        // 对外返回 事业所名称，供上下游继续读取当前业务字段。
        public String getWorkplaceName() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return workplaceName;
        }

        // 回填 事业所名称，让请求绑定或结果组装保存当前字段值。
        public void setWorkplaceName(String workplaceName) {
            // 把外部传入结果写入 事业所名称 字段，供后续流程继续使用。
            this.workplaceName = workplaceName;
        }

        // 对外返回 部门编码，供上下游继续读取当前业务字段。
        public String getDepartmentCode() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return departmentCode;
        }

        // 回填 部门编码，让请求绑定或结果组装保存当前字段值。
        public void setDepartmentCode(String departmentCode) {
            // 把外部传入结果写入 部门编码 字段，供后续流程继续使用。
            this.departmentCode = departmentCode;
        }

        // 对外返回 部门名称，供上下游继续读取当前业务字段。
        public String getDepartmentName() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return departmentName;
        }

        // 回填 部门名称，让请求绑定或结果组装保存当前字段值。
        public void setDepartmentName(String departmentName) {
            // 把外部传入结果写入 部门名称 字段，供后续流程继续使用。
            this.departmentName = departmentName;
        }

        // 对外返回 SortOrder，供上下游继续读取当前业务字段。
        public Integer getSortOrder() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return sortOrder;
        }

        // 回填 SortOrder，让请求绑定或结果组装保存当前字段值。
        public void setSortOrder(Integer sortOrder) {
            // 把外部传入结果写入 sortOrder 字段，供后续流程继续使用。
            this.sortOrder = sortOrder;
        }

        // 对外返回 状态，供上下游继续读取当前业务字段。
        public String getStatus() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return status;
        }

        // 回填 状态，让请求绑定或结果组装保存当前字段值。
        public void setStatus(String status) {
            // 把外部传入结果写入 状态 字段，供后续流程继续使用。
            this.status = status;
        }
    }

    /**
     * 员工输出。
     */
    // 定义 员工Out，承接当前文件对应的业务职责。
    public static class EmployeeOut {

        // 声明 id 字段，用来承载当前业务对象的传输信息。
        private Long id;
        // 声明 员工No 字段，用来承载当前业务对象的传输信息。
        private String employeeNo;
        // 声明 员工名称 字段，用来承载当前业务对象的传输信息。
        private String employeeName;
        // 声明 员工名称Kana 字段，用来承载当前业务对象的传输信息。
        private String employeeNameKana;
        // 声明 gender 字段，用来承载当前业务对象的传输信息。
        private String gender;
        // 声明 employmentType 字段，用来承载当前业务对象的传输信息。
        private String employmentType;
        // 声明 入社Date 字段，用来承载当前业务对象的传输信息。
        private LocalDate hireDate;
        // 声明 退社Date 字段，用来承载当前业务对象的传输信息。
        private LocalDate resignDate;
        // 声明 邮箱 字段，用来承载当前业务对象的传输信息。
        private String email;
        // 声明 电话 字段，用来承载当前业务对象的传输信息。
        private String phone;
        // 声明 事业所Id 字段，用来承载当前业务对象的传输信息。
        private Long workplaceId;
        // 声明 部门Id 字段，用来承载当前业务对象的传输信息。
        private Long departmentId;
        // 声明 事业所名称 字段，用来承载当前业务对象的传输信息。
        private String workplaceName;
        // 声明 部门名称 字段，用来承载当前业务对象的传输信息。
        private String departmentName;
        // 声明 状态 字段，用来承载当前业务对象的传输信息。
        private String status;
        // 声明 外部系统映射Bound 字段，用来承载当前业务对象的传输信息。
        private Boolean externalMappingBound;
        // 声明 外部系统SourceSystem 字段，用来承载当前业务对象的传输信息。
        private String externalSourceSystem;
        // 声明 外部系统员工Id 字段，用来承载当前业务对象的传输信息。
        private String externalEmployeeId;
        // 声明 外部系统员工No 字段，用来承载当前业务对象的传输信息。
        private String externalEmployeeNo;

        // 对外返回 Id，供上下游继续读取当前业务字段。
        public Long getId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return id;
        }

        // 回填 Id，让请求绑定或结果组装保存当前字段值。
        public void setId(Long id) {
            // 把外部传入结果写入 id 字段，供后续流程继续使用。
            this.id = id;
        }

        // 对外返回 员工No，供上下游继续读取当前业务字段。
        public String getEmployeeNo() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return employeeNo;
        }

        // 回填 员工No，让请求绑定或结果组装保存当前字段值。
        public void setEmployeeNo(String employeeNo) {
            // 把外部传入结果写入 员工No 字段，供后续流程继续使用。
            this.employeeNo = employeeNo;
        }

        // 对外返回 员工名称，供上下游继续读取当前业务字段。
        public String getEmployeeName() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return employeeName;
        }

        // 回填 员工名称，让请求绑定或结果组装保存当前字段值。
        public void setEmployeeName(String employeeName) {
            // 把外部传入结果写入 员工名称 字段，供后续流程继续使用。
            this.employeeName = employeeName;
        }

        // 对外返回 员工名称Kana，供上下游继续读取当前业务字段。
        public String getEmployeeNameKana() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return employeeNameKana;
        }

        // 回填 员工名称Kana，让请求绑定或结果组装保存当前字段值。
        public void setEmployeeNameKana(String employeeNameKana) {
            // 把外部传入结果写入 员工名称Kana 字段，供后续流程继续使用。
            this.employeeNameKana = employeeNameKana;
        }

        // 对外返回 Gender，供上下游继续读取当前业务字段。
        public String getGender() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return gender;
        }

        // 回填 Gender，让请求绑定或结果组装保存当前字段值。
        public void setGender(String gender) {
            // 把外部传入结果写入 gender 字段，供后续流程继续使用。
            this.gender = gender;
        }

        // 对外返回 EmploymentType，供上下游继续读取当前业务字段。
        public String getEmploymentType() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return employmentType;
        }

        // 回填 EmploymentType，让请求绑定或结果组装保存当前字段值。
        public void setEmploymentType(String employmentType) {
            // 把外部传入结果写入 employmentType 字段，供后续流程继续使用。
            this.employmentType = employmentType;
        }

        // 对外返回 入社Date，供上下游继续读取当前业务字段。
        public LocalDate getHireDate() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return hireDate;
        }

        // 回填 入社Date，让请求绑定或结果组装保存当前字段值。
        public void setHireDate(LocalDate hireDate) {
            // 把外部传入结果写入 入社Date 字段，供后续流程继续使用。
            this.hireDate = hireDate;
        }

        // 对外返回 退社Date，供上下游继续读取当前业务字段。
        public LocalDate getResignDate() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return resignDate;
        }

        // 回填 退社Date，让请求绑定或结果组装保存当前字段值。
        public void setResignDate(LocalDate resignDate) {
            // 把外部传入结果写入 退社Date 字段，供后续流程继续使用。
            this.resignDate = resignDate;
        }

        // 对外返回 邮箱，供上下游继续读取当前业务字段。
        public String getEmail() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return email;
        }

        // 回填 邮箱，让请求绑定或结果组装保存当前字段值。
        public void setEmail(String email) {
            // 把外部传入结果写入 邮箱 字段，供后续流程继续使用。
            this.email = email;
        }

        // 对外返回 电话，供上下游继续读取当前业务字段。
        public String getPhone() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return phone;
        }

        // 回填 电话，让请求绑定或结果组装保存当前字段值。
        public void setPhone(String phone) {
            // 把外部传入结果写入 电话 字段，供后续流程继续使用。
            this.phone = phone;
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

        // 对外返回 事业所名称，供上下游继续读取当前业务字段。
        public String getWorkplaceName() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return workplaceName;
        }

        // 回填 事业所名称，让请求绑定或结果组装保存当前字段值。
        public void setWorkplaceName(String workplaceName) {
            // 把外部传入结果写入 事业所名称 字段，供后续流程继续使用。
            this.workplaceName = workplaceName;
        }

        // 对外返回 部门名称，供上下游继续读取当前业务字段。
        public String getDepartmentName() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return departmentName;
        }

        // 回填 部门名称，让请求绑定或结果组装保存当前字段值。
        public void setDepartmentName(String departmentName) {
            // 把外部传入结果写入 部门名称 字段，供后续流程继续使用。
            this.departmentName = departmentName;
        }

        // 对外返回 状态，供上下游继续读取当前业务字段。
        public String getStatus() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return status;
        }

        // 回填 状态，让请求绑定或结果组装保存当前字段值。
        public void setStatus(String status) {
            // 把外部传入结果写入 状态 字段，供后续流程继续使用。
            this.status = status;
        }

        // 对外返回 外部系统映射Bound，供上下游继续读取当前业务字段。
        public Boolean getExternalMappingBound() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return externalMappingBound;
        }

        // 回填 外部系统映射Bound，让请求绑定或结果组装保存当前字段值。
        public void setExternalMappingBound(Boolean externalMappingBound) {
            // 把外部传入结果写入 外部系统映射Bound 字段，供后续流程继续使用。
            this.externalMappingBound = externalMappingBound;
        }

        // 对外返回 外部系统SourceSystem，供上下游继续读取当前业务字段。
        public String getExternalSourceSystem() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return externalSourceSystem;
        }

        // 回填 外部系统SourceSystem，让请求绑定或结果组装保存当前字段值。
        public void setExternalSourceSystem(String externalSourceSystem) {
            // 把外部传入结果写入 外部系统SourceSystem 字段，供后续流程继续使用。
            this.externalSourceSystem = externalSourceSystem;
        }

        // 对外返回 外部系统员工Id，供上下游继续读取当前业务字段。
        public String getExternalEmployeeId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return externalEmployeeId;
        }

        // 回填 外部系统员工Id，让请求绑定或结果组装保存当前字段值。
        public void setExternalEmployeeId(String externalEmployeeId) {
            // 把外部传入结果写入 外部系统员工Id 字段，供后续流程继续使用。
            this.externalEmployeeId = externalEmployeeId;
        }

        // 对外返回 外部系统员工No，供上下游继续读取当前业务字段。
        public String getExternalEmployeeNo() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return externalEmployeeNo;
        }

        // 回填 外部系统员工No，让请求绑定或结果组装保存当前字段值。
        public void setExternalEmployeeNo(String externalEmployeeNo) {
            // 把外部传入结果写入 外部系统员工No 字段，供后续流程继续使用。
            this.externalEmployeeNo = externalEmployeeNo;
        }
    }

    /**
     * 班次模板输出。
     */
    // 定义 班次模板Out，承接当前文件对应的业务职责。
    public static class ShiftTemplateOut {

        // 声明 id 字段，用来承载当前业务对象的传输信息。
        private Long id;
        // 声明 模板编码 字段，用来承载当前业务对象的传输信息。
        private String templateCode;
        // 声明 模板名称 字段，用来承载当前业务对象的传输信息。
        private String templateName;
        // 声明 班次Type 字段，用来承载当前业务对象的传输信息。
        private String shiftType;
        // 声明 startTime 字段，用来承载当前业务对象的传输信息。
        private String startTime;
        // 声明 endTime 字段，用来承载当前业务对象的传输信息。
        private String endTime;
        // 声明 crossDay 字段，用来承载当前业务对象的传输信息。
        private Boolean crossDay;
        // 声明 scheduledBreakMinutes 字段，用来承载当前业务对象的传输信息。
        private Integer scheduledBreakMinutes;
        // 声明 颜色 字段，用来承载当前业务对象的传输信息。
        private String color;
        // 声明 启用标记 字段，用来承载当前业务对象的传输信息。
        private Boolean active;

        // 对外返回 Id，供上下游继续读取当前业务字段。
        public Long getId() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return id;
        }

        // 回填 Id，让请求绑定或结果组装保存当前字段值。
        public void setId(Long id) {
            // 把外部传入结果写入 id 字段，供后续流程继续使用。
            this.id = id;
        }

        // 对外返回 模板编码，供上下游继续读取当前业务字段。
        public String getTemplateCode() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return templateCode;
        }

        // 回填 模板编码，让请求绑定或结果组装保存当前字段值。
        public void setTemplateCode(String templateCode) {
            // 把外部传入结果写入 模板编码 字段，供后续流程继续使用。
            this.templateCode = templateCode;
        }

        // 对外返回 模板名称，供上下游继续读取当前业务字段。
        public String getTemplateName() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return templateName;
        }

        // 回填 模板名称，让请求绑定或结果组装保存当前字段值。
        public void setTemplateName(String templateName) {
            // 把外部传入结果写入 模板名称 字段，供后续流程继续使用。
            this.templateName = templateName;
        }

        // 对外返回 班次Type，供上下游继续读取当前业务字段。
        public String getShiftType() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return shiftType;
        }

        // 回填 班次Type，让请求绑定或结果组装保存当前字段值。
        public void setShiftType(String shiftType) {
            // 把外部传入结果写入 班次Type 字段，供后续流程继续使用。
            this.shiftType = shiftType;
        }

        // 对外返回 StartTime，供上下游继续读取当前业务字段。
        public String getStartTime() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return startTime;
        }

        // 回填 StartTime，让请求绑定或结果组装保存当前字段值。
        public void setStartTime(String startTime) {
            // 把外部传入结果写入 startTime 字段，供后续流程继续使用。
            this.startTime = startTime;
        }

        // 对外返回 EndTime，供上下游继续读取当前业务字段。
        public String getEndTime() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return endTime;
        }

        // 回填 EndTime，让请求绑定或结果组装保存当前字段值。
        public void setEndTime(String endTime) {
            // 把外部传入结果写入 endTime 字段，供后续流程继续使用。
            this.endTime = endTime;
        }

        // 对外返回 CrossDay，供上下游继续读取当前业务字段。
        public Boolean getCrossDay() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return crossDay;
        }

        // 回填 CrossDay，让请求绑定或结果组装保存当前字段值。
        public void setCrossDay(Boolean crossDay) {
            // 把外部传入结果写入 crossDay 字段，供后续流程继续使用。
            this.crossDay = crossDay;
        }

        // 对外返回 ScheduledBreakMinutes，供上下游继续读取当前业务字段。
        public Integer getScheduledBreakMinutes() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return scheduledBreakMinutes;
        }

        // 回填 ScheduledBreakMinutes，让请求绑定或结果组装保存当前字段值。
        public void setScheduledBreakMinutes(Integer scheduledBreakMinutes) {
            // 把外部传入结果写入 scheduledBreakMinutes 字段，供后续流程继续使用。
            this.scheduledBreakMinutes = scheduledBreakMinutes;
        }

        // 对外返回 颜色，供上下游继续读取当前业务字段。
        public String getColor() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return color;
        }

        // 回填 颜色，让请求绑定或结果组装保存当前字段值。
        public void setColor(String color) {
            // 把外部传入结果写入 颜色 字段，供后续流程继续使用。
            this.color = color;
        }

        // 对外返回 启用标记，供上下游继续读取当前业务字段。
        public Boolean getActive() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return active;
        }

        // 回填 启用标记，让请求绑定或结果组装保存当前字段值。
        public void setActive(Boolean active) {
            // 把外部传入结果写入 启用标记 字段，供后续流程继续使用。
            this.active = active;
        }
    }

    /**
     * 员工导入结果输出。
     */
    // 定义 员工导入ResultOut，承接当前文件对应的业务职责。
    public static class EmployeeImportResultOut {

        // 成功数用于前端展示本次导入净新增量。
        // 声明 success数量 字段，用来承载当前业务对象的传输信息。
        private Integer successCount;
        // 失败数用于提醒管理员处理脏数据。
        // 声明 failed数量 字段，用来承载当前业务对象的传输信息。
        private Integer failedCount;
        // 失败明细用于定位哪一行导入失败。
        // 声明 errors 字段，用来承载当前业务对象的传输信息。
        private List<String> errors;

        // 对外返回 Success数量，供上下游继续读取当前业务字段。
        public Integer getSuccessCount() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return successCount;
        }

        // 回填 Success数量，让请求绑定或结果组装保存当前字段值。
        public void setSuccessCount(Integer successCount) {
            // 把外部传入结果写入 success数量 字段，供后续流程继续使用。
            this.successCount = successCount;
        }

        // 对外返回 Failed数量，供上下游继续读取当前业务字段。
        public Integer getFailedCount() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return failedCount;
        }

        // 回填 Failed数量，让请求绑定或结果组装保存当前字段值。
        public void setFailedCount(Integer failedCount) {
            // 把外部传入结果写入 failed数量 字段，供后续流程继续使用。
            this.failedCount = failedCount;
        }

        // 对外返回 Errors，供上下游继续读取当前业务字段。
        public List<String> getErrors() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return errors;
        }

        // 回填 Errors，让请求绑定或结果组装保存当前字段值。
        public void setErrors(List<String> errors) {
            // 把外部传入结果写入 errors 字段，供后续流程继续使用。
            this.errors = errors;
        }
    }

    /**
     * CSV 导出输出。
     */
    // 定义 Csv导出Out，承接当前文件对应的业务职责。
    public static class CsvExportOut {

        // 文件名用于前端下载保存。
        // 声明 file名称 字段，用来承载当前业务对象的传输信息。
        private String fileName;
        // 文本内容用于前端直接组装 Blob 下载。
        // 声明 content 字段，用来承载当前业务对象的传输信息。
        private String content;

        // 对外返回 File名称，供上下游继续读取当前业务字段。
        public String getFileName() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return fileName;
        }

        // 回填 File名称，让请求绑定或结果组装保存当前字段值。
        public void setFileName(String fileName) {
            // 把外部传入结果写入 file名称 字段，供后续流程继续使用。
            this.fileName = fileName;
        }

        // 对外返回 Content，供上下游继续读取当前业务字段。
        public String getContent() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return content;
        }

        // 回填 Content，让请求绑定或结果组装保存当前字段值。
        public void setContent(String content) {
            // 把外部传入结果写入 content 字段，供后续流程继续使用。
            this.content = content;
        }
    }
}
