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
// 把当前接口注册为 MyBatis Mapper，负责数据库读写映射。
@Mapper
// 定义 考勤排班数据访问，承接当前文件对应的业务职责。
public interface AttendanceScheduleDao {

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    List<AttendanceScheduleOut.ScheduleItemOut> selectScheduleItems(
        @Param("tenantId") Long tenantId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("employeeIds") List<Long> employeeIds
    );

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    AttendanceScheduleOut.ScheduleItemOut selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    AttendanceScheduleOut.ScheduleItemOut selectByEmployeeAndDate(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate
    );

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
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

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
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

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int deleteRange(
        @Param("tenantId") Long tenantId,
        @Param("employeeIds") List<Long> employeeIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
