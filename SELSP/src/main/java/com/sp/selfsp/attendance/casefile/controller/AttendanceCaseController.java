package com.sp.selfsp.attendance.casefile.controller;

import com.sp.selfsp.attendance.casefile.domain.in.AttendanceCaseIn;
import com.sp.selfsp.attendance.casefile.domain.out.AttendanceCaseOut;
import com.sp.selfsp.attendance.casefile.service.AttendanceCaseService;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第五阶段异常处理与审批控制器。
 */
@RestController
@RequestMapping("/api/attendance/cases")
public class AttendanceCaseController {

    private final AttendanceCaseService attendanceCaseService;

    // 注入第五阶段服务实现，统一承接列表、详情、建单和审批动作。
    public AttendanceCaseController(AttendanceCaseService attendanceCaseService) {
        this.attendanceCaseService = attendanceCaseService;
    }

    @GetMapping
    public CommonResponse<AttendanceCaseOut.CaseListOut> listCases(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(required = false) Long workplaceId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) String caseStatus,
        @RequestParam(required = false) String handlingStatus,
        @RequestParam(required = false) Boolean mineOnly,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        // 把筛选条件收敛成统一查询对象，保证分页和统计使用相同口径。
        AttendanceCaseIn.CaseQueryIn queryIn = new AttendanceCaseIn.CaseQueryIn();
        queryIn.setStartDate(startDate);
        queryIn.setEndDate(endDate);
        queryIn.setWorkplaceId(workplaceId);
        queryIn.setDepartmentId(departmentId);
        queryIn.setEmployeeKeyword(employeeKeyword);
        queryIn.setCaseStatus(caseStatus);
        queryIn.setHandlingStatus(handlingStatus);
        queryIn.setMineOnly(mineOnly);
        queryIn.setPage(page);
        queryIn.setPageSize(pageSize);
        return CommonResponse.success(attendanceCaseService.listCases(queryIn));
    }

    @GetMapping("/{id}")
    public CommonResponse<AttendanceCaseOut.CaseDetailOut> getCaseDetail(@PathVariable Long id) {
        return CommonResponse.success(attendanceCaseService.getCaseDetail(id));
    }

    @PostMapping
    public CommonResponse<AttendanceCaseOut.CaseMutationOut> createCase(@RequestBody AttendanceCaseIn.CaseCreateIn createIn) {
        return CommonResponse.success(attendanceCaseService.createCase(createIn));
    }

    @PostMapping("/{id}/actions")
    public CommonResponse<AttendanceCaseOut.CaseMutationOut> applyAction(
        @PathVariable Long id,
        @RequestBody AttendanceCaseIn.CaseActionIn actionIn
    ) {
        return CommonResponse.success(attendanceCaseService.applyAction(id, actionIn));
    }

    @PostMapping("/batch-actions")
    public CommonResponse<AttendanceCaseOut.CaseMutationOut> batchApplyAction(@RequestBody AttendanceCaseIn.CaseBatchActionIn actionIn) {
        return CommonResponse.success(attendanceCaseService.batchApplyAction(actionIn));
    }
}
