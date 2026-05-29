package com.sp.selfsp.attendance.daily.dao;

import com.sp.selfsp.attendance.daily.domain.in.AttendanceDailyIn;
import com.sp.selfsp.attendance.daily.domain.out.AttendanceDailyOut;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 第四阶段日次勤怠数据访问接口。
 */
@Mapper
public interface AttendanceDailyDao {

    // 读取本次查询涉及的员工日期组合，供列表前自动补算缺失日次结果。
    List<Map<String, Object>> selectCalculationTargets(
        @Param("tenantId") Long tenantId,
        @Param("query") AttendanceDailyIn.DailyQueryIn queryIn
    );

    // 按员工与日期读取排班快照，供日次计算确定计划班次和计划时间。
    AttendanceDailyOut.ScheduleSnapshotOut selectScheduleSnapshot(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate
    );

    // 按员工与日期读取已处理打卡，供日次计算选取最早上班卡和最晚下班卡。
    List<AttendanceDailyOut.PunchSnapshotOut> selectProcessedPunches(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate
    );

    // 读取当前员工日期已有日次结果，供重算时判断是更新还是首次生成。
    Map<String, Object> selectDailyIdentity(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate
    );

    // 用唯一键合并写入日次结果，保证重复重算时只保留最新结果。
    int mergeDaily(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate,
        @Param("shiftScheduleId") Long shiftScheduleId,
        @Param("workDayType") String workDayType,
        @Param("scheduledStartTime") LocalDateTime scheduledStartTime,
        @Param("scheduledEndTime") LocalDateTime scheduledEndTime,
        @Param("scheduledBreakMinutes") Integer scheduledBreakMinutes,
        @Param("scheduledWorkMinutes") Integer scheduledWorkMinutes,
        @Param("actualClockIn") LocalDateTime actualClockIn,
        @Param("actualClockOut") LocalDateTime actualClockOut,
        @Param("actualBreakMinutes") Integer actualBreakMinutes,
        @Param("actualWorkMinutes") Integer actualWorkMinutes,
        @Param("lateMinutes") Integer lateMinutes,
        @Param("earlyLeaveMinutes") Integer earlyLeaveMinutes,
        @Param("absenceMinutes") Integer absenceMinutes,
        @Param("normalWorkMinutes") Integer normalWorkMinutes,
        @Param("overtimeMinutes") Integer overtimeMinutes,
        @Param("legalOvertimeMinutes") Integer legalOvertimeMinutes,
        @Param("nightWorkMinutes") Integer nightWorkMinutes,
        @Param("holidayWorkMinutes") Integer holidayWorkMinutes,
        @Param("status") String status,
        @Param("approvalStatus") String approvalStatus,
        @Param("exceptionFlag") Boolean exceptionFlag,
        @Param("locked") Boolean locked,
        @Param("calculatedAt") LocalDateTime calculatedAt,
        @Param("calcVersion") String calcVersion,
        @Param("calcMessage") String calcMessage
    );

    // 删除旧异常，供每次重算后重建最新异常集合。
    int deleteExceptionsByDailyId(@Param("tenantId") Long tenantId, @Param("attendanceDailyId") Long attendanceDailyId);

    // 写入一条日次异常，供异常列表和详情抽屉复用。
    int insertException(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate,
        @Param("attendanceDailyId") Long attendanceDailyId,
        @Param("exceptionType") String exceptionType,
        @Param("exceptionLevel") String exceptionLevel,
        @Param("status") String status,
        @Param("message") String message,
        @Param("suggestedAction") String suggestedAction
    );

    // 删除旧计算日志，供每次重算后保留最新解释过程。
    int deleteCalcLogsByEmployeeDate(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate
    );

    // 写入一条计算步骤日志，供前端详情抽屉显示人话过程。
    int insertCalcLog(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate,
        @Param("attendanceDailyId") Long attendanceDailyId,
        @Param("triggerType") String triggerType,
        @Param("triggerRefId") Long triggerRefId,
        @Param("calcStatus") String calcStatus,
        @Param("stepName") String stepName,
        @Param("stepMessage") String stepMessage,
        @Param("payloadJson") String payloadJson
    );

    // 读取日次列表，供第四阶段主表格渲染。
    List<AttendanceDailyOut.DailyItemOut> selectDailyList(
        @Param("tenantId") Long tenantId,
        @Param("query") AttendanceDailyIn.DailyQueryIn queryIn,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );

    // 统计当前过滤条件下的日次总数，供前端分页复用。
    Integer countDailyList(@Param("tenantId") Long tenantId, @Param("query") AttendanceDailyIn.DailyQueryIn queryIn);

    // 统计当前过滤条件下的状态数量，供前端顶部统计卡先显示。
    List<Map<String, Object>> countDailySummary(@Param("tenantId") Long tenantId, @Param("query") AttendanceDailyIn.DailyQueryIn queryIn);

    // 按主键读取单条日次详情基础信息，供右侧详情抽屉承接。
    AttendanceDailyOut.DailyDetailOut selectDailyDetailById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 按日次主键读取异常列表，供详情抽屉和异常处理区展示。
    List<AttendanceDailyOut.ExceptionOut> selectExceptionsByDailyId(@Param("tenantId") Long tenantId, @Param("attendanceDailyId") Long attendanceDailyId);

    // 按员工与日期读取计算日志，供详情抽屉展示计算过程。
    List<AttendanceDailyOut.CalcStepOut> selectCalcStepsByEmployeeDate(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate
    );
}
