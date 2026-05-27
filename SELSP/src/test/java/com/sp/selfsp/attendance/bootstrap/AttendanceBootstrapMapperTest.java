package com.sp.selfsp.attendance.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sp.selfsp.attendance.bootstrap.dao.AttendanceBootstrapDao;
import com.sp.selfsp.attendance.schedule.dao.AttendanceScheduleDao;
import com.sp.selfsp.attendance.support.AttendanceMapperIntegrationSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AttendanceBootstrapMapperTest extends AttendanceMapperIntegrationSupport {

    @Autowired
    private AttendanceBootstrapDao attendanceBootstrapDao;

    @Autowired
    private AttendanceScheduleDao attendanceScheduleDao;

    /**
     * 测试目的：验证shouldReadBootstrapCountsFromResetDataset场景。
     */
    @Test
    void shouldReadBootstrapCountsFromResetDataset() {
        Map<String, Object> counts = attendanceBootstrapDao.selectCounts(TENANT_ID);

        assertEquals(1L, readCount(counts, "tenantCount"));
        assertEquals(2L, readCount(counts, "workplaceCount"));
        assertEquals(2L, readCount(counts, "departmentCount"));
        assertEquals(1L, readCount(counts, "employeeCount"));
        assertEquals(1L, readCount(counts, "workRuleCount"));
        assertEquals(0L, readCount(counts, "scheduleCount"));
    }

    /**
     * 测试目的：验证shouldReflectInsertedScheduleInBootstrapCounts场景。
     */
    @Test
    void shouldReflectInsertedScheduleInBootstrapCounts() {
        attendanceScheduleDao.insert(
            TENANT_ID,
            1L,
            LocalDate.of(2026, 6, 1),
            1L,
            LocalDateTime.of(2026, 6, 1, 9, 0),
            LocalDateTime.of(2026, 6, 1, 18, 0),
            60,
            "WORKDAY",
            "mapper seed"
        );

        Map<String, Object> counts = attendanceBootstrapDao.selectCounts(TENANT_ID);

        assertEquals(1L, readCount(counts, "scheduleCount"));
    }

    /**
     * 辅助目的：为readCount提供测试支撑。
     */
    private long readCount(Map<String, Object> counts, String key) {
        Object value = counts.get(key);
        if (value == null) {
            value = counts.get(key.toUpperCase());
        }
        if (value == null) {
            value = counts.get(Character.toUpperCase(key.charAt(0)) + key.substring(1));
        }
        return ((Number) value).longValue();
    }
}
