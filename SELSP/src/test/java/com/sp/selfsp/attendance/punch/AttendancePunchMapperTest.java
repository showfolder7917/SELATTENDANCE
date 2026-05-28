package com.sp.selfsp.attendance.punch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sp.selfsp.attendance.punch.dao.AttendancePunchDao;
import com.sp.selfsp.attendance.punch.domain.in.AttendancePunchIn;
import com.sp.selfsp.attendance.punch.domain.out.AttendancePunchOut;
import com.sp.selfsp.attendance.support.AttendanceMapperIntegrationSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AttendancePunchMapperTest extends AttendanceMapperIntegrationSupport {

    @Autowired
    private AttendancePunchDao attendancePunchDao;

    /**
     * 测试目的：验证shouldReadPunchLogsAndUpdateStatuses场景。
     */
    @Test
    void shouldReadPunchLogsAndUpdateStatuses() {
        AttendancePunchIn.PunchLogQueryIn queryIn = new AttendancePunchIn.PunchLogQueryIn();
        queryIn.setPage(1);
        queryIn.setPageSize(10);
        queryIn.setProcessStatus("UNMATCHED");

        List<AttendancePunchOut.PunchLogItemOut> unmatchedLogs = attendancePunchDao.selectLogs(TENANT_ID, queryIn, 0, 10);
        assertEquals(4, attendancePunchDao.countLogs(TENANT_ID, queryIn));
        assertEquals(4, unmatchedLogs.size());
        assertEquals("UNMATCHED", unmatchedLogs.get(0).getProcessStatus());

        attendancePunchDao.insertLog(
            TENANT_ID,
            1L,
            "KOT-0001",
            "MANUAL",
            "manual-map-test-1",
            LocalDateTime.of(2026, 5, 28, 9, 15),
            "CLOCK_IN",
            "manual-gate",
            "手動テスト端末",
            "{\"kind\":\"manual\"}",
            null,
            "PROCESSED",
            null,
            null
        );
        AttendancePunchOut.PunchLogDetailOut latestLog = attendancePunchDao.selectLatestLog(TENANT_ID);
        assertNotNull(latestLog);
        assertEquals("MANUAL", latestLog.getSourceSystem());

        attendancePunchDao.updateStatus(TENANT_ID, 1019L, "IGNORED", "手動確認済み");
        AttendancePunchOut.PunchLogDetailOut ignoredLog = attendancePunchDao.selectById(TENANT_ID, 1019L);
        assertEquals("IGNORED", ignoredLog.getProcessStatus());
        assertEquals("手動確認済み", ignoredLog.getErrorMessage());
    }
}
