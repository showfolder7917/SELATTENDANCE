package com.sp.selfsp.attendance.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.tenant.dao.AttendanceTenantDao;
import com.sp.selfsp.attendance.tenant.service.AttendanceTenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceTenantServiceBoundaryTest {

    @Autowired
    private AttendanceTenantService attendanceTenantService;

    @Autowired
    private AttendanceTenantDao attendanceTenantDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试目的：验证shouldInsertTenantWhenCurrentTenantRowIsMissing场景。
     */
    @Test
    void shouldInsertTenantWhenCurrentTenantRowIsMissing() {
        jdbcTemplate.update("DELETE FROM tenant WHERE id = 1");

        AttendanceOut.TenantOut tenantOut = attendanceTenantService.saveTenant(tenantSaveIn("NEW_TENANT", "Inserted Tenant", ""));

        assertNotNull(tenantOut);
        assertEquals("NEW_TENANT", tenantOut.getTenantCode());
        assertEquals("Asia/Tokyo", tenantOut.getTimezone());
        assertEquals(1, attendanceTenantDao.countById(1L));
    }

    /**
     * 测试目的：验证shouldUpdateExistingTenantAndRejectMissingTenantCode场景。
     */
    @Test
    void shouldUpdateExistingTenantAndRejectMissingTenantCode() {
        AttendanceOut.TenantOut updated = attendanceTenantService.saveTenant(tenantSaveIn("TENANT_DEMO", "Updated Tenant", "Asia/Seoul"));

        assertEquals("Updated Tenant", updated.getTenantName());
        assertEquals("Asia/Seoul", updated.getTimezone());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceTenantService.saveTenant(tenantSaveIn("", "Broken Tenant", "Asia/Tokyo"))
        );
        assertEquals("tenantCode 不能为空", exception.getMessage());
    }

    /**
     * 辅助目的：为tenantSaveIn提供测试支撑。
     */
    private AttendanceIn.TenantSaveIn tenantSaveIn(String tenantCode, String tenantName, String timezone) {
        AttendanceIn.TenantSaveIn saveIn = new AttendanceIn.TenantSaveIn();
        saveIn.setTenantCode(tenantCode);
        saveIn.setTenantName(tenantName);
        saveIn.setTimezone(timezone);
        return saveIn;
    }
}
