package com.sp.selfsp.attendance.schedule.service;

import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.schedule.domain.in.AttendanceScheduleIn;
import com.sp.selfsp.attendance.schedule.domain.out.AttendanceScheduleOut;
import java.util.List;

/**
 * 第二阶段排班服务接口。
 */
// 定义 考勤排班服务，承接当前文件对应的业务职责。
public interface AttendanceScheduleService {

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceScheduleOut.ScheduleBoardOut getScheduleBoard(AttendanceScheduleIn.ScheduleBoardQueryIn queryIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceScheduleOut.ScheduleItemOut createSchedule(AttendanceScheduleIn.ScheduleSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceScheduleOut.ScheduleItemOut updateSchedule(Long id, AttendanceScheduleIn.ScheduleSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    void deleteSchedule(Long id);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceScheduleOut.ScheduleBatchResultOut batchAssignSchedules(AttendanceScheduleIn.ScheduleBatchAssignIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceScheduleOut.ScheduleBatchResultOut copyLastWeek(AttendanceScheduleIn.ScheduleCopyIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceScheduleOut.ScheduleBatchResultOut copyLastMonth(AttendanceScheduleIn.ScheduleCopyIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceScheduleOut.ScheduleBatchResultOut clearSchedules(AttendanceScheduleIn.ScheduleClearRangeIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    List<AttendanceScheduleOut.ScheduleUnassignedOut> checkUnassignedSchedules(AttendanceScheduleIn.ScheduleBoardQueryIn queryIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.CsvExportOut exportSchedules(AttendanceScheduleIn.ScheduleBoardQueryIn queryIn);
}
