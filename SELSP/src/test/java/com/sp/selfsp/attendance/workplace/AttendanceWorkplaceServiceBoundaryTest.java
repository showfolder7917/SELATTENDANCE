package com.sp.selfsp.attendance.workplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.employee.service.AttendanceEmployeeService;
import com.sp.selfsp.attendance.workplace.dao.AttendanceWorkplaceDao;
import com.sp.selfsp.attendance.workplace.service.AttendanceWorkplaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceWorkplaceServiceBoundaryTest {

    @Autowired
    private AttendanceWorkplaceService attendanceWorkplaceService;

    @Autowired
    private AttendanceWorkplaceDao attendanceWorkplaceDao;

    @Autowired
    private AttendanceEmployeeService attendanceEmployeeService;

    /**
     * 测试目的：验证shouldCreateTrimmedWorkplaceAndRejectMissingCode场景。
     */
    @Test
    void shouldCreateTrimmedWorkplaceAndRejectMissingCode() {
        AttendanceOut.WorkplaceOut workplaceOut = attendanceWorkplaceService.createWorkplace(workplaceSaveIn(" TMP-WP ", " Temporary Workplace ", " ", " 03-0000-0000 ", ""));

        assertEquals("TMP-WP", workplaceOut.getWorkplaceCode());
        assertEquals("ACTIVE", workplaceOut.getStatus());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceWorkplaceService.createWorkplace(workplaceSaveIn("", "Broken", "", "", "ACTIVE"))
        );
        assertEquals("workplaceCode 不能为空", exception.getMessage());
    }

    /**
     * 测试目的：验证shouldRejectDeleteWhenDepartmentsOrEmployeesStillReferenceWorkplace场景。
     */
    @Test
    void shouldRejectDeleteWhenDepartmentsOrEmployeesStillReferenceWorkplace() {
        IllegalArgumentException departmentException = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceWorkplaceService.deleteWorkplace(1L)
        );
        assertEquals("该事业所下仍有部门，无法删除", departmentException.getMessage());

        AttendanceOut.WorkplaceOut workplaceOut = attendanceWorkplaceService.createWorkplace(workplaceSaveIn("EMP-WP", "Employee Workplace", "", "", "ACTIVE"));
        attendanceEmployeeService.createEmployee(employeeSaveIn("E9001", workplaceOut.getId(), 1L));

        IllegalArgumentException employeeException = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceWorkplaceService.deleteWorkplace(workplaceOut.getId())
        );
        assertEquals("该事业所下仍有员工，无法删除", employeeException.getMessage());
    }

    /**
     * 测试目的：验证shouldDeleteWorkplaceWhenNoReferencesRemain场景。
     */
    @Test
    void shouldDeleteWorkplaceWhenNoReferencesRemain() {
        AttendanceOut.WorkplaceOut workplaceOut = attendanceWorkplaceService.createWorkplace(workplaceSaveIn("FREE-WP", "Free Workplace", "", "", "ACTIVE"));

        attendanceWorkplaceService.deleteWorkplace(workplaceOut.getId());

        assertNull(attendanceWorkplaceDao.selectById(1L, workplaceOut.getId()));
    }

    /**
     * 辅助目的：为workplaceSaveIn提供测试支撑。
     */
    private AttendanceIn.WorkplaceSaveIn workplaceSaveIn(String code, String name, String address, String phone, String status) {
        AttendanceIn.WorkplaceSaveIn saveIn = new AttendanceIn.WorkplaceSaveIn();
        saveIn.setWorkplaceCode(code);
        saveIn.setWorkplaceName(name);
        saveIn.setAddress(address);
        saveIn.setPhone(phone);
        saveIn.setStatus(status);
        return saveIn;
    }

    /**
     * 辅助目的：为employeeSaveIn提供测试支撑。
     */
    private AttendanceIn.EmployeeSaveIn employeeSaveIn(String employeeNo, Long workplaceId, Long departmentId) {
        AttendanceIn.EmployeeSaveIn saveIn = new AttendanceIn.EmployeeSaveIn();
        saveIn.setEmployeeNo(employeeNo);
        saveIn.setEmployeeName("Temp Employee");
        saveIn.setEmploymentType("FULL_TIME");
        saveIn.setWorkplaceId(workplaceId);
        saveIn.setDepartmentId(departmentId);
        return saveIn;
    }
}
