package com.sp.selfsp.attendance.monthly.domain.in;

/**
 * 第六阶段月次汇总与月结输入对象集合。
 */
public final class AttendanceMonthlyIn {

    private AttendanceMonthlyIn() {
    }

    /**
     * 月次列表查询入参。
     */
    public static class MonthlyQueryIn {

        // 年月用于限定本次查看或重算的月份窗口，格式固定为 YYYY-MM。
        private String yearMonth;
        // 事业所筛选用于只看某个据点的月次结果。
        private Long workplaceId;
        // 部门筛选用于继续缩小月次结果范围。
        private Long departmentId;
        // 员工关键字用于按编号或姓名快速定位月次对象。
        private String employeeKeyword;
        // 员工主键用于服务层在单人月次重算时直接缩小聚合范围。
        private Long employeeId;
        // 月结状态用于区分未结、可结、已结和反结结果。
        private String closeStatus;
        // 只看阻塞项用于让管理员先聚焦不能月结的人。
        private Boolean blockedOnly;
        // 页码用于控制数据库分页。
        private Integer page;
        // 每页条数用于控制月次列表返回量。
        private Integer pageSize;

        public String getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
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

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getCloseStatus() {
            return closeStatus;
        }

        public void setCloseStatus(String closeStatus) {
            this.closeStatus = closeStatus;
        }

        public Boolean getBlockedOnly() {
            return blockedOnly;
        }

        public void setBlockedOnly(Boolean blockedOnly) {
            this.blockedOnly = blockedOnly;
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
    }

    /**
     * 月次重算入参。
     */
    public static class MonthlyRecalculateIn {

        // 月份用于指明本次要重算哪个月。
        private String yearMonth;
        // 事业所筛选用于缩小重算范围。
        private Long workplaceId;
        // 部门筛选用于继续缩小重算范围。
        private Long departmentId;
        // 员工主键用于单人月次重算。
        private Long employeeId;
        // 重算模式用于区分单人、范围或整月重算。
        private String recalcMode;
        // 是否允许覆盖已结月份，默认不允许。
        private Boolean overwriteClosed;
        // 操作人用于动作日志留痕。
        private Long operatorId;

        public String getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
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

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getRecalcMode() {
            return recalcMode;
        }

        public void setRecalcMode(String recalcMode) {
            this.recalcMode = recalcMode;
        }

        public Boolean getOverwriteClosed() {
            return overwriteClosed;
        }

        public void setOverwriteClosed(Boolean overwriteClosed) {
            this.overwriteClosed = overwriteClosed;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Long operatorId) {
            this.operatorId = operatorId;
        }
    }

    /**
     * 月结确认入参。
     */
    public static class MonthlyCloseIn {

        // 月份用于指明当前要结算哪个月。
        private String yearMonth;
        // 范围类型用于支持公司、据点、部门或单人月结。
        private String scopeType;
        // 范围主键用于和范围类型一起确定月结目标。
        private Long scopeId;
        // 操作人用于记录谁执行了月结确认。
        private Long operatorId;
        // 备注用于解释本次月结说明。
        private String comment;

        public String getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
        }

        public String getScopeType() {
            return scopeType;
        }

        public void setScopeType(String scopeType) {
            this.scopeType = scopeType;
        }

        public Long getScopeId() {
            return scopeId;
        }

        public void setScopeId(Long scopeId) {
            this.scopeId = scopeId;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Long operatorId) {
            this.operatorId = operatorId;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    /**
     * 反结入参。
     */
    public static class MonthlyReopenIn {

        // 月次主键用于定位需要反结的月结记录。
        private Long monthlyId;
        // 操作人用于记录谁发起了反结。
        private Long operatorId;
        // 原因用于解释为什么当前月次需要重新开放。
        private String reason;

        public Long getMonthlyId() {
            return monthlyId;
        }

        public void setMonthlyId(Long monthlyId) {
            this.monthlyId = monthlyId;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Long operatorId) {
            this.operatorId = operatorId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
