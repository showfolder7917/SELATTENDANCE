package com.sp.selfsp.attendance.department;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.department.dao.AttendanceDepartmentDao;
import com.sp.selfsp.attendance.support.AttendanceMapperIntegrationSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AttendanceDepartmentMapperTest extends AttendanceMapperIntegrationSupport {

    @Autowired
    private AttendanceDepartmentDao attendanceDepartmentDao;

    /**
     * 测试目的：验证shouldReadDepartmentJoinFieldsAndEmployeeCount场景。
     */
    @Test
    void shouldReadDepartmentJoinFieldsAndEmployeeCount() {
        List<AttendanceOut.DepartmentOut> departments = attendanceDepartmentDao.selectList(TENANT_ID);
        AttendanceOut.DepartmentOut adminDepartment = attendanceDepartmentDao.selectByCode(TENANT_ID, "ADMIN");

        assertEquals(2, departments.size());
        assertEquals("东京本部", adminDepartment.getWorkplaceName());
        assertEquals(1L, attendanceDepartmentDao.countEmployeesByDepartmentId(TENANT_ID, 1L));
    }
}
