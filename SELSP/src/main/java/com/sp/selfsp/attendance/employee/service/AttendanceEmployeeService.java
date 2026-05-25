package com.sp.selfsp.attendance.employee.service;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.util.List;

// 定义 考勤员工服务，承接当前文件对应的业务职责。
public interface AttendanceEmployeeService {

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    List<AttendanceOut.EmployeeOut> listEmployees(AttendanceIn.EmployeeQueryIn queryIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.EmployeeOut createEmployee(AttendanceIn.EmployeeSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.EmployeeOut updateEmployee(Long id, AttendanceIn.EmployeeSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    void deleteEmployee(Long id);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.EmployeeOut bindExternalMapping(Long id, AttendanceIn.ExternalMappingSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.EmployeeImportResultOut importEmployees(AttendanceIn.EmployeeImportIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.CsvExportOut exportEmployees();
}

