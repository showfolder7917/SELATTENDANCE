package com.sp.selfsp.attendance.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.employee.dao.AttendanceEmployeeDao;
import com.sp.selfsp.attendance.support.AttendanceMapperIntegrationSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AttendanceEmployeeMapperTest extends AttendanceMapperIntegrationSupport {

    @Autowired
    private AttendanceEmployeeDao attendanceEmployeeDao;

    /**
     * 测试目的：验证shouldFilterEmployeeListByKeywordAndStatus场景。
     */
    @Test
    void shouldFilterEmployeeListByKeywordAndStatus() {
        AttendanceIn.EmployeeQueryIn queryIn = new AttendanceIn.EmployeeQueryIn();
        queryIn.setKeyword("山田");
        queryIn.setStatus("ACTIVE");

        List<AttendanceOut.EmployeeOut> employees = attendanceEmployeeDao.selectList(TENANT_ID, queryIn);

        assertEquals(1, employees.size());
        assertEquals("E0001", employees.get(0).getEmployeeNo());
    }

    /**
     * 测试目的：验证shouldExposeExternalMappingColumnsAfterMapperWrite场景。
     */
    @Test
    void shouldExposeExternalMappingColumnsAfterMapperWrite() {
        AttendanceIn.ExternalMappingSaveIn saveIn = externalMappingSaveIn("KING_OF_TIME", "EXT-100", "NO-100", "ACTIVE");

        attendanceEmployeeDao.insertExternalMapping(TENANT_ID, 1L, saveIn);

        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectById(TENANT_ID, 1L);
        assertTrue(Boolean.TRUE.equals(employeeOut.getExternalMappingBound()));
        assertEquals("EXT-100", employeeOut.getExternalEmployeeId());
    }

    /**
     * 测试辅助目的：构造外部打卡映射入参，便于 mapper 测试验证映射列回读结果。
     */
    private AttendanceIn.ExternalMappingSaveIn externalMappingSaveIn(
        String sourceSystem,
        String externalEmployeeId,
        String externalEmployeeNo,
        String status
    ) {
        AttendanceIn.ExternalMappingSaveIn saveIn = new AttendanceIn.ExternalMappingSaveIn();
        saveIn.setSourceSystem(sourceSystem);
        saveIn.setExternalEmployeeId(externalEmployeeId);
        saveIn.setExternalEmployeeNo(externalEmployeeNo);
        saveIn.setStatus(status);
        return saveIn;
    }
}
