package com.sp.selfsp.attendance.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sp.selfsp.attendance.employee.dao.AttendanceEmployeeDao;
import com.sp.selfsp.attendance.employee.service.AttendanceEmployeeService;
import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.schedule.dao.AttendanceScheduleDao;
import com.sp.selfsp.attendance.schedule.domain.in.AttendanceScheduleIn;
import com.sp.selfsp.attendance.schedule.domain.out.AttendanceScheduleOut;
import com.sp.selfsp.attendance.schedule.service.AttendanceScheduleService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceScheduleServiceBoundaryTest {

    @Autowired
    private AttendanceScheduleService attendanceScheduleService;

    @Autowired
    private AttendanceScheduleDao attendanceScheduleDao;

    @Autowired
    private AttendanceEmployeeDao attendanceEmployeeDao;

    @Autowired
    private AttendanceEmployeeService attendanceEmployeeService;

    private Long seededEmployeeId;

    private Long seededWorkplaceId;

    /**
     * 辅助目的：为setUpSeededEmployee提供测试支撑。
     */
    @BeforeEach
    void setUpSeededEmployee() {
        AttendanceIn.EmployeeQueryIn queryIn = new AttendanceIn.EmployeeQueryIn();
        queryIn.setStatus("ACTIVE");
        List<AttendanceOut.EmployeeOut> employeeList = attendanceEmployeeDao.selectList(1L, queryIn);
        assertEquals(1, employeeList.size());
        seededEmployeeId = employeeList.get(0).getId();
        seededWorkplaceId = employeeList.get(0).getWorkplaceId();
    }

    /**
     * 测试目的：验证shouldCreateAgainAsUpdateWhenSameEmployeeAndDateAlreadyExist场景。
     */
    @Test
    void shouldCreateAgainAsUpdateWhenSameEmployeeAndDateAlreadyExist() {
        AttendanceScheduleOut.ScheduleItemOut firstCreated = attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 10), 1L, "first create")
        );
        assertNotNull(firstCreated);
        assertNotNull(firstCreated.getId());

        AttendanceScheduleOut.ScheduleItemOut secondCreated = attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 10), 2L, "replace by duplicate create")
        );
        assertEquals(firstCreated.getId(), secondCreated.getId());
        assertEquals("LATE", secondCreated.getTemplateCode());
        assertEquals("replace by duplicate create", secondCreated.getRemark());
    }

    /**
     * 测试目的：验证shouldRejectUpdateWhenAnotherScheduleAlreadyUsesTargetEmployeeAndDate场景。
     */
    @Test
    void shouldRejectUpdateWhenAnotherScheduleAlreadyUsesTargetEmployeeAndDate() {
        AttendanceScheduleOut.ScheduleItemOut firstSchedule = attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 11), 1L, "slot one")
        );
        AttendanceScheduleOut.ScheduleItemOut secondSchedule = attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 12), 2L, "slot two")
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.updateSchedule(
                firstSchedule.getId(),
                scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 12), 3L, "conflict move")
            )
        );
        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
        assertEquals(
            secondSchedule.getId(),
            attendanceScheduleDao.selectByEmployeeAndDate(1L, seededEmployeeId, LocalDate.of(2026, 6, 12)).getId()
        );
    }

    /**
     * 测试目的：验证shouldFilterBoardByWorkplaceAndRejectMissingMonth场景。
     */
    @Test
    void shouldFilterBoardByWorkplaceAndRejectMissingMonth() {
        attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 13), 1L, "board filter seed")
        );

        AttendanceScheduleIn.ScheduleBoardQueryIn matchedQuery = new AttendanceScheduleIn.ScheduleBoardQueryIn();
        matchedQuery.setMonth("2026-06");
        matchedQuery.setWorkplaceId(seededWorkplaceId);
        AttendanceScheduleOut.ScheduleBoardOut matchedBoard = attendanceScheduleService.getScheduleBoard(matchedQuery);
        assertEquals(1, matchedBoard.getEmployeeRows().size());
        assertEquals(1, matchedBoard.getScheduleItems().size());

        AttendanceScheduleIn.ScheduleBoardQueryIn emptyQuery = new AttendanceScheduleIn.ScheduleBoardQueryIn();
        emptyQuery.setMonth("2026-06");
        emptyQuery.setWorkplaceId(999L);
        AttendanceScheduleOut.ScheduleBoardOut emptyBoard = attendanceScheduleService.getScheduleBoard(emptyQuery);
        assertTrue(emptyBoard.getEmployeeRows().isEmpty());
        assertTrue(emptyBoard.getScheduleItems().isEmpty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.getScheduleBoard(new AttendanceScheduleIn.ScheduleBoardQueryIn())
        );
        assertTrue(exception.getMessage().contains("month"));
    }

    /**
     * 测试目的：验证shouldHandleBatchAssignSkipOverwriteAndUpdateBranches场景。
     */
    @Test
    void shouldHandleBatchAssignSkipOverwriteAndUpdateBranches() {
        attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 14), 1L, "existing day")
        );

        AttendanceScheduleOut.ScheduleBatchResultOut skipResult = attendanceScheduleService.batchAssignSchedules(
            scheduleBatchAssignIn(List.of(seededEmployeeId), LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 16), 2L, true, false, "skip existing")
        );
        assertEquals(2, skipResult.getCreatedCount());
        assertEquals(1, skipResult.getSkippedCount());
        assertEquals(0, skipResult.getUpdatedCount());

        AttendanceScheduleOut.ScheduleBatchResultOut noOverwriteResult = attendanceScheduleService.batchAssignSchedules(
            scheduleBatchAssignIn(List.of(seededEmployeeId), LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 16), 3L, false, false, "do not overwrite")
        );
        assertEquals(0, noOverwriteResult.getCreatedCount());
        assertEquals(0, noOverwriteResult.getUpdatedCount());
        assertEquals(3, noOverwriteResult.getSkippedCount());

        AttendanceScheduleOut.ScheduleBatchResultOut overwriteResult = attendanceScheduleService.batchAssignSchedules(
            scheduleBatchAssignIn(List.of(seededEmployeeId), LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 16), 3L, false, true, "overwrite existing")
        );
        assertEquals(0, overwriteResult.getCreatedCount());
        assertEquals(3, overwriteResult.getUpdatedCount());
        assertEquals(0, overwriteResult.getSkippedCount());

        AttendanceScheduleOut.ScheduleItemOut updatedItem = attendanceScheduleDao.selectByEmployeeAndDate(1L, seededEmployeeId, LocalDate.of(2026, 6, 15));
        assertEquals("NIGHT", updatedItem.getTemplateCode());
        assertTrue(Boolean.TRUE.equals(updatedItem.getCrossDay()));
    }

    /**
     * 测试目的：验证shouldCopyLastWeekRespectOverwriteAndCrossDayTiming场景。
     */
    @Test
    void shouldCopyLastWeekRespectOverwriteAndCrossDayTiming() {
        attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 5, 25), 3L, "night source")
        );
        attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 1), 1L, "existing target")
        );

        AttendanceScheduleOut.ScheduleBatchResultOut skipResult = attendanceScheduleService.copyLastWeek(
            scheduleCopyIn(List.of(seededEmployeeId), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), false)
        );
        assertEquals(0, skipResult.getCreatedCount());
        assertEquals(2, skipResult.getSkippedCount());

        AttendanceScheduleOut.ScheduleBatchResultOut overwriteResult = attendanceScheduleService.copyLastWeek(
            scheduleCopyIn(List.of(seededEmployeeId), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), true)
        );
        assertEquals(1, overwriteResult.getUpdatedCount());
        assertEquals(1, overwriteResult.getSkippedCount());

        AttendanceScheduleOut.ScheduleItemOut copiedItem = attendanceScheduleDao.selectByEmployeeAndDate(1L, seededEmployeeId, LocalDate.of(2026, 6, 1));
        assertEquals("NIGHT", copiedItem.getTemplateCode());
        assertEquals(LocalDate.of(2026, 6, 1), copiedItem.getScheduledStartTime().toLocalDate());
        assertEquals(LocalDate.of(2026, 6, 2), copiedItem.getScheduledEndTime().toLocalDate());
    }

    /**
     * 测试目的：验证shouldCopyLastMonthCreateThenSkipMissingSources场景。
     */
    @Test
    void shouldCopyLastMonthCreateThenSkipMissingSources() {
        attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 5, 1), 3L, "month source")
        );

        AttendanceScheduleOut.ScheduleBatchResultOut result = attendanceScheduleService.copyLastMonth(
            scheduleCopyIn(List.of(seededEmployeeId), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), true)
        );
        assertEquals(1, result.getCreatedCount());
        assertEquals(1, result.getSkippedCount());

        AttendanceScheduleOut.ScheduleItemOut copiedItem = attendanceScheduleDao.selectByEmployeeAndDate(1L, seededEmployeeId, LocalDate.of(2026, 6, 1));
        assertNotNull(copiedItem);
        assertEquals("NIGHT", copiedItem.getTemplateCode());
    }

    /**
     * 测试目的：验证shouldRejectInvalidClearRangeAndRemovePersistedSchedules场景。
     */
    @Test
    void shouldRejectInvalidClearRangeAndRemovePersistedSchedules() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.clearSchedules(
                scheduleClearRangeIn(List.of(seededEmployeeId), LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 19))
            )
        );
        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());

        attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 19), 1L, "clear day one")
        );
        attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 20), 2L, "clear day two")
        );

        AttendanceScheduleOut.ScheduleBatchResultOut cleared = attendanceScheduleService.clearSchedules(
            scheduleClearRangeIn(List.of(seededEmployeeId), LocalDate.of(2026, 6, 19), LocalDate.of(2026, 6, 20))
        );
        assertEquals(0, cleared.getCreatedCount());
        assertEquals(0, cleared.getUpdatedCount());
        assertEquals(0, cleared.getSkippedCount());
        assertEquals(2, cleared.getAffectedDateCount());

        assertNull(attendanceScheduleDao.selectByEmployeeAndDate(1L, seededEmployeeId, LocalDate.of(2026, 6, 19)));
        assertNull(attendanceScheduleDao.selectByEmployeeAndDate(1L, seededEmployeeId, LocalDate.of(2026, 6, 20)));
    }

    /**
     * 测试目的：验证shouldRejectEmptyEmployeeListForBatchAssign场景。
     */
    @Test
    void shouldRejectEmptyEmployeeListForBatchAssign() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.batchAssignSchedules(
                scheduleBatchAssignIn(List.of(), LocalDate.of(2026, 6, 21), LocalDate.of(2026, 6, 22), 1L, false, true, "invalid batch")
            )
        );
        assertTrue(exception.getMessage().contains("employeeIds"));
    }

    /**
     * 测试目的：验证shouldMoveScheduleToAnotherEmployeeAndDateWhenUpdateChangesAssignment场景。
     */
    @Test
    void shouldMoveScheduleToAnotherEmployeeAndDateWhenUpdateChangesAssignment() {
        AttendanceOut.EmployeeOut secondEmployee = attendanceEmployeeService.createEmployee(
            employeeSaveIn("E2001", "Move Target", 1L, 1L)
        );
        AttendanceScheduleOut.ScheduleItemOut original = attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 22), 1L, "move origin")
        );

        AttendanceScheduleOut.ScheduleItemOut moved = attendanceScheduleService.updateSchedule(
            original.getId(),
            scheduleSaveIn(secondEmployee.getId(), LocalDate.of(2026, 6, 23), 2L, "moved schedule")
        );
        assertEquals(secondEmployee.getId(), moved.getEmployeeId());
        assertEquals(LocalDate.of(2026, 6, 23), moved.getWorkDate());
        assertEquals("LATE", moved.getTemplateCode());
        assertNull(attendanceScheduleDao.selectByEmployeeAndDate(1L, seededEmployeeId, LocalDate.of(2026, 6, 22)));
    }

    /**
     * 测试目的：验证shouldReturnOnlyEmployeesWithGapsWhenCheckingUnassignedSections场景。
     */
    @Test
    void shouldReturnOnlyEmployeesWithGapsWhenCheckingUnassignedSections() {
        AttendanceOut.EmployeeOut fullyAssignedEmployee = attendanceEmployeeService.createEmployee(
            employeeSaveIn("E2002", "Fully Assigned", 1L, 1L)
        );
        AttendanceScheduleOut.ScheduleBatchResultOut fullMonthAssign = attendanceScheduleService.batchAssignSchedules(
            scheduleBatchAssignIn(List.of(fullyAssignedEmployee.getId()), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 1L, false, true, "full month")
        );
        assertEquals(30, fullMonthAssign.getCreatedCount());

        attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 1), 2L, "single assigned day")
        );

        AttendanceScheduleIn.ScheduleBoardQueryIn boardQuery = new AttendanceScheduleIn.ScheduleBoardQueryIn();
        boardQuery.setMonth("2026-06");
        List<AttendanceScheduleOut.ScheduleUnassignedOut> unassignedRows = attendanceScheduleService.checkUnassignedSchedules(boardQuery);
        assertEquals(1, unassignedRows.size());
        assertEquals(seededEmployeeId, unassignedRows.get(0).getEmployeeId());
        assertEquals(29, unassignedRows.get(0).getUnassignedCount());

        boardQuery.setOnlyUnassigned(true);
        AttendanceScheduleOut.ScheduleBoardOut onlyUnassignedBoard = attendanceScheduleService.getScheduleBoard(boardQuery);
        assertEquals(1, onlyUnassignedBoard.getEmployeeRows().size());
        assertEquals(seededEmployeeId, onlyUnassignedBoard.getEmployeeRows().get(0).getEmployeeId());
        assertTrue(onlyUnassignedBoard.getScheduleItems().stream().allMatch(item -> seededEmployeeId.equals(item.getEmployeeId())));
    }

    /**
     * 测试目的：验证shouldExportSchedulesWithQuotedNamesAndBlankCells场景。
     */
    @Test
    void shouldExportSchedulesWithQuotedNamesAndBlankCells() {
        AttendanceOut.EmployeeOut quotedEmployee = attendanceEmployeeService.createEmployee(
            employeeSaveIn("E2003", "Quoted \"Name\"", 1L, 1L)
        );
        attendanceScheduleService.createSchedule(
            scheduleSaveIn(quotedEmployee.getId(), LocalDate.of(2026, 6, 2), 5L, "rest export")
        );

        AttendanceScheduleIn.ScheduleBoardQueryIn boardQuery = new AttendanceScheduleIn.ScheduleBoardQueryIn();
        boardQuery.setMonth("2026-06");
        AttendanceOut.CsvExportOut exportOut = attendanceScheduleService.exportSchedules(boardQuery);
        assertEquals("attendance_schedule_2026-06.csv", exportOut.getFileName());
        assertTrue(exportOut.getContent().contains("\"Quoted \"\"Name\"\"\""));
        assertTrue(exportOut.getContent().contains("E2003"));
    }

    /**
     * 测试目的：验证shouldCreateRestAndPaidLeaveSchedulesWithoutClockTimes场景。
     */
    @Test
    void shouldCreateRestAndPaidLeaveSchedulesWithoutClockTimes() {
        AttendanceScheduleOut.ScheduleItemOut restSchedule = attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 24), 5L, "rest day")
        );
        assertEquals("REST", restSchedule.getWorkDayType());
        assertNull(restSchedule.getScheduledStartTime());
        assertNull(restSchedule.getScheduledEndTime());

        AttendanceScheduleOut.ScheduleItemOut paidLeaveSchedule = attendanceScheduleService.createSchedule(
            scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 25), 6L, "paid leave")
        );
        assertEquals("PAID_LEAVE", paidLeaveSchedule.getWorkDayType());
        assertNull(paidLeaveSchedule.getScheduledStartTime());
        assertNull(paidLeaveSchedule.getScheduledEndTime());
    }

    /**
     * 测试目的：验证shouldRejectNullCommandsAndMissingScheduleReferences场景。
     */
    @Test
    void shouldRejectNullCommandsAndMissingScheduleReferences() {
        IllegalArgumentException nullCreate = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.createSchedule(null)
        );
        assertTrue(nullCreate.getMessage().contains("scheduleSaveIn"));

        AttendanceScheduleIn.ScheduleSaveIn missingWorkDate = scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 26), 1L, "missing date");
        missingWorkDate.setWorkDate(null);
        IllegalArgumentException workDateError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.createSchedule(missingWorkDate)
        );
        assertTrue(workDateError.getMessage().contains("workDate"));

        IllegalArgumentException missingEmployee = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.createSchedule(scheduleSaveIn(999L, LocalDate.of(2026, 6, 26), 1L, "missing employee"))
        );
        assertTrue(missingEmployee.getMessage().contains("999"));

        IllegalArgumentException missingTemplate = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.createSchedule(scheduleSaveIn(seededEmployeeId, LocalDate.of(2026, 6, 26), 999L, "missing template"))
        );
        assertTrue(missingTemplate.getMessage().contains("999"));

        IllegalArgumentException missingSchedule = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.deleteSchedule(999L)
        );
        assertTrue(missingSchedule.getMessage().contains("999"));

        IllegalArgumentException nullBatch = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.batchAssignSchedules(null)
        );
        assertTrue(nullBatch.getMessage().contains("scheduleBatchAssignIn"));

        IllegalArgumentException nullCopy = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.copyLastWeek(null)
        );
        assertTrue(nullCopy.getMessage().contains("scheduleCopyIn"));

        IllegalArgumentException nullClear = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceScheduleService.clearSchedules(null)
        );
        assertTrue(nullClear.getMessage().contains("scheduleClearRangeIn"));
    }

    /**
     * 辅助目的：为scheduleSaveIn提供测试支撑。
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
     * 辅助目的：为scheduleBatchAssignIn提供测试支撑。
     */
    private AttendanceScheduleIn.ScheduleBatchAssignIn scheduleBatchAssignIn(
        List<Long> employeeIds,
        LocalDate startDate,
        LocalDate endDate,
        Long shiftTemplateId,
        Boolean skipExisting,
        Boolean overwriteExisting,
        String remark
    ) {
        AttendanceScheduleIn.ScheduleBatchAssignIn saveIn = new AttendanceScheduleIn.ScheduleBatchAssignIn();
        saveIn.setEmployeeIds(employeeIds);
        saveIn.setStartDate(startDate);
        saveIn.setEndDate(endDate);
        saveIn.setShiftTemplateId(shiftTemplateId);
        saveIn.setSkipExisting(skipExisting);
        saveIn.setOverwriteExisting(overwriteExisting);
        saveIn.setRemark(remark);
        return saveIn;
    }

    /**
     * 辅助目的：为scheduleCopyIn提供测试支撑。
     */
    private AttendanceScheduleIn.ScheduleCopyIn scheduleCopyIn(
        List<Long> employeeIds,
        LocalDate startDate,
        LocalDate endDate,
        Boolean overwriteExisting
    ) {
        AttendanceScheduleIn.ScheduleCopyIn saveIn = new AttendanceScheduleIn.ScheduleCopyIn();
        saveIn.setEmployeeIds(employeeIds);
        saveIn.setStartDate(startDate);
        saveIn.setEndDate(endDate);
        saveIn.setOverwriteExisting(overwriteExisting);
        return saveIn;
    }

    /**
     * 辅助目的：为scheduleClearRangeIn提供测试支撑。
     */
    private AttendanceScheduleIn.ScheduleClearRangeIn scheduleClearRangeIn(
        List<Long> employeeIds,
        LocalDate startDate,
        LocalDate endDate
    ) {
        AttendanceScheduleIn.ScheduleClearRangeIn saveIn = new AttendanceScheduleIn.ScheduleClearRangeIn();
        saveIn.setEmployeeIds(employeeIds);
        saveIn.setStartDate(startDate);
        saveIn.setEndDate(endDate);
        return saveIn;
    }

    /**
     * 辅助目的：为employeeSaveIn提供测试支撑。
     */
    private AttendanceIn.EmployeeSaveIn employeeSaveIn(String employeeNo, String employeeName, Long workplaceId, Long departmentId) {
        AttendanceIn.EmployeeSaveIn saveIn = new AttendanceIn.EmployeeSaveIn();
        saveIn.setEmployeeNo(employeeNo);
        saveIn.setEmployeeName(employeeName);
        saveIn.setEmploymentType("FULL_TIME");
        saveIn.setWorkplaceId(workplaceId);
        saveIn.setDepartmentId(departmentId);
        return saveIn;
    }
}
