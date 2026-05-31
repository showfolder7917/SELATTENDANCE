package com.sp.selfsp.attendance.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.support.AttendanceMapperIntegrationSupport;
import com.sp.selfsp.attendance.tenant.dao.AttendanceTenantDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AttendanceTenantMapperTest extends AttendanceMapperIntegrationSupport {

    @Autowired
    private AttendanceTenantDao attendanceTenantDao;

    /**
     * 测试目的：验证shouldReadSeededTenantSnapshot场景。
     */
    @Test
    void shouldReadSeededTenantSnapshot() {
        AttendanceOut.TenantOut tenantOut = attendanceTenantDao.selectCurrentTenant(TENANT_ID);

        assertEquals("TENANT_DEMO", tenantOut.getTenantCode());
        assertEquals("Asia/Tokyo", tenantOut.getTimezone());
        assertEquals(1, attendanceTenantDao.countById(TENANT_ID));
    }

    /**
     * 测试目的：验证shouldUpdateCurrentTenantThroughMapper场景。
     */
    @Test
    void shouldUpdateCurrentTenantThroughMapper() {
        AttendanceIn.TenantSaveIn saveIn = tenantSaveIn("TENANT_DEMO", "测试租户", "Asia/Shanghai");

        attendanceTenantDao.updateCurrentTenant(TENANT_ID, saveIn);

        AttendanceOut.TenantOut tenantOut = attendanceTenantDao.selectCurrentTenant(TENANT_ID);
        assertEquals("测试租户", tenantOut.getTenantName());
        assertEquals("Asia/Shanghai", tenantOut.getTimezone());
    }

    /**
     * 测试辅助目的：构造租户保存入参，便于 mapper 测试直接覆盖租户更新字段。
     */
    private AttendanceIn.TenantSaveIn tenantSaveIn(String tenantCode, String tenantName, String timezone) {
        AttendanceIn.TenantSaveIn saveIn = new AttendanceIn.TenantSaveIn();
        saveIn.setTenantCode(tenantCode);
        saveIn.setTenantName(tenantName);
        saveIn.setTimezone(timezone);
        return saveIn;
    }
}
