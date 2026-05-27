package com.sp.selfsp.attendance.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.schedule.domain.in.AttendanceScheduleIn;
import com.sp.selfsp.attendance.bootstrap.service.AttendanceBootstrapService;
import com.sp.selfsp.attendance.schedule.service.AttendanceScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceBootstrapServiceBoundaryTest {

    @Autowired
    private AttendanceBootstrapService attendanceBootstrapService;

    @Autowired
    private AttendanceScheduleService attendanceScheduleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试目的：验证shouldRecommendScheduleThenPunchAsCountsAdvance场景。
     */
    @Test
    void shouldRecommendScheduleThenPunchAsCountsAdvance() {
        AttendanceOut.BootstrapSummaryOut initial = attendanceBootstrapService.getBootstrapSummary();

        assertNotNull(initial.getTenant());
        assertEquals("wizard.schedule", initial.getRecommendedNextAction());

        attendanceScheduleService.createSchedule(scheduleSaveIn(1L, 1L, "bootstrap seed"));

        AttendanceOut.BootstrapSummaryOut advanced = attendanceBootstrapService.getBootstrapSummary();
        assertEquals("wizard.punch", advanced.getRecommendedNextAction());
        assertEquals("COMPLETED", advanced.getSteps().stream().filter(step -> "schedule".equals(step.getStepCode())).findFirst().orElseThrow().getStatus());
    }

    /**
     * 测试目的：验证shouldLockScheduleStepWhenPrerequisiteCountsAreIncomplete场景。
     */
    @Test
    void shouldLockScheduleStepWhenPrerequisiteCountsAreIncomplete() {
        jdbcTemplate.update("DELETE FROM employee_work_rule WHERE tenant_id = 1");

        AttendanceOut.BootstrapSummaryOut summary = attendanceBootstrapService.getBootstrapSummary();

        assertEquals("LOCKED_NEXT_PHASE", summary.getSteps().stream().filter(step -> "schedule".equals(step.getStepCode())).findFirst().orElseThrow().getStatus());
        assertEquals("wizard.workRule", summary.getRecommendedNextAction());
    }

    /**
     * 辅助目的：为scheduleSaveIn提供测试支撑。
     */
    private AttendanceScheduleIn.ScheduleSaveIn scheduleSaveIn(Long employeeId, Long shiftTemplateId, String remark) {
        AttendanceScheduleIn.ScheduleSaveIn saveIn = new AttendanceScheduleIn.ScheduleSaveIn();
        saveIn.setEmployeeId(employeeId);
        saveIn.setWorkDate(java.time.LocalDate.of(2026, 6, 1));
        saveIn.setShiftTemplateId(shiftTemplateId);
        saveIn.setRemark(remark);
        return saveIn;
    }
}
