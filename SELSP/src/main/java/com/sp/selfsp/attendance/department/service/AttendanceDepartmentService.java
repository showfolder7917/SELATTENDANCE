package com.sp.selfsp.attendance.department.service;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.util.List;

// 定义 考勤部门服务，承接当前文件对应的业务职责。
public interface AttendanceDepartmentService {

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    List<AttendanceOut.DepartmentOut> listDepartments();

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.DepartmentOut createDepartment(AttendanceIn.DepartmentSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.DepartmentOut updateDepartment(Long id, AttendanceIn.DepartmentSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    void deleteDepartment(Long id);
}

