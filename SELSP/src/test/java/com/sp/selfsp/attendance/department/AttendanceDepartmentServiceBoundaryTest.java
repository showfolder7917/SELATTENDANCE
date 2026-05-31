package com.sp.selfsp.attendance.department;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.department.dao.AttendanceDepartmentDao;
import com.sp.selfsp.attendance.department.service.AttendanceDepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceDepartmentServiceBoundaryTest {

    @Autowired
    private AttendanceDepartmentService attendanceDepartmentService;

    @Autowired
    private AttendanceDepartmentDao attendanceDepartmentDao;

    /**
     * 测试目的：验证shouldCreateDepartmentWithDefaultsAndRejectMissingWorkplace场景。
     */
    @Test
    void shouldCreateDepartmentWithDefaultsAndRejectMissingWorkplace() {
        AttendanceOut.DepartmentOut departmentOut = attendanceDepartmentService.createDepartment(departmentSaveIn(1L, "TMP-DEP", "Temp Department", null, ""));

        assertEquals("TMP-DEP", departmentOut.getDepartmentCode());
        assertEquals(0, departmentOut.getSortOrder());
        assertEquals("ACTIVE", departmentOut.getStatus());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceDepartmentService.createDepartment(departmentSaveIn(999L, "BAD-DEP", "Broken Department", 1, "ACTIVE"))
        );
        assertEquals("事业所不存在，id=999", exception.getMessage());
    }

    /**
     * 测试目的：验证shouldRejectDeleteWhenEmployeesStillReferenceDepartmentAndDeleteIsolatedDepartment场景。
     */
    @Test
    void shouldRejectDeleteWhenEmployeesStillReferenceDepartmentAndDeleteIsolatedDepartment() {
        IllegalArgumentException occupiedException = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceDepartmentService.deleteDepartment(1L)
        );
        assertEquals("该部门下仍有员工，无法删除", occupiedException.getMessage());

        AttendanceOut.DepartmentOut isolated = attendanceDepartmentService.createDepartment(departmentSaveIn(1L, "FREE-DEP", "Free Department", 2, "ACTIVE"));

        attendanceDepartmentService.deleteDepartment(isolated.getId());

        assertNull(attendanceDepartmentDao.selectById(1L, isolated.getId()));
    }

    /**
     * 测试辅助目的：构造部门保存入参，统一复用部门新增、更新和删除前置场景。
     */
    private AttendanceIn.DepartmentSaveIn departmentSaveIn(Long workplaceId, String code, String name, Integer sortOrder, String status) {
        AttendanceIn.DepartmentSaveIn saveIn = new AttendanceIn.DepartmentSaveIn();
        saveIn.setWorkplaceId(workplaceId);
        saveIn.setDepartmentCode(code);
        saveIn.setDepartmentName(name);
        saveIn.setSortOrder(sortOrder);
        saveIn.setStatus(status);
        return saveIn;
    }
}
