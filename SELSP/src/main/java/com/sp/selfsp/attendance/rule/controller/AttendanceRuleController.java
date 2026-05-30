package com.sp.selfsp.attendance.rule.controller;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.rule.service.AttendanceRuleService;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/rules")
public class AttendanceRuleController {

    private final AttendanceRuleService attendanceRuleService;

    public AttendanceRuleController(AttendanceRuleService attendanceRuleService) {
        this.attendanceRuleService = attendanceRuleService;
    }

    @GetMapping
    public CommonResponse<AttendanceOut.RuleWorkbenchOut> loadWorkbench(
        @RequestParam(required = false) String yearMonth,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean activeOnly
    ) {
        // 查询参数统一落成工作台查询对象，保证规则清单、适用列表和预警看板共享同一口径。
        AttendanceIn.RuleWorkbenchQueryIn queryIn = new AttendanceIn.RuleWorkbenchQueryIn();
        queryIn.setYearMonth(yearMonth);
        queryIn.setKeyword(keyword);
        queryIn.setActiveOnly(activeOnly);
        return CommonResponse.success(attendanceRuleService.loadWorkbench(queryIn));
    }

    @PostMapping
    public CommonResponse<AttendanceOut.RuleOut> createRule(@RequestBody AttendanceIn.RuleSaveIn saveIn) {
        // 新增规则时直接把表单入参交给服务层做校验、规范化和保存。
        return CommonResponse.success(attendanceRuleService.createRule(saveIn));
    }

    @PutMapping("/{id}")
    public CommonResponse<AttendanceOut.RuleOut> updateRule(@PathVariable Long id, @RequestBody AttendanceIn.RuleSaveIn saveIn) {
        // 更新规则时用路径主键锁定目标规则，避免只靠请求体造成误覆盖。
        return CommonResponse.success(attendanceRuleService.updateRule(id, saveIn));
    }

    @PutMapping("/assignments/{employeeId}")
    public CommonResponse<AttendanceOut.EmployeeRuleAssignmentOut> assignRule(
        @PathVariable Long employeeId,
        @RequestBody AttendanceIn.RuleAssignmentSaveIn saveIn
    ) {
        // 员工适用单独拆接口，便于规则页右侧直接保存某个员工当前适用规则。
        return CommonResponse.success(attendanceRuleService.assignRule(employeeId, saveIn));
    }
}
