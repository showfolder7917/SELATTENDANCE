package com.sp.selfsp.attendance.employee.service;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.util.List;

public interface AttendanceEmployeeService {

    List<AttendanceOut.EmployeeOut> listEmployees(AttendanceIn.EmployeeQueryIn queryIn);

    AttendanceOut.EmployeeOut createEmployee(AttendanceIn.EmployeeSaveIn saveIn);

    AttendanceOut.EmployeeOut updateEmployee(Long id, AttendanceIn.EmployeeSaveIn saveIn);

    void deleteEmployee(Long id);

    AttendanceOut.EmployeeOut bindExternalMapping(Long id, AttendanceIn.ExternalMappingSaveIn saveIn);

    AttendanceOut.EmployeeImportResultOut importEmployees(AttendanceIn.EmployeeImportIn saveIn);

    AttendanceOut.CsvExportOut exportEmployees();
}

