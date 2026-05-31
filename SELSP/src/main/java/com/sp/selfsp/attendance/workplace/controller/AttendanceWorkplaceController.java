package com.sp.selfsp.attendance.workplace.controller;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.workplace.service.AttendanceWorkplaceService;
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
@RequestMapping("/api/attendance/workplaces")
public class AttendanceWorkplaceController {

    private final AttendanceWorkplaceService attendanceWorkplaceService;

    public AttendanceWorkplaceController(AttendanceWorkplaceService attendanceWorkplaceService) {
        this.attendanceWorkplaceService = attendanceWorkplaceService;
    }

    @GetMapping
    public CommonResponse<List<AttendanceOut.WorkplaceOut>> listWorkplaces() {
        return CommonResponse.success(attendanceWorkplaceService.listWorkplaces());
    }

    @PostMapping
    public CommonResponse<AttendanceOut.WorkplaceOut> createWorkplace(@RequestBody AttendanceIn.WorkplaceSaveIn saveIn) {
        return CommonResponse.success(attendanceWorkplaceService.createWorkplace(saveIn));
    }

    @PutMapping("/{id}")
    public CommonResponse<AttendanceOut.WorkplaceOut> updateWorkplace(@PathVariable Long id, @RequestBody AttendanceIn.WorkplaceSaveIn saveIn) {
        return CommonResponse.success(attendanceWorkplaceService.updateWorkplace(id, saveIn));
    }

    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteWorkplace(@PathVariable Long id) {
        attendanceWorkplaceService.deleteWorkplace(id);
        return CommonResponse.success(null);
    }
}

