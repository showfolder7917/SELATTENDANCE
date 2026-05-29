package com.sp.selfsp.attendance.department.service;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.util.List;

public interface AttendanceDepartmentService {

    List<AttendanceOut.DepartmentOut> listDepartments();

    AttendanceOut.DepartmentOut createDepartment(AttendanceIn.DepartmentSaveIn saveIn);

    AttendanceOut.DepartmentOut updateDepartment(Long id, AttendanceIn.DepartmentSaveIn saveIn);

    void deleteDepartment(Long id);
}

