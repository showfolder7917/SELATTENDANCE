package com.sp.selfsp.attendance.schedule.controller;

import com.sp.selfsp.attendance.domain.out.AttendanceOut;
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
// 把当前类注册为 Spring REST 控制器，负责对外暴露考勤接口。
@RestController
// 给当前控制器绑定统一接口前缀，便于前端按模块访问。
@RequestMapping("/api/attendance/schedules")
// 定义 考勤排班控制器，承接当前文件对应的业务职责。
public class AttendanceScheduleController {

    // 声明 考勤排班服务 字段，用来保存当前业务状态或依赖。
    private final AttendanceScheduleService attendanceScheduleService;

    // 定义 考勤排班控制器 接口入口，负责接收前端请求并转发到业务服务。
    public AttendanceScheduleController(AttendanceScheduleService attendanceScheduleService) {
        // 把外部传入结果写入 考勤排班服务 字段，供后续流程继续使用。
        this.attendanceScheduleService = attendanceScheduleService;
    }

    // 把当前方法暴露为查询接口，供前端读取业务数据。
    @GetMapping
    // 定义 查询排班看板 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceScheduleOut.ScheduleBoardOut> getScheduleBoard(
        @RequestParam String month,
        @RequestParam(required = false) Long workplaceId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) Boolean onlyUnassigned
    ) {
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
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
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceScheduleService.getScheduleBoard(queryIn));
    }

    // 把当前方法暴露为新增接口，供前端提交新数据。
    @PostMapping
    // 定义 新增单日排班 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceScheduleOut.ScheduleItemOut> createSchedule(@RequestBody AttendanceScheduleIn.ScheduleSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceScheduleService.createSchedule(saveIn));
    }

    // 把当前方法暴露为更新接口，供前端保存修改结果。
    @PutMapping("/{id}")
    // 定义 更新单日排班 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceScheduleOut.ScheduleItemOut> updateSchedule(
        @PathVariable Long id,
        @RequestBody AttendanceScheduleIn.ScheduleSaveIn saveIn
    ) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceScheduleService.updateSchedule(id, saveIn));
    }

    // 把当前方法暴露为删除接口，供前端移除业务数据。
    @DeleteMapping("/{id}")
    // 定义 删除单日排班 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<Void> deleteSchedule(@PathVariable Long id) {
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
        attendanceScheduleService.deleteSchedule(id);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(null);
    }

    // 把当前方法暴露为新增接口，供前端提交批量排班动作。
    @PostMapping("/batch-assign")
    // 定义 批量排班 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceScheduleOut.ScheduleBatchResultOut> batchAssignSchedules(
        @RequestBody AttendanceScheduleIn.ScheduleBatchAssignIn saveIn
    ) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceScheduleService.batchAssignSchedules(saveIn));
    }

    // 把当前方法暴露为新增接口，供前端执行上周复制动作。
    @PostMapping("/copy-last-week")
    // 定义 复制上周排班 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceScheduleOut.ScheduleBatchResultOut> copyLastWeek(
        @RequestBody AttendanceScheduleIn.ScheduleCopyIn saveIn
    ) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceScheduleService.copyLastWeek(saveIn));
    }

    // 把当前方法暴露为新增接口，供前端执行上月复制动作。
    @PostMapping("/copy-last-month")
    // 定义 复制上月排班 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceScheduleOut.ScheduleBatchResultOut> copyLastMonth(
        @RequestBody AttendanceScheduleIn.ScheduleCopyIn saveIn
    ) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceScheduleService.copyLastMonth(saveIn));
    }

    // 把当前方法暴露为新增接口，供前端清空一段排班数据。
    @PostMapping("/clear-range")
    // 定义 清空排班区间 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceScheduleOut.ScheduleBatchResultOut> clearSchedules(
        @RequestBody AttendanceScheduleIn.ScheduleClearRangeIn saveIn
    ) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceScheduleService.clearSchedules(saveIn));
    }

    // 把当前方法暴露为查询接口，供前端读取未排班检查结果。
    @GetMapping("/unassigned-check")
    // 定义 检查未排班员工 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<List<AttendanceScheduleOut.ScheduleUnassignedOut>> checkUnassignedSchedules(
        @RequestParam String month,
        @RequestParam(required = false) Long workplaceId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) Boolean onlyUnassigned
    ) {
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
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
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceScheduleService.checkUnassignedSchedules(queryIn));
    }

    // 把当前方法暴露为查询接口，供前端导出排班 CSV。
    @GetMapping("/export")
    // 定义 导出排班表 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.CsvExportOut> exportSchedules(
        @RequestParam String month,
        @RequestParam(required = false) Long workplaceId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) Boolean onlyUnassigned
    ) {
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
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
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceScheduleService.exportSchedules(queryIn));
    }
}
