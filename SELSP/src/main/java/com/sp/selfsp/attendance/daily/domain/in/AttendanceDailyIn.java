package com.sp.selfsp.attendance.daily.domain.in;

/**
 * 第四阶段日次勤怠输入对象集合。
 */
public final class AttendanceDailyIn {

    private AttendanceDailyIn() {
    }

    /**
     * 日次列表查询入参。
     */
    public static class DailyQueryIn {

        // 开始日期用于圈定本次要查看和自动补算的日次范围。
        private String startDate;
        // 结束日期用于圈定本次要查看和自动补算的日次范围。
        private String endDate;
        // 事业所筛选用于把日次结果限制在指定地点。
        private Long workplaceId;
        // 部门筛选用于把日次结果限制在指定组织范围。
        private Long departmentId;
        // 员工关键字用于按编号或姓名快速定位目标员工。
        private String employeeKeyword;
        // 状态筛选用于优先查看迟到、缺勤或缺卡记录。
        private String status;
        // 只看异常用于把正常记录先隐藏，让管理员集中处理问题日次。
        private Boolean exceptionOnly;
        // 页码用于控制列表分页。
        private Integer page;
        // 每页条数用于控制数据库分页返回量。
        private Integer pageSize;

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Boolean getExceptionOnly() {
            return exceptionOnly;
        }

        public void setExceptionOnly(Boolean exceptionOnly) {
            this.exceptionOnly = exceptionOnly;
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
     * 单日重算入参。
     */
    public static class DailyRecalculateIn {

        // 员工主键用于定位要重算哪位员工。
        private Long employeeId;
        // 工作日期用于定位要重算哪一天。
        private String workDate;

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getWorkDate() {
            return workDate;
        }

        public void setWorkDate(String workDate) {
            this.workDate = workDate;
        }
    }

    /**
     * 范围重算入参。
     */
    public static class DailyRecalculateRangeIn {

        // 开始日期用于定义本次批量重算起点。
        private String startDate;
        // 结束日期用于定义本次批量重算终点。
        private String endDate;
        // 事业所筛选用于缩小批量重算范围。
        private Long workplaceId;
        // 部门筛选用于缩小批量重算范围。
        private Long departmentId;
        // 员工关键字用于按姓名或编号快速限定范围。
        private String employeeKeyword;
        // 只看异常用于只重算当前异常记录集合。
        private Boolean exceptionOnly;

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
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

        public Boolean getExceptionOnly() {
            return exceptionOnly;
        }

        public void setExceptionOnly(Boolean exceptionOnly) {
            this.exceptionOnly = exceptionOnly;
        }
    }
}
