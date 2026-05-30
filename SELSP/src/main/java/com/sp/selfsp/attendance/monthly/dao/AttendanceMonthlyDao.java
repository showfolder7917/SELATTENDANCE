package com.sp.selfsp.attendance.monthly.dao;

import com.sp.selfsp.attendance.monthly.domain.in.AttendanceMonthlyIn;
import com.sp.selfsp.attendance.monthly.domain.out.AttendanceMonthlyOut;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 第六阶段月次汇总与月结数据访问接口。
 */
@Mapper
public interface AttendanceMonthlyDao {

    // 读取指定月份和筛选范围内的日次明细，供服务层按员工聚合为月次结果。
    List<Map<String, Object>> selectMonthlyAggregationRows(
        @Param("tenantId") Long tenantId,
        @Param("query") AttendanceMonthlyIn.MonthlyQueryIn queryIn
    );

    // 读取已生成月次的列表，供第六阶段中间主表格渲染。
    List<AttendanceMonthlyOut.MonthlyItemOut> selectMonthlyList(
        @Param("tenantId") Long tenantId,
        @Param("query") AttendanceMonthlyIn.MonthlyQueryIn queryIn,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );

    // 统计当前月次查询总数，供共享分页器回显数据库分页。
    Integer countMonthlyList(@Param("tenantId") Long tenantId, @Param("query") AttendanceMonthlyIn.MonthlyQueryIn queryIn);

    // 统计不同月结状态数量，供顶部统计卡使用。
    List<Map<String, Object>> countMonthlySummary(@Param("tenantId") Long tenantId, @Param("query") AttendanceMonthlyIn.MonthlyQueryIn queryIn);

    // 按主键读取一条月次详情基础信息，供右侧详情面板展示。
    AttendanceMonthlyOut.MonthlyDetailOut selectMonthlyDetailById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 读取月次统计项，供右侧详情解释每个数字的构成。
    List<AttendanceMonthlyOut.MonthlyMetricItemOut> selectMonthlyItems(@Param("tenantId") Long tenantId, @Param("monthlyId") Long monthlyId);

    // 读取月次动作日志，供右侧详情展示重算、月结和反结留痕。
    List<AttendanceMonthlyOut.ActionLogOut> selectMonthlyActionLogs(@Param("tenantId") Long tenantId, @Param("monthlyId") Long monthlyId);

    // 读取月次下的日次快照，供右侧详情追溯来源。
    List<AttendanceMonthlyOut.DailySnapshotOut> selectMonthlyDailySnapshots(@Param("tenantId") Long tenantId, @Param("monthlyId") Long monthlyId);

    // 读取月次阻塞原因原始行，供服务层整理成阻塞原因清单。
    List<Map<String, Object>> selectMonthlyBlockRows(@Param("tenantId") Long tenantId, @Param("monthlyId") Long monthlyId);

    // 按租户、月份和员工读取已有月次主键与状态，供聚合时决定保留还是覆盖。
    Map<String, Object> selectMonthlyIdentity(
        @Param("tenantId") Long tenantId,
        @Param("yearMonth") String yearMonth,
        @Param("employeeId") Long employeeId
    );

    // 以唯一键合并月次主表，保证重复重算时只保留最新月汇总。
    int mergeMonthly(
        @Param("tenantId") Long tenantId,
        @Param("yearMonth") String yearMonth,
        @Param("employeeId") Long employeeId,
        @Param("workplaceId") Long workplaceId,
        @Param("departmentId") Long departmentId,
        @Param("scheduledDays") Integer scheduledDays,
        @Param("attendanceDays") Integer attendanceDays,
        @Param("normalDays") Integer normalDays,
        @Param("lateCount") Integer lateCount,
        @Param("earlyLeaveCount") Integer earlyLeaveCount,
        @Param("missingPunchCount") Integer missingPunchCount,
        @Param("absenceCount") Integer absenceCount,
        @Param("exceptionDays") Integer exceptionDays,
        @Param("paidLeaveDays") BigDecimal paidLeaveDays,
        @Param("restDays") BigDecimal restDays,
        @Param("closeStatus") String closeStatus,
        @Param("blockReasonCount") Integer blockReasonCount,
        @Param("remark") String remark,
        @Param("closedAt") LocalDateTime closedAt,
        @Param("closedBy") Long closedBy,
        @Param("reopenedAt") LocalDateTime reopenedAt,
        @Param("reopenedBy") Long reopenedBy,
        @Param("createdAt") LocalDateTime createdAt,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    // 删除旧统计项，供本次月次重算后重建最新快照。
    int deleteMonthlyItems(@Param("tenantId") Long tenantId, @Param("monthlyId") Long monthlyId);

    // 插入一条月次统计项，供详情页展示每个数字的来源说明。
    int insertMonthlyItem(
        @Param("tenantId") Long tenantId,
        @Param("monthlyId") Long monthlyId,
        @Param("itemCode") String itemCode,
        @Param("itemName") String itemName,
        @Param("itemValue") String itemValue,
        @Param("itemOrder") Integer itemOrder,
        @Param("sourceJson") String sourceJson
    );

    // 写入月结动作日志，供月次详情展示重算、月结和反结留痕。
    int insertMonthlyActionLog(
        @Param("tenantId") Long tenantId,
        @Param("monthlyId") Long monthlyId,
        @Param("actionType") String actionType,
        @Param("operatorId") Long operatorId,
        @Param("actionComment") String actionComment,
        @Param("snapshotJson") String snapshotJson
    );

    // 按主键更新月结状态，供月结和反结动作复用。
    int updateMonthlyCloseState(
        @Param("tenantId") Long tenantId,
        @Param("id") Long id,
        @Param("closeStatus") String closeStatus,
        @Param("closedAt") LocalDateTime closedAt,
        @Param("closedBy") Long closedBy,
        @Param("reopenedAt") LocalDateTime reopenedAt,
        @Param("reopenedBy") Long reopenedBy,
        @Param("remark") String remark
    );

    // 按月份和范围读取待月结月次主键，供月结动作批量更新状态。
    List<Map<String, Object>> selectMonthlyScopeRows(
        @Param("tenantId") Long tenantId,
        @Param("yearMonth") String yearMonth,
        @Param("scopeType") String scopeType,
        @Param("scopeId") Long scopeId
    );
}
