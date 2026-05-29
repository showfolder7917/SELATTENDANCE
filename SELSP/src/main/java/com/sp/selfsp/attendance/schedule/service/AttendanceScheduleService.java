package com.sp.selfsp.attendance.schedule.service;

import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.schedule.domain.in.AttendanceScheduleIn;
import com.sp.selfsp.attendance.schedule.domain.out.AttendanceScheduleOut;
import java.util.List;

/**
 * 第二阶段排班服务接口。
 */
public interface AttendanceScheduleService {

    AttendanceScheduleOut.ScheduleBoardOut getScheduleBoard(AttendanceScheduleIn.ScheduleBoardQueryIn queryIn);

    AttendanceScheduleOut.ScheduleItemOut createSchedule(AttendanceScheduleIn.ScheduleSaveIn saveIn);

    AttendanceScheduleOut.ScheduleItemOut updateSchedule(Long id, AttendanceScheduleIn.ScheduleSaveIn saveIn);

    void deleteSchedule(Long id);

    AttendanceScheduleOut.ScheduleBatchResultOut batchAssignSchedules(AttendanceScheduleIn.ScheduleBatchAssignIn saveIn);

    AttendanceScheduleOut.ScheduleBatchResultOut copyLastWeek(AttendanceScheduleIn.ScheduleCopyIn saveIn);

    AttendanceScheduleOut.ScheduleBatchResultOut copyLastMonth(AttendanceScheduleIn.ScheduleCopyIn saveIn);

    AttendanceScheduleOut.ScheduleBatchResultOut clearSchedules(AttendanceScheduleIn.ScheduleClearRangeIn saveIn);

    List<AttendanceScheduleOut.ScheduleUnassignedOut> checkUnassignedSchedules(AttendanceScheduleIn.ScheduleBoardQueryIn queryIn);

    AttendanceOut.CsvExportOut exportSchedules(AttendanceScheduleIn.ScheduleBoardQueryIn queryIn);
}
