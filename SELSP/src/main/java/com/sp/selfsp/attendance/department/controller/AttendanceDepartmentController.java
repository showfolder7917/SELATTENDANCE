package com.sp.selfsp.attendance.department.controller;

import com.sp.selfsp.attendance.department.service.AttendanceDepartmentService;
import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
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
@RequestMapping("/api/attendance/departments")
public class AttendanceDepartmentController {

    private final AttendanceDepartmentService attendanceDepartmentService;

    public AttendanceDepartmentController(AttendanceDepartmentService attendanceDepartmentService) {
        this.attendanceDepartmentService = attendanceDepartmentService;
    }

    @GetMapping
    public CommonResponse<List<AttendanceOut.DepartmentOut>> listDepartments() {
        return CommonResponse.success(attendanceDepartmentService.listDepartments());
    }

    @PostMapping
    public CommonResponse<AttendanceOut.DepartmentOut> createDepartment(@RequestBody AttendanceIn.DepartmentSaveIn saveIn) {
        return CommonResponse.success(attendanceDepartmentService.createDepartment(saveIn));
    }

    @PutMapping("/{id}")
    public CommonResponse<AttendanceOut.DepartmentOut> updateDepartment(@PathVariable Long id, @RequestBody AttendanceIn.DepartmentSaveIn saveIn) {
        return CommonResponse.success(attendanceDepartmentService.updateDepartment(id, saveIn));
    }

    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteDepartment(@PathVariable Long id) {
        attendanceDepartmentService.deleteDepartment(id);
        return CommonResponse.success(null);
    }
}
