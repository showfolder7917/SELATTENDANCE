package com.sp.selfsp.attendance.schedule;

import com.fasterxml.jackson.databind.JsonNode;
import com.sp.selfsp.attendance.support.AttendanceControllerIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceScheduleControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证shouldCreateScheduleAndReturnBoard场景。
     */
    @Test
    public void shouldCreateScheduleAndReturnBoard() throws Exception {
        mockMvc.perform(post("/api/attendance/schedules")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeId", 1,
                    "workDate", "2026-06-02",
                    "shiftTemplateId", 1,
                    "remark", "首日人工排班"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.employeeId").value(1))
            .andExpect(jsonPath("$.data.templateCode").value("EARLY"));

        mockMvc.perform(get("/api/attendance/schedules").param("month", "2026-06"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.month").value("2026-06"))
            .andExpect(jsonPath("$.data.employeeRows.length()").value(1))
            .andExpect(jsonPath("$.data.scheduleItems.length()").value(1))
            .andExpect(jsonPath("$.data.scheduleItems[0].templateCode").value("EARLY"));
    }

    /**
     * 测试目的：验证shouldBatchAssignSchedulesAndCheckUnassigned场景。
     */
    @Test
    public void shouldBatchAssignSchedulesAndCheckUnassigned() throws Exception {
        mockMvc.perform(post("/api/attendance/schedules/batch-assign")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeIds", java.util.List.of(1L),
                    "startDate", "2026-06-03",
                    "endDate", "2026-06-05",
                    "shiftTemplateId", 2,
                    "skipExisting", false,
                    "overwriteExisting", true,
                    "remark", "批量晚班"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.createdCount").value(3))
            .andExpect(jsonPath("$.data.updatedCount").value(0));

        mockMvc.perform(get("/api/attendance/schedules/unassigned-check").param("month", "2026-06"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].employeeNo").value("E0001"))
            .andExpect(jsonPath("$.data[0].unassignedCount").value(27));
    }

    /**
     * 测试目的：验证shouldCopyLastWeekAndExportSchedules场景。
     */
    @Test
    public void shouldCopyLastWeekAndExportSchedules() throws Exception {
        mockMvc.perform(post("/api/attendance/schedules")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeId", 1,
                    "workDate", "2026-05-25",
                    "shiftTemplateId", 3,
                    "remark", "夜班来源"
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/schedules/copy-last-week")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeIds", java.util.List.of(1L),
                    "startDate", "2026-06-01",
                    "endDate", "2026-06-01",
                    "overwriteExisting", true
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.createdCount").value(1));

        mockMvc.perform(get("/api/attendance/schedules/export").param("month", "2026-06"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.fileName").value("attendance_schedule_2026-06.csv"))
            .andExpect(jsonPath("$.data.content").value(org.hamcrest.Matchers.containsString("E0001")));
    }

    /**
     * 测试目的：验证shouldCopyLastMonthUpdateDeleteAndClearSchedules场景。
     */
    @Test
    public void shouldCopyLastMonthUpdateDeleteAndClearSchedules() throws Exception {
        mockMvc.perform(post("/api/attendance/schedules")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeId", 1,
                    "workDate", "2026-05-01",
                    "shiftTemplateId", 3,
                    "remark", "五月夜班样本"
                ))))
            .andExpect(status().isOk());

        JsonNode copiedResult = readData(mockMvc.perform(post("/api/attendance/schedules/copy-last-month")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeIds", java.util.List.of(1L),
                    "startDate", "2026-06-01",
                    "endDate", "2026-06-02",
                    "overwriteExisting", true
                ))))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(1, copiedResult.get("createdCount").asInt());
        assertEquals(1, copiedResult.get("skippedCount").asInt());

        JsonNode juneBoard = readData(mockMvc.perform(get("/api/attendance/schedules").param("month", "2026-06"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(1, juneBoard.get("scheduleItems").size());
        assertEquals("NIGHT", juneBoard.get("scheduleItems").get(0).get("templateCode").asText());

        long copiedScheduleId = juneBoard.get("scheduleItems").get(0).get("id").asLong();

        JsonNode updatedSchedule = readData(mockMvc.perform(put("/api/attendance/schedules/{id}", copiedScheduleId)
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeId", 1,
                    "workDate", "2026-06-01",
                    "shiftTemplateId", 1,
                    "remark", "六月已调整"
                ))))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals("EARLY", updatedSchedule.get("templateCode").asText());

        mockMvc.perform(delete("/api/attendance/schedules/{id}", copiedScheduleId))
            .andExpect(status().isOk());

        JsonNode boardAfterDelete = readData(mockMvc.perform(get("/api/attendance/schedules").param("month", "2026-06"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(0, boardAfterDelete.get("scheduleItems").size());

        mockMvc.perform(post("/api/attendance/schedules")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeId", 1,
                    "workDate", "2026-06-03",
                    "shiftTemplateId", 1,
                    "remark", "六月三日"
                ))))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/attendance/schedules")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeId", 1,
                    "workDate", "2026-06-04",
                    "shiftTemplateId", 2,
                    "remark", "六月四日"
                ))))
            .andExpect(status().isOk());

        JsonNode clearedResult = readData(mockMvc.perform(post("/api/attendance/schedules/clear-range")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeIds", java.util.List.of(1L),
                    "startDate", "2026-06-03",
                    "endDate", "2026-06-04"
                ))))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(0, clearedResult.get("createdCount").asInt());
        assertEquals(2, clearedResult.get("affectedDateCount").asInt());

        JsonNode boardAfterClear = readData(mockMvc.perform(get("/api/attendance/schedules").param("month", "2026-06"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(0, boardAfterClear.get("scheduleItems").size());
    }
}
