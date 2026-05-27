package com.sp.selfsp.attendance.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sp.selfsp.attendance.schedule.dao.AttendanceScheduleDao;
import com.sp.selfsp.attendance.schedule.domain.out.AttendanceScheduleOut;
import com.sp.selfsp.attendance.support.AttendanceMapperIntegrationSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AttendanceScheduleMapperTest extends AttendanceMapperIntegrationSupport {

    @Autowired
    private AttendanceScheduleDao attendanceScheduleDao;

    /**
     * 测试目的：验证shouldReadScheduleRangeAndDeleteByEmployeeScope场景。
     */
    @Test
    void shouldReadScheduleRangeAndDeleteByEmployeeScope() {
        attendanceScheduleDao.insert(
            TENANT_ID,
            1L,
            LocalDate.of(2026, 6, 1),
            1L,
            LocalDateTime.of(2026, 6, 1, 9, 0),
            LocalDateTime.of(2026, 6, 1, 18, 0),
            60,
            "WORKDAY",
            "first"
        );
        attendanceScheduleDao.insert(
            TENANT_ID,
            1L,
            LocalDate.of(2026, 6, 2),
            3L,
            LocalDateTime.of(2026, 6, 2, 22, 0),
            LocalDateTime.of(2026, 6, 3, 7, 0),
            60,
            "WORKDAY",
            "second"
        );

        List<AttendanceScheduleOut.ScheduleItemOut> scheduleItems = attendanceScheduleDao.selectScheduleItems(
            TENANT_ID,
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            List.of(1L)
        );

        assertEquals(2, scheduleItems.size());
        assertEquals("EARLY", scheduleItems.get(0).getTemplateCode());
        assertEquals("NIGHT", scheduleItems.get(1).getTemplateCode());

        attendanceScheduleDao.deleteRange(TENANT_ID, List.of(1L), LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 2));

        List<AttendanceScheduleOut.ScheduleItemOut> remainingItems = attendanceScheduleDao.selectScheduleItems(
            TENANT_ID,
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            List.of(1L)
        );
        assertEquals(1, remainingItems.size());
        assertEquals(LocalDate.of(2026, 6, 1), remainingItems.get(0).getWorkDate());
    }
}
