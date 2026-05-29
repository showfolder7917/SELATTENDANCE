package com.sp.selfsp.attendance.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sp.selfsp.attendance.schedule.domain.in.AttendanceScheduleIn;
import com.sp.selfsp.attendance.schedule.domain.out.AttendanceScheduleOut;
import com.sp.selfsp.attendance.schedule.service.AttendanceScheduleService;
import com.sp.selfsp.attendance.support.AttendanceServiceIntegrationSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AttendanceScheduleRegressionTest extends AttendanceServiceIntegrationSupport {

    @Autowired
    private AttendanceScheduleService attendanceScheduleService;

    /**
     * 测试目的：验证shouldRejectMovingScheduleIntoOccupiedEmployeeDateSlot场景。
     */
    @Test
    void shouldRejectMovingScheduleIntoOccupiedEmployeeDateSlot() {
        AttendanceScheduleOut.ScheduleItemOut firstSchedule = attendanceScheduleService.createSchedule(
            scheduleSaveIn(1L, LocalDate.of(2026, 6, 11), 1L, "slot one")
        );
        attendanceScheduleService.createSchedule(
            scheduleSaveIn(1L, LocalDate.of(2026, 6, 12), 2L, "slot two")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.updateSchedule(
                firstSchedule.getId(),
                scheduleSaveIn(1L, LocalDate.of(2026, 6, 12), 3L, "conflict move")
            )
        );
    }

    /**
     * 测试目的：验证shouldKeepCrossDayEndDateWhenCopyingNightShiftFromLastWeek场景。
     */
    @Test
    void shouldKeepCrossDayEndDateWhenCopyingNightShiftFromLastWeek() {
        attendanceScheduleService.createSchedule(
            scheduleSaveIn(1L, LocalDate.of(2026, 5, 25), 3L, "night source")
        );

        attendanceScheduleService.copyLastWeek(scheduleCopyIn(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 1), true));

        AttendanceScheduleOut.ScheduleBoardOut boardOut = attendanceScheduleService.getScheduleBoard(boardQueryIn("2026-06"));
        AttendanceScheduleOut.ScheduleItemOut copiedItem = boardOut.getScheduleItems().stream()
            .filter(item -> LocalDate.of(2026, 6, 1).equals(item.getWorkDate()))
            .findFirst()
            .orElseThrow();
        assertEquals(LocalDate.of(2026, 6, 2), copiedItem.getScheduledEndTime().toLocalDate());
    }

    /**
     * 测试辅助目的：构造单日排班入参，统一复用回归测试里的建数与更新动作。
     */
    private AttendanceScheduleIn.ScheduleSaveIn scheduleSaveIn(Long employeeId, LocalDate workDate, Long shiftTemplateId, String remark) {
        AttendanceScheduleIn.ScheduleSaveIn saveIn = new AttendanceScheduleIn.ScheduleSaveIn();
        saveIn.setEmployeeId(employeeId);
        saveIn.setWorkDate(workDate);
        saveIn.setShiftTemplateId(shiftTemplateId);
        saveIn.setRemark(remark);
        return saveIn;
    }

    /**
     * 测试辅助目的：构造复制排班入参，便于验证上周或上月复制逻辑。
     */
    private AttendanceScheduleIn.ScheduleCopyIn scheduleCopyIn(LocalDate startDate, LocalDate endDate, Boolean overwriteExisting) {
        AttendanceScheduleIn.ScheduleCopyIn saveIn = new AttendanceScheduleIn.ScheduleCopyIn();
        saveIn.setEmployeeIds(java.util.List.of(1L));
        saveIn.setStartDate(startDate);
        saveIn.setEndDate(endDate);
        saveIn.setOverwriteExisting(overwriteExisting);
        return saveIn;
    }

    /**
     * 测试辅助目的：构造排班看板查询条件，便于验证复制后页面读到的看板结果。
     */
    private AttendanceScheduleIn.ScheduleBoardQueryIn boardQueryIn(String month) {
        AttendanceScheduleIn.ScheduleBoardQueryIn queryIn = new AttendanceScheduleIn.ScheduleBoardQueryIn();
        queryIn.setMonth(month);
        return queryIn;
    }
}
