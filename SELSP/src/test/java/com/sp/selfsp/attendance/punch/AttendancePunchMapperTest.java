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
        // 先构造“只看未匹配原始打卡”的查询条件，验证 mapper 的筛选条件是否生效。
        AttendancePunchIn.PunchLogQueryIn queryIn = new AttendancePunchIn.PunchLogQueryIn();
        queryIn.setPage(1);
        queryIn.setPageSize(10);
        queryIn.setProcessStatus("UNMATCHED");

        // 读取未匹配打卡列表，确认初始化数据集中未处理记录的数量和状态都正确。
        List<AttendancePunchOut.PunchLogItemOut> unmatchedLogs = attendancePunchDao.selectLogs(TENANT_ID, queryIn, 0, 10);
        assertEquals(4, attendancePunchDao.countLogs(TENANT_ID, queryIn));
        assertEquals(4, unmatchedLogs.size());
        assertEquals("UNMATCHED", unmatchedLogs.get(0).getProcessStatus());

        // 插入一条手工打卡记录，验证 mapper 能完整写入来源系统、设备和原始载荷字段。
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
        // 回读最新打卡，确认刚插入的记录已经成为最新一条并保留来源系统信息。
        AttendancePunchOut.PunchLogDetailOut latestLog = attendancePunchDao.selectLatestLog(TENANT_ID);
        assertNotNull(latestLog);
        assertEquals("MANUAL", latestLog.getSourceSystem());

        // 把现有打卡状态改成已忽略，验证状态更新和错误原因字段会一起持久化。
        attendancePunchDao.updateStatus(TENANT_ID, 1019L, "IGNORED", "手動確認済み");
        AttendancePunchOut.PunchLogDetailOut ignoredLog = attendancePunchDao.selectById(TENANT_ID, 1019L);
        assertEquals("IGNORED", ignoredLog.getProcessStatus());
        assertEquals("手動確認済み", ignoredLog.getErrorMessage());
    }
}
