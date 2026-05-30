package com.sp.selfsp.attendance.monthly.controller;

import com.sp.selfsp.attendance.monthly.domain.in.AttendanceMonthlyIn;
import com.sp.selfsp.attendance.monthly.domain.out.AttendanceMonthlyOut;
import com.sp.selfsp.attendance.monthly.service.AttendanceMonthlyService;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第六阶段月次汇总与月结控制器。
 */
@RestController
@RequestMapping("/api/attendance/monthly")
public class AttendanceMonthlyController {

    private final AttendanceMonthlyService attendanceMonthlyService;

    // 注入第六阶段服务实现，统一承接月次列表、详情、重算、月结和反结动作。
    public AttendanceMonthlyController(AttendanceMonthlyService attendanceMonthlyService) {
        this.attendanceMonthlyService = attendanceMonthlyService;
    }

    @GetMapping
    public CommonResponse<AttendanceMonthlyOut.MonthlyListOut> listMonthly(
        @RequestParam(required = false) String yearMonth,
        @RequestParam(required = false) Long workplaceId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) String closeStatus,
        @RequestParam(required = false) Boolean blockedOnly,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        AttendanceMonthlyIn.MonthlyQueryIn queryIn = new AttendanceMonthlyIn.MonthlyQueryIn();
        queryIn.setYearMonth(yearMonth);
        queryIn.setWorkplaceId(workplaceId);
        queryIn.setDepartmentId(departmentId);
        queryIn.setEmployeeKeyword(employeeKeyword);
        queryIn.setCloseStatus(closeStatus);
        queryIn.setBlockedOnly(blockedOnly);
        queryIn.setPage(page);
        queryIn.setPageSize(pageSize);
        return CommonResponse.success(attendanceMonthlyService.listMonthly(queryIn));
    }

    @GetMapping("/{id}")
    public CommonResponse<AttendanceMonthlyOut.MonthlyDetailOut> getMonthlyDetail(@PathVariable Long id) {
        return CommonResponse.success(attendanceMonthlyService.getMonthlyDetail(id));
    }

    @PostMapping("/recalculate")
    public CommonResponse<AttendanceMonthlyOut.MonthlyRecalculateResultOut> recalculateMonthly(
        @RequestBody AttendanceMonthlyIn.MonthlyRecalculateIn recalculateIn
    ) {
        return CommonResponse.success(attendanceMonthlyService.recalculateMonthly(recalculateIn));
    }

    @PostMapping("/close")
    public CommonResponse<AttendanceMonthlyOut.MonthlyCloseResultOut> closeMonthly(
        @RequestBody AttendanceMonthlyIn.MonthlyCloseIn closeIn
    ) {
        return CommonResponse.success(attendanceMonthlyService.closeMonthly(closeIn));
    }

    @PostMapping("/reopen")
    public CommonResponse<Void> reopenMonthly(@RequestBody AttendanceMonthlyIn.MonthlyReopenIn reopenIn) {
        attendanceMonthlyService.reopenMonthly(reopenIn);
        return CommonResponse.success(null);
    }

    @PostMapping("/export")
    public CommonResponse<AttendanceMonthlyOut.MonthlyExportOut> exportMonthly(
        @RequestBody AttendanceMonthlyIn.MonthlyQueryIn queryIn
    ) {
        return CommonResponse.success(attendanceMonthlyService.exportMonthly(queryIn));
    }
}
