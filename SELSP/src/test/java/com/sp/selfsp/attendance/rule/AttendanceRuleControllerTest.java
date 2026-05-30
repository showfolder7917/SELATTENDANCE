package com.sp.selfsp.attendance.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.sp.selfsp.attendance.support.AttendanceControllerIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
public class AttendanceRuleControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证 shouldCreateAssignAndLoadRuleWorkbench 场景。
     */
    @Test
    public void shouldCreateAssignAndLoadRuleWorkbench() throws Exception {
        LinkedHashMap<String, Object> createRequest = new LinkedHashMap<>();
        // 规则编码固定成测试专用值，便于后续从规则列表里精确回找新增结果。
        createRequest.put("ruleCode", "JP_LOW_ALERT");
        // 规则名称先写成初始名，后面更新接口再验证修改是否生效。
        createRequest.put("ruleName", "测试低阈值规则");
        // 标准工时和周工时沿用日本标准样本，保证超限提醒只由阈值控制。
        createRequest.put("standardDailyMinutes", 480);
        createRequest.put("standardWeeklyMinutes", 2400);
        // 自动休息与深夜窗口一起写入，确保这条规则是完整正式规则而不是最小残缺样本。
        createRequest.put("autoBreakEnabled", true);
        createRequest.put("autoBreakThresholdMinutes", 360);
        createRequest.put("autoBreakDeductMinutes", 60);
        createRequest.put("nightWorkStart", "22:00");
        createRequest.put("nightWorkEnd", "05:00");
        // 取整和提醒阈值刻意压低，便于测试月次与年度提醒都快速触发。
        createRequest.put("roundingUnitMinutes", 15);
        createRequest.put("roundingMode", "ROUND_UP");
        createRequest.put("monthlyOvertimeAlertHours", 1);
        createRequest.put("yearlyOvertimeAlertHours", 1);
        // 保留有休提醒和启用标志，确保规则工作台三类提醒都能在同一条规则下验证。
        createRequest.put("paidLeaveReminderEnabled", true);
        createRequest.put("activeFlag", true);
        createRequest.put("note", "让测试月次快速触发预警");
        JsonNode createdRule = readData(mockMvc.perform(post("/api/attendance/rules")
                .contentType(JSON)
                .content(writeJson(createRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ruleCode").value("JP_LOW_ALERT"))
            .andReturn());
        long ruleId = createdRule.get("id").asLong();

        // 先跑一次第六阶段月次重算，让第七阶段规则看板拿到当月残业和有休统计。
        mockMvc.perform(post("/api/attendance/monthly/recalculate")
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "yearMonth", "2026-05",
                    "operatorId", 9001,
                    "forceRebuild", true
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(put("/api/attendance/rules/assignments/1")
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "ruleId", ruleId,
                    "effectiveStartDate", "2026-05-01",
                    "note", "测试员工切换到低阈值规则"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.employeeId").value(1))
            .andExpect(jsonPath("$.data.ruleId").value(ruleId));

        LinkedHashMap<String, Object> updateRuleRequest = new LinkedHashMap<>();
        // 更新请求沿用同一规则编码，验证的是“更新既有规则”而不是新建第二条规则。
        updateRuleRequest.put("ruleCode", "JP_LOW_ALERT");
        updateRuleRequest.put("ruleName", "测试低阈值规则-更新");
        updateRuleRequest.put("standardDailyMinutes", 480);
        updateRuleRequest.put("standardWeeklyMinutes", 2400);
        updateRuleRequest.put("autoBreakEnabled", true);
        updateRuleRequest.put("autoBreakThresholdMinutes", 360);
        updateRuleRequest.put("autoBreakDeductMinutes", 60);
        updateRuleRequest.put("nightWorkStart", "22:00");
        updateRuleRequest.put("nightWorkEnd", "05:00");
        updateRuleRequest.put("roundingUnitMinutes", 10);
        updateRuleRequest.put("roundingMode", "ROUND_NEAREST");
        updateRuleRequest.put("monthlyOvertimeAlertHours", 1);
        updateRuleRequest.put("yearlyOvertimeAlertHours", 1);
        updateRuleRequest.put("paidLeaveReminderEnabled", true);
        updateRuleRequest.put("activeFlag", true);
        updateRuleRequest.put("note", "更新规则展示名称");
        mockMvc.perform(put("/api/attendance/rules/{id}", ruleId)
                .contentType(JSON)
                .content(writeJson(updateRuleRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ruleName").value("测试低阈值规则-更新"));

        // 5 月工作台需要明确产出“月残业预警”，证明第七阶段月 45h 口径已进入正式看板。
        JsonNode workbench = readData(mockMvc.perform(get("/api/attendance/rules")
                .param("yearMonth", "2026-05"))
            .andExpect(status().isOk())
            .andReturn());

        // 规则列表至少包含刚刚创建并更新过的规则，证明规则主数据已进入工作台。
        assertTrue(workbench.get("rules").size() >= 1);
        assertEquals("测试低阈值规则-更新", findRuleByCode(workbench, "JP_LOW_ALERT").get("ruleName").asText());
        // 员工适用关系里要能看到测试员工已经切到更新后的规则。
        assertTrue(workbench.get("assignments").size() >= 1);
        JsonNode mayAssignment = findAssignmentByEmployeeId(workbench, 1L);
        assertEquals("测试低阈值规则-更新", mayAssignment.get("ruleName").asText());
        // 月残业分钟至少达到 60，证明第六阶段月次统计已经正确回灌给第七阶段规则工作台。
        assertTrue(mayAssignment.get("monthlyOvertimeMinutes").asInt() >= 60);
        // 头部摘要必须同步统计已绑定员工数量，保证规则页上方指标卡不是空壳。
        assertTrue(workbench.get("summary").get("boundEmployeeCount").asInt() >= 1);
        JsonNode monthlyAlert = findAlert(workbench, "MONTHLY_OVERTIME", 1L);
        // 5 月样本必须产出月残业高风险提醒，证明月阈值提醒在看板里真正可见。
        assertNotNull(monthlyAlert);
        assertEquals("HIGH", monthlyAlert.get("alertLevel").asText());
        assertTrue(monthlyAlert.get("currentValueMinutes").asInt() >= monthlyAlert.get("thresholdMinutes").asInt());

        // 10 月工作台要同时证明“年残业预警 + 有休提醒”都能稳定出现，覆盖文档里的两类长期提醒。
        JsonNode octoberWorkbench = readData(mockMvc.perform(get("/api/attendance/rules")
                .param("yearMonth", "2026-10"))
            .andExpect(status().isOk())
            .andReturn());

        JsonNode octoberAssignment = findAssignmentByEmployeeId(octoberWorkbench, 1L);
        // 10 月样本同时验证年度累计残业和有休累计天数，覆盖规则页长期提醒的两类数据源。
        assertTrue(octoberAssignment.get("yearlyOvertimeMinutes").asInt() >= 60);
        assertTrue(octoberAssignment.get("yearlyPaidLeaveDays").asDouble() < 5D);
        JsonNode yearlyAlert = findAlert(octoberWorkbench, "YEARLY_OVERTIME", 1L);
        // 年残业提醒必须是高风险，证明年度阈值判断已经接通。
        assertNotNull(yearlyAlert);
        assertEquals("HIGH", yearlyAlert.get("alertLevel").asText());
        JsonNode paidLeaveAlert = findAlert(octoberWorkbench, "PAID_LEAVE_REMINDER", 1L);
        // 有休提醒要给出提醒级别和 5 天阈值，证明规则工作台没有丢失法定义务提醒口径。
        assertNotNull(paidLeaveAlert);
        assertEquals("REMINDER", paidLeaveAlert.get("alertLevel").asText());
        assertEquals(5D, paidLeaveAlert.get("thresholdDays").asDouble());
    }

    // 按规则编码回找规则，避免测试依赖规则列表固定排序。
    private JsonNode findRuleByCode(JsonNode workbench, String ruleCode) {
        for (JsonNode item : workbench.get("rules")) {
            if (ruleCode.equals(item.get("ruleCode").asText())) {
                return item;
            }
        }
        throw new IllegalArgumentException("未找到规则：" + ruleCode);
    }

    // 按员工回找适用关系，保证月次和年度断言都落在同一个样本员工上。
    private JsonNode findAssignmentByEmployeeId(JsonNode workbench, Long employeeId) {
        for (JsonNode item : workbench.get("assignments")) {
            if (employeeId.equals(item.get("employeeId").asLong())) {
                return item;
            }
        }
        throw new IllegalArgumentException("未找到员工适用关系：" + employeeId);
    }

    // 按预警类型和员工定位目标预警，避免把“只要有一条 alerts”误判成第七阶段全部通过。
    private JsonNode findAlert(JsonNode workbench, String alertType, Long employeeId) {
        for (JsonNode item : workbench.get("alerts")) {
            if (alertType.equals(item.get("alertType").asText()) && employeeId.equals(item.get("employeeId").asLong())) {
                return item;
            }
        }
        throw new IllegalArgumentException("未找到预警：" + alertType + " / " + employeeId);
    }
}
