package com.sp.selfsp.attendance.daily.controller;

import com.sp.selfsp.attendance.daily.domain.in.AttendanceDailyIn;
import com.sp.selfsp.attendance.daily.domain.out.AttendanceDailyOut;
import com.sp.selfsp.attendance.daily.service.AttendanceDailyService;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第四阶段日次勤怠控制器。
 */
@RestController
@RequestMapping("/api/attendance/daily")
public class AttendanceDailyController {

    private final AttendanceDailyService attendanceDailyService;

    // 注入第四阶段服务实现，统一承接列表、详情和重算动作。
    public AttendanceDailyController(AttendanceDailyService attendanceDailyService) {
        this.attendanceDailyService = attendanceDailyService;
    }

    @GetMapping
    public CommonResponse<AttendanceDailyOut.DailyListOut> listDailyResults(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(required = false) Long workplaceId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Boolean exceptionOnly,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        AttendanceDailyIn.DailyQueryIn queryIn = new AttendanceDailyIn.DailyQueryIn();
        queryIn.setStartDate(startDate);
        queryIn.setEndDate(endDate);
        queryIn.setWorkplaceId(workplaceId);
        queryIn.setDepartmentId(departmentId);
        queryIn.setEmployeeKeyword(employeeKeyword);
        queryIn.setStatus(status);
        queryIn.setExceptionOnly(exceptionOnly);
        queryIn.setPage(page);
        queryIn.setPageSize(pageSize);
        return CommonResponse.success(attendanceDailyService.listDailyResults(queryIn));
    }

    @GetMapping("/{id}")
    public CommonResponse<AttendanceDailyOut.DailyDetailOut> getDailyDetail(@PathVariable Long id) {
        return CommonResponse.success(attendanceDailyService.getDailyDetail(id));
    }

    @PostMapping("/recalculate")
    public CommonResponse<AttendanceDailyOut.RecalculateResultOut> recalculateDaily(
        @RequestBody AttendanceDailyIn.DailyRecalculateIn recalculateIn
    ) {
        return CommonResponse.success(attendanceDailyService.recalculateDaily(recalculateIn));
    }

    @PostMapping("/recalculate-range")
    public CommonResponse<AttendanceDailyOut.RecalculateResultOut> recalculateRange(
        @RequestBody AttendanceDailyIn.DailyRecalculateRangeIn recalculateRangeIn
    ) {
        return CommonResponse.success(attendanceDailyService.recalculateRange(recalculateRangeIn));
    }

    @PostMapping("/{id}/lock")
    public CommonResponse<Void> lockDaily(@PathVariable Long id) {
        attendanceDailyService.lockDaily(id);
        return CommonResponse.success(null);
    }

    @PostMapping("/{id}/unlock")
    public CommonResponse<Void> unlockDaily(@PathVariable Long id) {
        attendanceDailyService.unlockDaily(id);
        return CommonResponse.success(null);
    }
}
