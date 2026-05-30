package com.sp.selfsp.attendance.casefile;

import com.fasterxml.jackson.databind.JsonNode;
import com.sp.selfsp.attendance.support.AttendanceControllerIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 第五阶段异常处理与审批控制器集成测试。
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceCaseControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证第五阶段从异常列表、创建处理单、审批通过到锁定解锁的最小闭环。
     */
    @Test
    public void shouldCreateApproveAndLockAttendanceCase() throws Exception {
        // 第一次打开第五阶段入口时，也应该能自动补齐第四阶段日次结果并看到待处理异常。
        JsonNode listData = readData(mockMvc.perform(get("/api/attendance/cases")
                .param("startDate", "2026-05-01")
                .param("endDate", "2026-05-31")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").isNumber())
            .andReturn());
        assertTrue(listData.get("total").asInt() > 0);
        JsonNode firstUnhandled = null;
        for (JsonNode item : listData.get("items")) {
            if (item.get("pseudoCase").asBoolean()) {
                firstUnhandled = item;
                break;
            }
        }
        assertNotNull(firstUnhandled);
        long attendanceDailyId = firstUnhandled.get("attendanceDailyId").asLong();

        // 基于未建单异常创建真实处理单，并把日次状态推进到审核中。
        JsonNode createData = readData(mockMvc.perform(post("/api/attendance/cases")
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "attendanceDailyId", attendanceDailyId,
                    "caseType", firstUnhandled.get("caseType").asText(),
                    "applicantId", 9001,
                    "applicantRole", "MANAGER",
                    "reasonCategory", "DEVICE_ERROR",
                    "reasonText", "终端在下班前断线，先提交处理单。",
                    "expectedResolution", "请按正常下班处理。"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.caseStatus").value("SUBMITTED"))
            .andExpect(jsonPath("$.data.handlingStatus").value("IN_REVIEW"))
            .andReturn());
        long caseId = createData.get("caseId").asLong();

        // 详情页需要能带回日次详情和动作时间线，供右侧审批区直接使用。
        JsonNode detailData = readData(mockMvc.perform(get("/api/attendance/cases/{id}", caseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.caseStatus").value("SUBMITTED"))
            .andReturn());
        assertTrue(detailData.get("dailyDetail").has("workDate"));
        assertTrue(detailData.get("actionLogs").size() >= 1);

        // 审批通过后要把最终结论回写到日次结果，并把处理单状态改成已通过。
        mockMvc.perform(post("/api/attendance/cases/{id}/actions", caseId)
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "actionType", "APPROVE",
                    "approverId", 9001,
                    "comment", "同意按人工修正结果通过。",
                    "patchPayload", Map.of(
                        "finalStatus", "NORMAL",
                        "finalExceptionFlag", false
                    )
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.caseStatus").value("APPROVED"))
            .andExpect(jsonPath("$.data.handlingStatus").value("RESOLVED"));

        // 审批通过后的日次允许锁定，再允许管理员解锁进行后续修正。
        mockMvc.perform(post("/api/attendance/daily/{id}/lock", attendanceDailyId))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/attendance/daily/{id}/unlock", attendanceDailyId))
            .andExpect(status().isOk());
    }
}
