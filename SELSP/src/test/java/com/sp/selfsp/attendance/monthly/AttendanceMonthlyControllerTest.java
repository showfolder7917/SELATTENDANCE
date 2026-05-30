package com.sp.selfsp.attendance.monthly;

import com.fasterxml.jackson.databind.JsonNode;
import com.sp.selfsp.attendance.support.AttendanceControllerIntegrationSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import jakarta.servlet.ServletException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceMonthlyControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试目的：验证第六阶段月次列表、详情、重算、月结、反结和导出主链路。
     */
    @Test
    public void shouldListDetailCloseReopenAndExportMonthlyResults() throws Exception {
        // 先拉取 5 月月次列表，确认服务会自动补齐日次并生成月汇总。
        JsonNode mayListData = readData(mockMvc.perform(get("/api/attendance/monthly")
                .param("yearMonth", "2026-05")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.page").value(1))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andReturn());
        assertEquals(1, mayListData.get("items").size());
        Long mayMonthlyId = mayListData.get("items").get(0).get("monthlyId").asLong();

        // 5 月详情页要能带回统计项、阻塞原因、动作日志和日次快照，供右侧详情直接使用。
        JsonNode mayDetail = readData(mockMvc.perform(get("/api/attendance/monthly/{id}", mayMonthlyId))
            .andExpect(status().isOk())
            .andReturn());
        assertTrue(mayDetail.get("items").size() >= 10);
        assertTrue(mayDetail.get("dailySnapshots").size() >= 1);
        assertTrue(mayDetail.get("blockReasons").size() >= 1);

        // 先验证存在阻塞时月结会被拒绝，防止未闭环数据误进入已结状态。
        assertThrows(ServletException.class, () ->
            mockMvc.perform(post("/api/attendance/monthly/close")
                    .contentType(JSON)
                    .content(writeJson(Map.of(
                        "yearMonth", "2026-05",
                        "scopeType", "COMPANY",
                        "operatorId", 9001,
                        "comment", "blocked-close"
                    ))))
                .andReturn()
        );
        // 被阻塞时不能出现部分月次已经改成 CLOSED 的脏数据。
        Integer closedCountAfterBlocked = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM attendance_monthly WHERE close_status = 'CLOSED'",
            Integer.class
        );
        assertEquals(0, closedCountAfterBlocked == null ? 0 : closedCountAfterBlocked);

        // 6 月准备了无异常样本，先重算生成可月结月次，再单独验证成功链路。
        mockMvc.perform(post("/api/attendance/monthly/recalculate")
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "yearMonth", "2026-06",
                    "recalcMode", "FULL",
                    "overwriteClosed", true,
                    "operatorId", 9001
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successCount").value(1));

        // 重算后读取 6 月月次，供后续月结、反结和导出都围绕同一条成功样本展开。
        JsonNode juneListData = readData(mockMvc.perform(get("/api/attendance/monthly")
                .param("yearMonth", "2026-06")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andReturn());
        Long juneMonthlyId = juneListData.get("items").get(0).get("monthlyId").asLong();

        // 6 月没有阻塞项，因此月结应直接成功，并把状态更新为 CLOSED。
        mockMvc.perform(post("/api/attendance/monthly/close")
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "yearMonth", "2026-06",
                    "scopeType", "COMPANY",
                    "operatorId", 9001,
                    "comment", "close-month"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.closedCount").value(1));

        // 反结后应允许重新回到 REOPENED 状态，供继续审批和重算。
        mockMvc.perform(post("/api/attendance/monthly/reopen")
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "monthlyId", juneMonthlyId,
                    "operatorId", 9001,
                    "reason", "need-adjustment"
                ))))
            .andExpect(status().isOk());

        // 导出动作要返回文件名和 CSV 文本，供前端直接下载。
        mockMvc.perform(post("/api/attendance/monthly/export")
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "yearMonth", "2026-06",
                    "page", 1,
                    "pageSize", 20
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.fileName").value("attendance-monthly-2026-06.csv"))
            .andExpect(jsonPath("$.data.content").isString());
    }
}
