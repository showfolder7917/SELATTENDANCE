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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    /**
     * 测试目的：验证审批修改 final 时间后，日次增强分钟会同步刷新。
     */
    @Test
    public void shouldRefreshEnhancedMinutesAfterApproveFinalPatch() throws Exception {
        // 只看 5 月 18 日，锁定一个确定的缺下班卡样本，避免从整月异常池里拿到不稳定数据。
        JsonNode listData = readData(mockMvc.perform(get("/api/attendance/cases")
                .param("startDate", "2026-05-18")
                .param("endDate", "2026-05-18")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(1, listData.get("items").size());
        JsonNode targetCaseCandidate = listData.get("items").get(0);
        long attendanceDailyId = targetCaseCandidate.get("attendanceDailyId").asLong();

        // 先建单，让第五阶段审批链路进入可回写最终业务时间的状态。
        JsonNode createData = readData(mockMvc.perform(post("/api/attendance/cases")
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "attendanceDailyId", attendanceDailyId,
                    "caseType", targetCaseCandidate.get("caseType").asText(),
                    "applicantId", 9001,
                    "applicantRole", "MANAGER",
                    "reasonCategory", "MANUAL_CONFIRM",
                    "reasonText", "补录最终下班时间并同步增强分钟。",
                    "expectedResolution", "按人工修正后的最终时间重算。"
                ))))
            .andExpect(status().isOk())
            .andReturn());
        long caseId = createData.get("caseId").asLong();

        // 审批时补写最终上下班和休息分钟，验证第七阶段增强字段会一起变化。
        mockMvc.perform(post("/api/attendance/cases/{id}/actions", caseId)
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "actionType", "APPROVE",
                    "approverId", 9001,
                    "comment", "补录最终下班时间后同步刷新增强分钟。",
                    "patchPayload", Map.of(
                        "actualClockIn", "2026-05-18T09:00:00",
                        "actualClockOut", "2026-05-18T18:45:00",
                        "finalBreakMinutes", 60,
                        "finalStatus", "NORMAL",
                        "finalExceptionFlag", false
                    )
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.caseStatus").value("APPROVED"));

        // 审批完成后，日次详情里的工时、残业和规则字段都应已经联动刷新。
        mockMvc.perform(get("/api/attendance/daily/{id}", attendanceDailyId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("NORMAL"))
            .andExpect(jsonPath("$.data.actualWorkMinutes").value(525))
            .andExpect(jsonPath("$.data.normalWorkMinutes").value(480))
            .andExpect(jsonPath("$.data.overtimeMinutes").value(45))
            .andExpect(jsonPath("$.data.legalOvertimeMinutes").value(45))
            .andExpect(jsonPath("$.data.nightWorkMinutes").value(0))
            .andExpect(jsonPath("$.data.holidayWorkMinutes").value(0))
            .andExpect(jsonPath("$.data.appliedRuleName").value("日本标准规则"));
    }
}
