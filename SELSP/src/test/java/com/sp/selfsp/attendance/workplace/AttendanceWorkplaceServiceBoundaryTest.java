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
        // 先创建带前后空格的事业所，验证服务层会在入库前统一裁剪输入值并补默认状态。
        AttendanceOut.WorkplaceOut workplaceOut = attendanceWorkplaceService.createWorkplace(workplaceSaveIn(" TMP-WP ", " Temporary Workplace ", " ", " 03-0000-0000 ", ""));

        assertEquals("TMP-WP", workplaceOut.getWorkplaceCode());
        assertEquals("ACTIVE", workplaceOut.getStatus());

        // 再提交空事业所编码，确认必填校验会阻止脏数据进入主数据表。
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
        // 先验证系统初始化事业所因为仍被部门引用，所以不能直接删除。
        IllegalArgumentException departmentException = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceWorkplaceService.deleteWorkplace(1L)
        );
        assertEquals("该事业所下仍有部门，无法删除", departmentException.getMessage());

        // 再制造一个被员工引用的新事业所，验证员工引用也会阻止删除。
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
        // 创建一个没有部门和员工引用的新事业所，作为可安全删除的目标数据。
        AttendanceOut.WorkplaceOut workplaceOut = attendanceWorkplaceService.createWorkplace(workplaceSaveIn("FREE-WP", "Free Workplace", "", "", "ACTIVE"));

        attendanceWorkplaceService.deleteWorkplace(workplaceOut.getId());

        // 删除后直接从 DAO 回读，确认数据库里已经没有残留记录。
        assertNull(attendanceWorkplaceDao.selectById(1L, workplaceOut.getId()));
    }

    /**
     * 测试辅助目的：构造事业所保存入参，统一复用边界测试中的基础主数据表单。
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
     * 测试辅助目的：构造员工保存入参，便于在事业所删除测试里快速制造员工引用关系。
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
