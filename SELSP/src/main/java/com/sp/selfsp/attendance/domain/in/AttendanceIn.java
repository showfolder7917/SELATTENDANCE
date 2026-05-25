package com.sp.selfsp.attendance.domain.in;

// LocalDate 用于承接员工入社日和规则生效日这类日期字段。
import java.time.LocalDate;

/**
 * 考勤第一阶段输入对象集合。
 */
// 定义 考勤In，承接当前文件对应的业务职责。
public final class AttendanceIn {

    // 定义 考勤In 处理入口，承接当前业务动作。
    private AttendanceIn() {
    }

    /**
     * 公司 / 教室信息保存入参。
     */
    // 定义 租户保存In，承接当前文件对应的业务职责。
    public static class TenantSaveIn {

        // 租户编码用于第一阶段唯一标识当前公司或教室。
        // 声明 租户编码 字段，用来承载当前业务对象的传输信息。
        private String tenantCode;
        // 租户名称用于页面标题和导出抬头展示。
        // 声明 租户名称 字段，用来承载当前业务对象的传输信息。
        private String tenantName;
        // 联系人用于初始化向导确认基础信息是否完整。
        // 声明 contact名称 字段，用来承载当前业务对象的传输信息。
        private String contactName;
        // 联系电话用于后台运营联系。
        // 声明 contact电话 字段，用来承载当前业务对象的传输信息。
        private String contactPhone;
        // 联系邮箱用于后续通知和资料追踪。
        // 声明 contact邮箱 字段，用来承载当前业务对象的传输信息。
        private String contactEmail;
        // 时区用于后续日本勤怠计算边界。
        // 声明 时区 字段，用来承载当前业务对象的传输信息。
        private String timezone;

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
    }

    /**
     * 事业所保存入参。
     */
    // 定义 事业所保存In，承接当前文件对应的业务职责。
    public static class WorkplaceSaveIn {

        // 事业所编码用于员工导入按编码关联主数据。
        // 声明 事业所编码 字段，用来承载当前业务对象的传输信息。
        private String workplaceCode;
        // 事业所名称用于员工归属和向导展示。
        // 声明 事业所名称 字段，用来承载当前业务对象的传输信息。
        private String workplaceName;
        // 地址用于基础资料完整性展示。
        // 声明 地址 字段，用来承载当前业务对象的传输信息。
        private String address;
        // 电话用于日常联系。
        // 声明 电话 字段，用来承载当前业务对象的传输信息。
        private String phone;
        // 状态用于决定事业所是否参与后续业务。
        // 声明 状态 字段，用来承载当前业务对象的传输信息。
        private String status;

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
     * 部门保存入参。
     */
    // 定义 部门保存In，承接当前文件对应的业务职责。
    public static class DepartmentSaveIn {

        // 事业所主键用于限定部门归属地点。
        // 声明 事业所Id 字段，用来承载当前业务对象的传输信息。
        private Long workplaceId;
        // 父部门主键用于预留层级组织结构。
        // 声明 parentId 字段，用来承载当前业务对象的传输信息。
        private Long parentId;
        // 部门编码用于员工导入时做部门匹配。
        // 声明 部门编码 字段，用来承载当前业务对象的传输信息。
        private String departmentCode;
        // 部门名称用于列表展示和筛选。
        // 声明 部门名称 字段，用来承载当前业务对象的传输信息。
        private String departmentName;
        // 排序号用于控制列表稳定顺序。
        // 声明 sortOrder 字段，用来承载当前业务对象的传输信息。
        private Integer sortOrder;
        // 状态用于标记启用或停用。
        // 声明 状态 字段，用来承载当前业务对象的传输信息。
        private String status;

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
     * 员工保存入参。
     */
    // 定义 员工保存In，承接当前文件对应的业务职责。
    public static class EmployeeSaveIn {

        // 员工编号用于主数据唯一标识。
        // 声明 员工No 字段，用来承载当前业务对象的传输信息。
        private String employeeNo;
        // 员工姓名用于列表、导出和详情展示。
        // 声明 员工名称 字段，用来承载当前业务对象的传输信息。
        private String employeeName;
        // 假名用于日本场景检索和展示。
        // 声明 员工名称Kana 字段，用来承载当前业务对象的传输信息。
        private String employeeNameKana;
        // 性别用于基础资料占位。
        // 声明 gender 字段，用来承载当前业务对象的传输信息。
        private String gender;
        // 雇佣类型用于生成默认勤怠规则。
        // 声明 employmentType 字段，用来承载当前业务对象的传输信息。
        private String employmentType;
        // 入社日用于后续排班可用性判断。
        // 声明 入社Date 字段，用来承载当前业务对象的传输信息。
        private LocalDate hireDate;
        // 退社日用于员工状态说明。
        // 声明 退社Date 字段，用来承载当前业务对象的传输信息。
        private LocalDate resignDate;
        // 邮箱用于联系方式展示。
        // 声明 邮箱 字段，用来承载当前业务对象的传输信息。
        private String email;
        // 电话用于联系方式展示。
        // 声明 电话 字段，用来承载当前业务对象的传输信息。
        private String phone;
        // 事业所归属用于后续排班地点范围。
        // 声明 事业所Id 字段，用来承载当前业务对象的传输信息。
        private Long workplaceId;
        // 部门归属用于管理筛选。
        // 声明 部门Id 字段，用来承载当前业务对象的传输信息。
        private Long departmentId;
        // 状态用于启用或停用控制。
        // 声明 状态 字段，用来承载当前业务对象的传输信息。
        private String status;

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
     * 员工列表查询入参。
     */
    // 定义 员工QueryIn，承接当前文件对应的业务职责。
    public static class EmployeeQueryIn {

        // 关键字用于匹配姓名和员工编号。
        // 声明 keyword 字段，用来承载当前业务对象的传输信息。
        private String keyword;
        // 部门主键用于员工筛选。
        // 声明 部门Id 字段，用来承载当前业务对象的传输信息。
        private Long departmentId;
        // 雇佣类型用于筛选不同工种。
        // 声明 employmentType 字段，用来承载当前业务对象的传输信息。
        private String employmentType;
        // 状态用于筛选启停用员工。
        // 声明 状态 字段，用来承载当前业务对象的传输信息。
        private String status;

        // 对外返回 Keyword，供上下游继续读取当前业务字段。
        public String getKeyword() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return keyword;
        }

        // 回填 Keyword，让请求绑定或结果组装保存当前字段值。
        public void setKeyword(String keyword) {
            // 把外部传入结果写入 keyword 字段，供后续流程继续使用。
            this.keyword = keyword;
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
     * 外部打卡绑定入参。
     */
    // 定义 外部系统映射保存In，承接当前文件对应的业务职责。
    public static class ExternalMappingSaveIn {

        // 外部系统名称用于标识打卡来源。
        // 声明 sourceSystem 字段，用来承载当前业务对象的传输信息。
        private String sourceSystem;
        // 外部员工 ID 用于后续对接真实打卡平台。
        // 声明 外部系统员工Id 字段，用来承载当前业务对象的传输信息。
        private String externalEmployeeId;
        // 外部员工编号用于人工核对。
        // 声明 外部系统员工No 字段，用来承载当前业务对象的传输信息。
        private String externalEmployeeNo;
        // 状态用于记录当前绑定是否启用。
        // 声明 状态 字段，用来承载当前业务对象的传输信息。
        private String status;

        // 对外返回 SourceSystem，供上下游继续读取当前业务字段。
        public String getSourceSystem() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return sourceSystem;
        }

        // 回填 SourceSystem，让请求绑定或结果组装保存当前字段值。
        public void setSourceSystem(String sourceSystem) {
            // 把外部传入结果写入 sourceSystem 字段，供后续流程继续使用。
            this.sourceSystem = sourceSystem;
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
     * 员工导入入参。
     */
    // 定义 员工导入In，承接当前文件对应的业务职责。
    public static class EmployeeImportIn {

        // CSV 文本承接前端粘贴或文件读取后的内容。
        // 声明 csvText 字段，用来承载当前业务对象的传输信息。
        private String csvText;

        // 对外返回 CsvText，供上下游继续读取当前业务字段。
        public String getCsvText() {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return csvText;
        }

        // 回填 CsvText，让请求绑定或结果组装保存当前字段值。
        public void setCsvText(String csvText) {
            // 把外部传入结果写入 csvText 字段，供后续流程继续使用。
            this.csvText = csvText;
        }
    }

    /**
     * 班次模板保存入参。
     */
    // 定义 班次模板保存In，承接当前文件对应的业务职责。
    public static class ShiftTemplateSaveIn {

        // 模板编码用于业务唯一识别。
        // 声明 模板编码 字段，用来承载当前业务对象的传输信息。
        private String templateCode;
        // 模板名称用于页面和导出展示。
        // 声明 模板名称 字段，用来承载当前业务对象的传输信息。
        private String templateName;
        // 班次类型用于区分工作班、休息、有休。
        // 声明 班次Type 字段，用来承载当前业务对象的传输信息。
        private String shiftType;
        // 开始时间用于后续排班和工时计算。
        // 声明 startTime 字段，用来承载当前业务对象的传输信息。
        private String startTime;
        // 结束时间用于后续排班和工时计算。
        // 声明 endTime 字段，用来承载当前业务对象的传输信息。
        private String endTime;
        // 是否跨日用于夜班等场景。
        // 声明 crossDay 字段，用来承载当前业务对象的传输信息。
        private Boolean crossDay;
        // 休息分钟数用于后续计算基线。
        // 声明 scheduledBreakMinutes 字段，用来承载当前业务对象的传输信息。
        private Integer scheduledBreakMinutes;
        // 颜色用于前端展示区分。
        // 声明 颜色 字段，用来承载当前业务对象的传输信息。
        private String color;
        // 是否启用用于保留历史模板。
        // 声明 启用标记 字段，用来承载当前业务对象的传输信息。
        private Boolean active;

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
}
