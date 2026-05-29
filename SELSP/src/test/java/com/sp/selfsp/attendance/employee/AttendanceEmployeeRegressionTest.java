package com.sp.selfsp.attendance.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.employee.dao.AttendanceEmployeeDao;
import com.sp.selfsp.attendance.employee.service.AttendanceEmployeeService;
import com.sp.selfsp.attendance.support.AttendanceServiceIntegrationSupport;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class AttendanceEmployeeRegressionTest extends AttendanceServiceIntegrationSupport {

    @Autowired
    private AttendanceEmployeeService attendanceEmployeeService;

    @Autowired
    private AttendanceEmployeeDao attendanceEmployeeDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试目的：验证shouldKeepSingleWorkRuleWhenUpdatingEmployeeInPlace场景。
     */
    @Test
    void shouldKeepSingleWorkRuleWhenUpdatingEmployeeInPlace() {
        AttendanceIn.EmployeeSaveIn createIn = employeeSaveIn("E3001", "回归员工", "FULL_TIME", LocalDate.of(2026, 5, 1));
        AttendanceOut.EmployeeOut created = attendanceEmployeeService.createEmployee(createIn);

        AttendanceIn.EmployeeSaveIn updateIn = employeeSaveIn("E3001", "回归员工-更新", "PART_TIME", null);
        attendanceEmployeeService.updateEmployee(created.getId(), updateIn);

        assertEquals(1, attendanceEmployeeDao.countWorkRuleByEmployeeId(TENANT_ID, created.getId()));
        Map<String, Object> workRule = jdbcTemplate.queryForMap(
            "SELECT work_rule_type, standard_daily_minutes FROM employee_work_rule WHERE tenant_id = 1 AND employee_id = ?",
            created.getId()
        );
        assertEquals("PART_TIME", workRule.get("work_rule_type"));
        assertEquals(300, ((Number) workRule.get("standard_daily_minutes")).intValue());
    }

    /**
     * 测试辅助目的：构造员工保存入参，便于回归测试快速生成可重复更新的员工数据。
     */
    private AttendanceIn.EmployeeSaveIn employeeSaveIn(String employeeNo, String employeeName, String employmentType, LocalDate hireDate) {
        AttendanceIn.EmployeeSaveIn saveIn = new AttendanceIn.EmployeeSaveIn();
        saveIn.setEmployeeNo(employeeNo);
        saveIn.setEmployeeName(employeeName);
        saveIn.setEmploymentType(employmentType);
        saveIn.setWorkplaceId(1L);
        saveIn.setDepartmentId(1L);
        saveIn.setHireDate(hireDate);
        saveIn.setStatus("ACTIVE");
        return saveIn;
    }
}
