package com.sp.selfsp.attendance.workplace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.support.AttendanceMapperIntegrationSupport;
import com.sp.selfsp.attendance.workplace.dao.AttendanceWorkplaceDao;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AttendanceWorkplaceMapperTest extends AttendanceMapperIntegrationSupport {

    @Autowired
    private AttendanceWorkplaceDao attendanceWorkplaceDao;

    /**
     * 测试目的：验证shouldReadOrderedWorkplacesAndReferenceCounts场景。
     */
    @Test
    void shouldReadOrderedWorkplacesAndReferenceCounts() {
        List<AttendanceOut.WorkplaceOut> workplaces = attendanceWorkplaceDao.selectList(TENANT_ID);

        assertEquals(2, workplaces.size());
        assertEquals("TKY-HQ", workplaces.get(0).getWorkplaceCode());
        assertEquals("YKH-CLS", workplaces.get(1).getWorkplaceCode());
        assertEquals(1L, attendanceWorkplaceDao.countEmployeesByWorkplaceId(TENANT_ID, 1L));
        assertEquals(1L, attendanceWorkplaceDao.countDepartmentsByWorkplaceId(TENANT_ID, 1L));
    }
}
