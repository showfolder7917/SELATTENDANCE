package com.sp.selfsp.attendance.casefile.domain.in;

import java.util.List;
import java.util.Map;

/**
 * 第五阶段异常处理与审批输入对象集合。
 */
public final class AttendanceCaseIn {

    private AttendanceCaseIn() {
    }

    /**
     * 处理单列表查询入参。
     */
    public static class CaseQueryIn {

        // 开始日期用于缩小异常列表范围，避免默认加载整月外的数据。
        private String startDate;
        // 结束日期用于和开始日期组成统一查询窗口。
        private String endDate;
        // 事业所筛选用于让管理员只看当前据点的待处理异常。
        private Long workplaceId;
        // 部门筛选用于进一步缩小审批对象范围。
        private Long departmentId;
        // 员工关键字用于按员工编号或姓名快速定位处理单。
        private String employeeKeyword;
        // 处理单状态用于区分待处理、已通过、已驳回等流转节点。
        private String caseStatus;
        // 日次处理状态用于第一眼判断这条异常现在卡在哪一步。
        private String handlingStatus;
        // 仅看我的用于后续审批人只看与自己相关的案件。
        private Boolean mineOnly;
        // 页码用于控制列表分页。
        private Integer page;
        // 每页条数用于控制列表返回量。
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

        public String getCaseStatus() {
            return caseStatus;
        }

        public void setCaseStatus(String caseStatus) {
            this.caseStatus = caseStatus;
        }

        public String getHandlingStatus() {
            return handlingStatus;
        }

        public void setHandlingStatus(String handlingStatus) {
            this.handlingStatus = handlingStatus;
        }

        public Boolean getMineOnly() {
            return mineOnly;
        }

        public void setMineOnly(Boolean mineOnly) {
            this.mineOnly = mineOnly;
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
     * 创建处理单入参。
     */
    public static class CaseCreateIn {

        // 目标日次主键用于把异常处理单和具体日次结果绑定在一起。
        private Long attendanceDailyId;
        // 处理单类型用于标明这次申请处理的是哪类异常。
        private String caseType;
        // 申请人主键用于留痕是谁发起了这条处理单。
        private Long applicantId;
        // 申请人角色用于区分管理员代提还是员工自提。
        private String applicantRole;
        // 原因分类用于做后续统计和筛选。
        private String reasonCategory;
        // 原因说明用于解释为什么需要审批修正。
        private String reasonText;
        // 期望处理结果用于提前告诉审批人希望怎么回写最终结论。
        private String expectedResolution;

        public Long getAttendanceDailyId() {
            return attendanceDailyId;
        }

        public void setAttendanceDailyId(Long attendanceDailyId) {
            this.attendanceDailyId = attendanceDailyId;
        }

        public String getCaseType() {
            return caseType;
        }

        public void setCaseType(String caseType) {
            this.caseType = caseType;
        }

        public Long getApplicantId() {
            return applicantId;
        }

        public void setApplicantId(Long applicantId) {
            this.applicantId = applicantId;
        }

        public String getApplicantRole() {
            return applicantRole;
        }

        public void setApplicantRole(String applicantRole) {
            this.applicantRole = applicantRole;
        }

        public String getReasonCategory() {
            return reasonCategory;
        }

        public void setReasonCategory(String reasonCategory) {
            this.reasonCategory = reasonCategory;
        }

        public String getReasonText() {
            return reasonText;
        }

        public void setReasonText(String reasonText) {
            this.reasonText = reasonText;
        }

        public String getExpectedResolution() {
            return expectedResolution;
        }

        public void setExpectedResolution(String expectedResolution) {
            this.expectedResolution = expectedResolution;
        }
    }

    /**
     * 单条处理动作入参。
     */
    public static class CaseActionIn {

        // 动作类型用于区分通过、驳回和退回补充。
        private String actionType;
        // 审批人主键用于留痕是谁完成了这次动作。
        private Long approverId;
        // 审批备注用于记录通过、退回、驳回的业务说明。
        private String comment;
        // 回写补丁用于审批通过时修正最终打卡时间和最终状态。
        private Map<String, Object> patchPayload;

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public Long getApproverId() {
            return approverId;
        }

        public void setApproverId(Long approverId) {
            this.approverId = approverId;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Map<String, Object> getPatchPayload() {
            return patchPayload;
        }

        public void setPatchPayload(Map<String, Object> patchPayload) {
            this.patchPayload = patchPayload;
        }
    }

    /**
     * 批量处理动作入参。
     */
    public static class CaseBatchActionIn {

        // 处理单主键集合用于一次处理多条同类异常。
        private List<Long> caseIds;
        // 动作类型用于把批量操作统一下发到每条处理单。
        private String actionType;
        // 审批人主键用于批量动作统一留痕。
        private Long approverId;
        // 批量备注用于说明这次批量通过或退回的依据。
        private String comment;
        // 回写补丁用于批量通过时统一应用同一套修正规则。
        private Map<String, Object> patchPayload;

        public List<Long> getCaseIds() {
            return caseIds;
        }

        public void setCaseIds(List<Long> caseIds) {
            this.caseIds = caseIds;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public Long getApproverId() {
            return approverId;
        }

        public void setApproverId(Long approverId) {
            this.approverId = approverId;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Map<String, Object> getPatchPayload() {
            return patchPayload;
        }

        public void setPatchPayload(Map<String, Object> patchPayload) {
            this.patchPayload = patchPayload;
        }
    }
}
