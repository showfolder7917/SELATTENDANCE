package com.sp.selfsp.attendance.shifttemplate.controller;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.shifttemplate.service.AttendanceShiftTemplateService;
import com.sp.selfsp.common.util.CommonResponse;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/shift-templates")
public class AttendanceShiftTemplateController {

    private final AttendanceShiftTemplateService attendanceShiftTemplateService;

    public AttendanceShiftTemplateController(AttendanceShiftTemplateService attendanceShiftTemplateService) {
        this.attendanceShiftTemplateService = attendanceShiftTemplateService;
    }

    @GetMapping
    public CommonResponse<List<AttendanceOut.ShiftTemplateOut>> listShiftTemplates() {
        return CommonResponse.success(attendanceShiftTemplateService.listShiftTemplates());
    }

    @PostMapping
    public CommonResponse<AttendanceOut.ShiftTemplateOut> createShiftTemplate(@RequestBody AttendanceIn.ShiftTemplateSaveIn saveIn) {
        return CommonResponse.success(attendanceShiftTemplateService.createShiftTemplate(saveIn));
    }

    @PutMapping("/{id}")
    public CommonResponse<AttendanceOut.ShiftTemplateOut> updateShiftTemplate(@PathVariable Long id, @RequestBody AttendanceIn.ShiftTemplateSaveIn saveIn) {
        return CommonResponse.success(attendanceShiftTemplateService.updateShiftTemplate(id, saveIn));
    }

    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteShiftTemplate(@PathVariable Long id) {
        attendanceShiftTemplateService.deleteShiftTemplate(id);
        return CommonResponse.success(null);
    }

    @PostMapping("/recommended")
    public CommonResponse<List<AttendanceOut.ShiftTemplateOut>> generateRecommendedShiftTemplates() {
        return CommonResponse.success(attendanceShiftTemplateService.generateRecommendedShiftTemplates());
    }
}
