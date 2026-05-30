package com.sp.selfsp.attendance.casefile.dao;

import com.sp.selfsp.attendance.casefile.domain.in.AttendanceCaseIn;
import com.sp.selfsp.attendance.casefile.domain.out.AttendanceCaseOut;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 第五阶段异常处理单数据访问接口。
 */
@Mapper
public interface AttendanceCaseDao {

    // 读取处理中案件和未建单异常，供第五阶段主列表统一展示。
    List<AttendanceCaseOut.CaseItemOut> selectCaseList(
        @Param("tenantId") Long tenantId,
        @Param("query") AttendanceCaseIn.CaseQueryIn queryIn,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );

    // 统计当前过滤条件下总案件数，供分页栏展示。
    Integer countCaseList(@Param("tenantId") Long tenantId, @Param("query") AttendanceCaseIn.CaseQueryIn queryIn);

    // 按案件状态统计数量，供顶部统计卡显示流转分布。
    List<Map<String, Object>> countCaseSummary(@Param("tenantId") Long tenantId, @Param("query") AttendanceCaseIn.CaseQueryIn queryIn);

    // 按案件主键读取详情基础字段，供右侧详情区渲染。
    AttendanceCaseOut.CaseDetailOut selectCaseDetail(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 读取案件动作时间线，供详情页展示审批轨迹。
    List<AttendanceCaseOut.CaseActionLogOut> selectCaseActionLogs(@Param("tenantId") Long tenantId, @Param("caseId") Long caseId);

    // 按日次主键读取最新处理单，供创建前检查是否已存在活动案件。
    Map<String, Object> selectLatestCaseIdentityByDailyId(@Param("tenantId") Long tenantId, @Param("attendanceDailyId") Long attendanceDailyId);

    // 按案件主键读取当前状态和绑定的日次，供动作前校验流转合法性。
    Map<String, Object> selectCaseIdentityById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 新建处理单主记录，把异常说明和期望结果沉淀下来。
    int insertCase(
        @Param("tenantId") Long tenantId,
        @Param("attendanceDailyId") Long attendanceDailyId,
        @Param("employeeId") Long employeeId,
        @Param("workplaceId") Long workplaceId,
        @Param("departmentId") Long departmentId,
        @Param("caseType") String caseType,
        @Param("caseStatus") String caseStatus,
        @Param("applicantId") Long applicantId,
        @Param("applicantRole") String applicantRole,
        @Param("currentApproverId") Long currentApproverId,
        @Param("reasonCategory") String reasonCategory,
        @Param("reasonText") String reasonText,
        @Param("expectedResolution") String expectedResolution,
        @Param("patchPayloadJson") String patchPayloadJson,
        @Param("submittedAt") LocalDateTime submittedAt
    );

    // 按日次主键回读最新处理单详情，供建单后直接刷新右侧结果。
    AttendanceCaseOut.CaseDetailOut selectLatestCaseDetailByDailyId(@Param("tenantId") Long tenantId, @Param("attendanceDailyId") Long attendanceDailyId);

    // 更新处理单状态和审批痕迹，供通过、退回、驳回动作复用。
    int updateCaseStatus(
        @Param("tenantId") Long tenantId,
        @Param("id") Long id,
        @Param("caseStatus") String caseStatus,
        @Param("currentApproverId") Long currentApproverId,
        @Param("patchPayloadJson") String patchPayloadJson,
        @Param("approvedAt") LocalDateTime approvedAt,
        @Param("rejectedAt") LocalDateTime rejectedAt,
        @Param("lockedAt") LocalDateTime lockedAt
    );

    // 写入审批动作日志，供详情页时间线和事后追责复用。
    int insertActionLog(
        @Param("tenantId") Long tenantId,
        @Param("attendanceCaseId") Long attendanceCaseId,
        @Param("actionType") String actionType,
        @Param("operatorId") Long operatorId,
        @Param("operatorRole") String operatorRole,
        @Param("actionComment") String actionComment,
        @Param("beforeSnapshotJson") String beforeSnapshotJson,
        @Param("afterSnapshotJson") String afterSnapshotJson
    );
}
