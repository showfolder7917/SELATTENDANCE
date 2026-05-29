package com.sp.selfsp.attendance.schedule.dao;

import com.sp.selfsp.attendance.schedule.domain.out.AttendanceScheduleOut;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 第二阶段排班数据访问接口。
 */
@Mapper
public interface AttendanceScheduleDao {

    // 按租户、日期范围和员工范围读取排班看板数据，供月度排班页面直接渲染。
    List<AttendanceScheduleOut.ScheduleItemOut> selectScheduleItems(
        @Param("tenantId") Long tenantId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("employeeIds") List<Long> employeeIds
    );

    // 按主键读取单条排班，用于编辑、复制和删除前确认当前记录是否存在。
    AttendanceScheduleOut.ScheduleItemOut selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 按员工和工作日读取排班，用于防止同一天重复占用员工的排班槽位。
    AttendanceScheduleOut.ScheduleItemOut selectByEmployeeAndDate(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate
    );

    // 新增单日排班，把班次模板展开后的计划时间和工作日类型一并落库。
    int insert(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate,
        @Param("shiftTemplateId") Long shiftTemplateId,
        @Param("scheduledStartTime") LocalDateTime scheduledStartTime,
        @Param("scheduledEndTime") LocalDateTime scheduledEndTime,
        @Param("scheduledBreakMinutes") Integer scheduledBreakMinutes,
        @Param("workDayType") String workDayType,
        @Param("remark") String remark
    );

    // 更新既有排班，用于改班次、改日期或调整备注等排班维护动作。
    int updateById(
        @Param("tenantId") Long tenantId,
        @Param("id") Long id,
        @Param("shiftTemplateId") Long shiftTemplateId,
        @Param("scheduledStartTime") LocalDateTime scheduledStartTime,
        @Param("scheduledEndTime") LocalDateTime scheduledEndTime,
        @Param("scheduledBreakMinutes") Integer scheduledBreakMinutes,
        @Param("workDayType") String workDayType,
        @Param("remark") String remark
    );

    // 删除单条排班，供页面删除指定一天的排班记录。
    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 按员工集合和日期范围批量清空排班，供批量清除或覆盖前先删旧数据。
    int deleteRange(
        @Param("tenantId") Long tenantId,
        @Param("employeeIds") List<Long> employeeIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
