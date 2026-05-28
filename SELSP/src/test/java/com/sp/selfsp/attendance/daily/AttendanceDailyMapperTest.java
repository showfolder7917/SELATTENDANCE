package com.sp.selfsp.attendance.daily;

import com.sp.selfsp.attendance.daily.dao.AttendanceDailyDao;
import com.sp.selfsp.attendance.daily.domain.in.AttendanceDailyIn;
import com.sp.selfsp.attendance.daily.domain.out.AttendanceDailyOut;
import com.sp.selfsp.attendance.support.AttendanceMapperIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttendanceDailyMapperTest extends AttendanceMapperIntegrationSupport {

    @Autowired
    private AttendanceDailyDao attendanceDailyDao;

    /**
     * 测试目的：验证第四阶段 mapper 能读取计算目标、排班快照和打卡快照。
     */
    @Test
    void shouldReadDailyTargetsSchedulesAndPunches() {
        // 用整月查询条件读取第四阶段计算目标，确保排班日和无排班打卡日都会进入候选集合。
        AttendanceDailyIn.DailyQueryIn queryIn = new AttendanceDailyIn.DailyQueryIn();
        queryIn.setStartDate("2026-05-01");
        queryIn.setEndDate("2026-05-31");
        List<Map<String, Object>> targets = attendanceDailyDao.selectCalculationTargets(TENANT_ID, queryIn);
        assertEquals(21, targets.size());

        // 指定一个有完整上下班卡的日期，确认排班快照和打卡快照都能正确命中。
        AttendanceDailyOut.ScheduleSnapshotOut schedule = attendanceDailyDao.selectScheduleSnapshot(
            TENANT_ID,
            1L,
            LocalDate.of(2026, 5, 2)
        );
        assertNotNull(schedule);
        assertEquals("WORKDAY", schedule.getWorkDayType());
        assertEquals(3002L, schedule.getShiftScheduleId());

        List<AttendanceDailyOut.PunchSnapshotOut> punches = attendanceDailyDao.selectProcessedPunches(
            TENANT_ID,
            1L,
            LocalDate.of(2026, 5, 2)
        );
        assertEquals(4, punches.size());
        assertEquals("CLOCK_IN", punches.get(0).getPunchType());
        assertEquals("CLOCK_OUT", punches.get(3).getPunchType());

        // 无排班但有打卡的样本日也应该能从打卡表进入候选集合。
        assertTrue(targets.stream().anyMatch(item -> {
            Object workDate = item.containsKey("workDate") ? item.get("workDate") : item.get("WORKDATE");
            return Objects.equals("2026-05-18", String.valueOf(workDate));
        }));
    }
}
