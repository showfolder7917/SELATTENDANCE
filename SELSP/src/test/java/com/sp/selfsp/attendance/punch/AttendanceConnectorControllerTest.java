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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceConnectorControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证第八阶段正式接入工作台、Webhook 日志和失败重试闭环。
     */
    @Test
    public void shouldManageConnectorWorkbenchWebhookAndRetry() throws Exception {
        mockMvc.perform(get("/api/attendance/connectors").param("activeOnly", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.connectors.length()").value(2))
            .andExpect(jsonPath("$.data.summary.activeConnectorCount").value(2))
            .andExpect(jsonPath("$.data.summary.mappedEmployeeCount").value(1))
            .andExpect(jsonPath("$.data.summary.failedSyncCount").value(1));

        JsonNode createdConnector = readData(mockMvc.perform(post("/api/attendance/connectors")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "sourceSystem", "JOBCAN_PULL",
                    "connectorName", "Jobcan Pull 接入",
                    "providerType", "JOBCAN",
                    "receiveMode", "PULL",
                    "apiBaseUrl", "https://api.jobcan.example",
                    "apiKey", "jobcan-key-001",
                    "apiSecret", "jobcan-secret-001",
                    "syncCron", "0 */15 * * * ?",
                    "activeFlag", true,
                    "note", "第八阶段测试接入"
                ))))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals("JOBCAN_PULL", createdConnector.get("sourceSystem").asText());

        Long connectorId = createdConnector.get("id").asLong();
        mockMvc.perform(put("/api/attendance/connectors/{id}", connectorId)
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "sourceSystem", "JOBCAN_PULL",
                    "connectorName", "Jobcan Pull 接入更新版",
                    "providerType", "JOBCAN",
                    "receiveMode", "PULL",
                    "apiBaseUrl", "https://api.jobcan.example/v2",
                    "apiKey", "jobcan-key-002",
                    "apiSecret", "jobcan-secret-002",
                    "syncCron", "0 */10 * * * ?",
                    "activeFlag", true,
                    "note", "更新后的测试接入"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.connectorName").value("Jobcan Pull 接入更新版"));

        mockMvc.perform(post("/api/attendance/connectors/{id}/test", 2L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.success").value(true))
            .andExpect(jsonPath("$.data.webhookUrl").value("/api/attendance/connectors/webhook/WEBHOOK"));

        mockMvc.perform(post("/api/attendance/connectors/webhook/{sourceSystem}", "WEBHOOK")
                .header("X-Webhook-Secret", "demo-hook-secret")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "tenantCode", "DEFAULT",
                    "sourceEventId", "hook-live-001",
                    "externalEmployeeId", "KOT-0001",
                    "punchTime", "2026-05-29 09:05:00",
                    "punchType", "CLOCK_IN",
                    "deviceId", "gate-05",
                    "deviceName", "東京本部入口",
                    "rawData", java.util.Map.of("event", "live")
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.processStatus").value("PROCESSED"));

        mockMvc.perform(put("/api/attendance/employees/{id}/external-mapping", 1L)
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "sourceSystem", "WEBHOOK",
                    "externalEmployeeId", "WEBHOOK-NEW-9001",
                    "externalEmployeeNo", "WEBHOOK-NO-9001",
                    "status", "ACTIVE"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.externalEmployeeId").value("WEBHOOK-NEW-9001"));

        mockMvc.perform(post("/api/attendance/connectors/sync-logs/{id}/retry", 2L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.syncStatus").value("SUCCESS"))
            .andExpect(jsonPath("$.data.retryFlag").value(true))
            .andExpect(jsonPath("$.data.retryCount").value(1));

        JsonNode workbenchAfter = readData(mockMvc.perform(get("/api/attendance/connectors").param("sourceSystem", "WEBHOOK"))
            .andExpect(status().isOk())
            .andReturn());
        assertTrue(workbenchAfter.get("syncLogs").toString().contains("hook-live-001"));
        assertTrue(workbenchAfter.get("mappings").toString().contains("WEBHOOK-NEW-9001"));
    }
}
