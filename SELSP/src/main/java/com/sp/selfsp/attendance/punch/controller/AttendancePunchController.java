package com.sp.selfsp.attendance.punch.controller;

import com.sp.selfsp.attendance.punch.domain.in.AttendancePunchIn;
import com.sp.selfsp.attendance.punch.domain.out.AttendancePunchOut;
import com.sp.selfsp.attendance.punch.service.AttendancePunchService;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第三阶段打卡控制器。
 */
@RestController
@RequestMapping("/api/attendance/punch")
public class AttendancePunchController {

    private final AttendancePunchService attendancePunchService;

    public AttendancePunchController(AttendancePunchService attendancePunchService) {
        this.attendancePunchService = attendancePunchService;
    }

    @GetMapping("/logs")
    public CommonResponse<AttendancePunchOut.PunchLogListOut> listPunchLogs(
        @RequestParam(required = false) String dateFrom,
        @RequestParam(required = false) String dateTo,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) String sourceSystem,
        @RequestParam(required = false) String processStatus,
        @RequestParam(required = false) String punchType,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        // 组装查询对象，统一传给第三阶段服务层处理过滤和分页口径。
        AttendancePunchIn.PunchLogQueryIn queryIn = new AttendancePunchIn.PunchLogQueryIn();
        queryIn.setDateFrom(dateFrom);
        queryIn.setDateTo(dateTo);
        queryIn.setEmployeeKeyword(employeeKeyword);
        queryIn.setSourceSystem(sourceSystem);
        queryIn.setProcessStatus(processStatus);
        queryIn.setPunchType(punchType);
        queryIn.setPage(page);
        queryIn.setPageSize(pageSize);
        return CommonResponse.success(attendancePunchService.listPunchLogs(queryIn));
    }

    @GetMapping("/logs/{id}")
    public CommonResponse<AttendancePunchOut.PunchLogDetailOut> getPunchLogDetail(@PathVariable Long id) {
        return CommonResponse.success(attendancePunchService.getPunchLogDetail(id));
    }

    @PostMapping("/manual")
    public CommonResponse<AttendancePunchOut.PunchManualResultOut> createManualPunch(@RequestBody AttendancePunchIn.PunchManualSaveIn saveIn) {
        return CommonResponse.success(attendancePunchService.createManualPunch(saveIn));
    }

    @PostMapping("/import-csv/preview")
    public CommonResponse<AttendancePunchOut.PunchImportPreviewOut> previewImport(@RequestBody AttendancePunchIn.PunchImportIn saveIn) {
        return CommonResponse.success(attendancePunchService.previewImport(saveIn));
    }

    @PostMapping("/import-csv")
    public CommonResponse<AttendancePunchOut.PunchImportResultOut> importCsv(@RequestBody AttendancePunchIn.PunchImportIn saveIn) {
        return CommonResponse.success(attendancePunchService.importCsv(saveIn));
    }

    @PostMapping("/webhook")
    public CommonResponse<AttendancePunchOut.PunchManualResultOut> receiveWebhook(@RequestBody AttendancePunchIn.PunchWebhookIn saveIn) {
        return CommonResponse.success(attendancePunchService.receiveWebhook(saveIn));
    }

    @PostMapping("/logs/{id}/bind-employee")
    public CommonResponse<AttendancePunchOut.PunchLogDetailOut> bindEmployee(
        @PathVariable Long id,
        @RequestBody AttendancePunchIn.PunchBindEmployeeIn saveIn
    ) {
        return CommonResponse.success(attendancePunchService.bindEmployee(id, saveIn));
    }

    @PostMapping("/logs/{id}/ignore")
    public CommonResponse<AttendancePunchOut.PunchLogDetailOut> ignoreLog(
        @PathVariable Long id,
        @RequestBody(required = false) AttendancePunchIn.PunchIgnoreIn saveIn
    ) {
        return CommonResponse.success(attendancePunchService.ignoreLog(id, saveIn));
    }

    @PostMapping("/logs/{id}/reprocess")
    public CommonResponse<AttendancePunchOut.PunchLogDetailOut> reprocessLog(@PathVariable Long id) {
        return CommonResponse.success(attendancePunchService.reprocessLog(id));
    }
}
