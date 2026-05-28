package com.sp.selfsp.attendance.punch;

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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendancePunchControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证shouldListManualImportBindAndReprocessPunchLogs场景。
     */
    @Test
    public void shouldListManualImportBindAndReprocessPunchLogs() throws Exception {
        mockMvc.perform(get("/api/attendance/punch/logs")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(72))
            .andExpect(jsonPath("$.data.page").value(1))
            .andExpect(jsonPath("$.data.pageSize").value(20))
            .andExpect(jsonPath("$.data.totalPages").value(4))
            .andExpect(jsonPath("$.data.summary.processed").value(60))
            .andExpect(jsonPath("$.data.summary.unmatched").value(4));

        mockMvc.perform(get("/api/attendance/punch/logs")
                .param("page", "1")
                .param("pageSize", "200"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items.length()").value(72))
            .andExpect(jsonPath("$.data.pageSize").value(200))
            .andExpect(jsonPath("$.data.totalPages").value(1));

        mockMvc.perform(post("/api/attendance/punch/manual")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "employeeId", 1,
                    "punchTime", "2026-05-28 09:01:23",
                    "punchType", "CLOCK_IN",
                    "deviceName", "管理者手動補錄",
                    "note", "打刻漏れ対応"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.processStatus").value("PROCESSED"));

        mockMvc.perform(post("/api/attendance/punch/import-csv/preview")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "fileName", "preview.csv",
                    "csvText", String.join("\n",
                        "externalEmployeeId,punchTime,punchType,sourceSystem,sourceEventId,deviceId,deviceName",
                        "KOT-0001,2026-05-28 09:10:00,CLOCK_IN,CSV_IMPORT,preview-001,gate-01,東京本部入口",
                        "UNKNOWN-001,2026-05-28 18:10:00,CLOCK_OUT,CSV_IMPORT,preview-002,gate-01,東京本部入口"
                    )
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.summary.totalCount").value(2))
            .andExpect(jsonPath("$.data.summary.readyCount").value(1))
            .andExpect(jsonPath("$.data.summary.unmatchedCount").value(1));

        JsonNode importResult = readData(mockMvc.perform(post("/api/attendance/punch/import-csv")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "fileName", "import.csv",
                    "csvText", String.join("\n",
                        "externalEmployeeId,punchTime,punchType,sourceSystem,sourceEventId,deviceId,deviceName",
                        "KOT-0001,2026-05-28 09:20:00,CLOCK_IN,CSV_IMPORT,import-001,gate-01,東京本部入口",
                        "UNKNOWN-002,2026-05-28 18:20:00,CLOCK_OUT,CSV_IMPORT,import-002,gate-01,東京本部入口"
                    )
                ))))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(2, importResult.get("totalCount").asInt());
        assertEquals(1, importResult.get("successCount").asInt());
        assertEquals(1, importResult.get("unmatchedCount").asInt());

        JsonNode unmatchedDetail = readData(mockMvc.perform(get("/api/attendance/punch/logs/{id}", 1017L))
            .andExpect(status().isOk())
            .andReturn());
        assertTrue(unmatchedDetail.get("availableActions").toString().contains("BIND_EMPLOYEE"));

        mockMvc.perform(post("/api/attendance/punch/logs/{id}/bind-employee", 1017L)
                .contentType(JSON)
                .content(writeJson(java.util.Map.of("employeeId", 1))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.processStatus").value("PROCESSED"))
            .andExpect(jsonPath("$.data.employeeId").value(1));

        mockMvc.perform(post("/api/attendance/punch/logs/{id}/ignore", 1019L)
                .contentType(JSON)
                .content(writeJson(java.util.Map.of("reason", "確認済みのため除外"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.processStatus").value("IGNORED"));

        mockMvc.perform(post("/api/attendance/punch/logs/{id}/reprocess", 1034L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.processStatus").value("UNMATCHED"));
    }
}
