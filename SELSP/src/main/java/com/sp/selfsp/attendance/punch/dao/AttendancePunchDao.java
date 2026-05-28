package com.sp.selfsp.attendance.punch.dao;

import com.sp.selfsp.attendance.punch.domain.in.AttendancePunchIn;
import com.sp.selfsp.attendance.punch.domain.out.AttendancePunchOut;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 第三阶段打卡数据访问接口。
 */
// 把当前接口注册为 MyBatis Mapper，负责数据库读写映射。
@Mapper
// 定义 考勤打卡数据访问，承接当前文件对应的业务职责。
public interface AttendancePunchDao {

    // 读取打卡记录列表，供第三阶段页面列表和导出摘要复用。
    List<AttendancePunchOut.PunchLogItemOut> selectLogs(
        @Param("tenantId") Long tenantId,
        @Param("query") AttendancePunchIn.PunchLogQueryIn queryIn,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );

    // 统计当前过滤条件下的总记录数，供前端分页和规模提示复用。
    Integer countLogs(@Param("tenantId") Long tenantId, @Param("query") AttendancePunchIn.PunchLogQueryIn queryIn);

    // 读取当前过滤条件下各状态数量，供页面顶部摘要先显示处理结果结构。
    List<java.util.Map<String, Object>> countSummaryByStatus(@Param("tenantId") Long tenantId, @Param("query") AttendancePunchIn.PunchLogQueryIn queryIn);

    // 按主键读取打卡详情，供右侧详情面板和重处理动作复用。
    AttendancePunchOut.PunchLogDetailOut selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 按来源和事件编号读取既有记录，供 CSV / Webhook 去重复用。
    AttendancePunchOut.PunchLogDetailOut selectBySourceEvent(
        @Param("tenantId") Long tenantId,
        @Param("sourceSystem") String sourceSystem,
        @Param("sourceEventId") String sourceEventId
    );

    // 新增一条原始打卡记录，统一承接手动、CSV 和 Webhook 的落库动作。
    int insertLog(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("externalEmployeeId") String externalEmployeeId,
        @Param("sourceSystem") String sourceSystem,
        @Param("sourceEventId") String sourceEventId,
        @Param("punchTime") LocalDateTime punchTime,
        @Param("punchType") String punchType,
        @Param("deviceId") String deviceId,
        @Param("deviceName") String deviceName,
        @Param("rawPayload") String rawPayload,
        @Param("importBatchId") Long importBatchId,
        @Param("processStatus") String processStatus,
        @Param("errorMessage") String errorMessage,
        @Param("ignoredReason") String ignoredReason
    );

    // 读取当前租户最新插入的打卡记录，供新增接口快速返回落库结果。
    AttendancePunchOut.PunchLogDetailOut selectLatestLog(@Param("tenantId") Long tenantId);

    // 更新记录归属员工和处理状态，供绑定员工和重处理成功路径复用。
    int updateEmployeeAndStatus(
        @Param("tenantId") Long tenantId,
        @Param("id") Long id,
        @Param("employeeId") Long employeeId,
        @Param("processStatus") String processStatus,
        @Param("errorMessage") String errorMessage
    );

    // 更新记录为忽略状态，供用户确认不再处理误导入记录。
    int updateIgnored(
        @Param("tenantId") Long tenantId,
        @Param("id") Long id,
        @Param("processStatus") String processStatus,
        @Param("ignoredReason") String ignoredReason
    );

    // 更新记录状态和错误信息，供重处理失败或重复判断回写结果。
    int updateStatus(
        @Param("tenantId") Long tenantId,
        @Param("id") Long id,
        @Param("processStatus") String processStatus,
        @Param("errorMessage") String errorMessage
    );

    // 新建导入批次，供 CSV 导入把整批统计和单条记录挂到同一批次下。
    int insertImportBatch(
        @Param("tenantId") Long tenantId,
        @Param("sourceSystem") String sourceSystem,
        @Param("importType") String importType,
        @Param("fileName") String fileName,
        @Param("status") String status
    );

    // 读取当前租户最新导入批次，供导入完成后返回批次主键和统计结果。
    java.util.Map<String, Object> selectLatestImportBatch(@Param("tenantId") Long tenantId);

    // 回写导入批次统计结果，供导入向导展示成功、失败、重复和未匹配数量。
    int updateImportBatchResult(
        @Param("tenantId") Long tenantId,
        @Param("id") Long id,
        @Param("totalCount") Integer totalCount,
        @Param("successCount") Integer successCount,
        @Param("duplicateCount") Integer duplicateCount,
        @Param("unmatchedCount") Integer unmatchedCount,
        @Param("errorCount") Integer errorCount,
        @Param("ignoredCount") Integer ignoredCount,
        @Param("status") String status
    );
}
