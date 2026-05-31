package com.sp.selfsp.attendance.schedule.controller;

import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.schedule.domain.in.AttendanceScheduleIn;
import com.sp.selfsp.attendance.schedule.domain.out.AttendanceScheduleOut;
import com.sp.selfsp.attendance.schedule.service.AttendanceScheduleService;
import com.sp.selfsp.common.util.CommonResponse;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第二阶段排班控制器。
 */
@RestController
@RequestMapping("/api/attendance/schedules")
public class AttendanceScheduleController {

    private final AttendanceScheduleService attendanceScheduleService;

    public AttendanceScheduleController(AttendanceScheduleService attendanceScheduleService) {
        this.attendanceScheduleService = attendanceScheduleService;
    }

    @GetMapping
    public CommonResponse<AttendanceScheduleOut.ScheduleBoardOut> getScheduleBoard(
        @RequestParam String month,
        @RequestParam(required = false) Long workplaceId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) Boolean onlyUnassigned
    ) {
        AttendanceScheduleIn.ScheduleBoardQueryIn queryIn = new AttendanceScheduleIn.ScheduleBoardQueryIn();
        // 把请求里的月份写入查询对象，让服务层据此构建日历区间。
        queryIn.setMonth(month);
        // 把地点过滤条件写入查询对象，便于只看单个事业所的排班。
        queryIn.setWorkplaceId(workplaceId);
        // 把部门过滤条件写入查询对象，便于缩小到一个组织单元。
        queryIn.setDepartmentId(departmentId);
        // 把员工关键字写入查询对象，便于服务层复用统一筛选逻辑。
        queryIn.setEmployeeKeyword(employeeKeyword);
        // 把只看未排班开关写入查询对象，便于优先暴露需要处理的人。
        queryIn.setOnlyUnassigned(onlyUnassigned);
        return CommonResponse.success(attendanceScheduleService.getScheduleBoard(queryIn));
    }

    @PostMapping
    public CommonResponse<AttendanceScheduleOut.ScheduleItemOut> createSchedule(@RequestBody AttendanceScheduleIn.ScheduleSaveIn saveIn) {
        return CommonResponse.success(attendanceScheduleService.createSchedule(saveIn));
    }

    @PutMapping("/{id}")
    public CommonResponse<AttendanceScheduleOut.ScheduleItemOut> updateSchedule(
        @PathVariable Long id,
        @RequestBody AttendanceScheduleIn.ScheduleSaveIn saveIn
    ) {
        return CommonResponse.success(attendanceScheduleService.updateSchedule(id, saveIn));
    }

    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteSchedule(@PathVariable Long id) {
        attendanceScheduleService.deleteSchedule(id);
        return CommonResponse.success(null);
    }

    @PostMapping("/batch-assign")
    public CommonResponse<AttendanceScheduleOut.ScheduleBatchResultOut> batchAssignSchedules(
        @RequestBody AttendanceScheduleIn.ScheduleBatchAssignIn saveIn
    ) {
        return CommonResponse.success(attendanceScheduleService.batchAssignSchedules(saveIn));
    }

    @PostMapping("/copy-last-week")
    public CommonResponse<AttendanceScheduleOut.ScheduleBatchResultOut> copyLastWeek(
        @RequestBody AttendanceScheduleIn.ScheduleCopyIn saveIn
    ) {
        return CommonResponse.success(attendanceScheduleService.copyLastWeek(saveIn));
    }

    @PostMapping("/copy-last-month")
    public CommonResponse<AttendanceScheduleOut.ScheduleBatchResultOut> copyLastMonth(
        @RequestBody AttendanceScheduleIn.ScheduleCopyIn saveIn
    ) {
        return CommonResponse.success(attendanceScheduleService.copyLastMonth(saveIn));
    }

    @PostMapping("/clear-range")
    public CommonResponse<AttendanceScheduleOut.ScheduleBatchResultOut> clearSchedules(
        @RequestBody AttendanceScheduleIn.ScheduleClearRangeIn saveIn
    ) {
        return CommonResponse.success(attendanceScheduleService.clearSchedules(saveIn));
    }

    @GetMapping("/unassigned-check")
    public CommonResponse<List<AttendanceScheduleOut.ScheduleUnassignedOut>> checkUnassignedSchedules(
        @RequestParam String month,
        @RequestParam(required = false) Long workplaceId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) Boolean onlyUnassigned
    ) {
        AttendanceScheduleIn.ScheduleBoardQueryIn queryIn = new AttendanceScheduleIn.ScheduleBoardQueryIn();
        // 把本次检查所处月份写入查询对象。
        queryIn.setMonth(month);
        // 把当前筛选中的事业所带入未排班检查。
        queryIn.setWorkplaceId(workplaceId);
        // 把当前筛选中的部门带入未排班检查。
        queryIn.setDepartmentId(departmentId);
        // 把当前关键字带入未排班检查，避免返回无关员工。
        queryIn.setEmployeeKeyword(employeeKeyword);
        // 把只看未排班开关也带入，保证左右两边结果一致。
        queryIn.setOnlyUnassigned(onlyUnassigned);
        return CommonResponse.success(attendanceScheduleService.checkUnassignedSchedules(queryIn));
    }

    @GetMapping("/export")
    public CommonResponse<AttendanceOut.CsvExportOut> exportSchedules(
        @RequestParam String month,
        @RequestParam(required = false) Long workplaceId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) Boolean onlyUnassigned
    ) {
        AttendanceScheduleIn.ScheduleBoardQueryIn queryIn = new AttendanceScheduleIn.ScheduleBoardQueryIn();
        // 把月份写入导出查询对象，保证导出的区间和页面一致。
        queryIn.setMonth(month);
        // 把筛选中的事业所带入导出，避免导出无关地点的数据。
        queryIn.setWorkplaceId(workplaceId);
        // 把筛选中的部门带入导出，保持和当前列表一致。
        queryIn.setDepartmentId(departmentId);
        // 把关键字带入导出，便于导出当前视图结果。
        queryIn.setEmployeeKeyword(employeeKeyword);
        // 把只看未排班开关带入导出，让导出和页面内容一致。
        queryIn.setOnlyUnassigned(onlyUnassigned);
        return CommonResponse.success(attendanceScheduleService.exportSchedules(queryIn));
    }
}
