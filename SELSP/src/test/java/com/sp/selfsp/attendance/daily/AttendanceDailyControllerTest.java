package com.sp.selfsp.attendance.daily;

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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceDailyControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证第四阶段日次列表、详情和重算主链路。
     */
    @Test
    public void shouldListDetailAndRecalculateDailyResults() throws Exception {
        // 先拉取整月日次结果，确认服务会自动计算并返回分页摘要。
        JsonNode listData = readData(mockMvc.perform(get("/api/attendance/daily")
                .param("startDate", "2026-05-01")
                .param("endDate", "2026-05-31")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(21))
            .andExpect(jsonPath("$.data.page").value(1))
            .andExpect(jsonPath("$.data.pageSize").value(20))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.summary.normalCount").value(1))
            .andExpect(jsonPath("$.data.summary.lateCount").value(2))
            .andExpect(jsonPath("$.data.summary.missingClockCount").value(14))
            .andExpect(jsonPath("$.data.summary.absenceCount").value(3))
            .andReturn());
        assertEquals(20, listData.get("items").size());

        // 只看异常时应该把唯一正常记录过滤掉，保留 20 条异常结果。
        mockMvc.perform(get("/api/attendance/daily")
                .param("startDate", "2026-05-01")
                .param("endDate", "2026-05-31")
                .param("exceptionOnly", "true")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(20))
            .andExpect(jsonPath("$.data.items[0].exceptionFlag").value(true));

        // 详情页要能带回排班、打卡、异常和计算过程，供前端抽屉直接展示。
        JsonNode detail = readData(mockMvc.perform(get("/api/attendance/daily/{id}", 2L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("LATE"))
            .andReturn());
        assertTrue(detail.get("schedule").get("label").asText().contains("早番"));
        assertTrue(detail.get("punches").size() >= 3);
        assertTrue(detail.get("exceptions").size() >= 1);
        assertTrue(detail.get("calcSteps").size() >= 3);

        // 单日重算要能按员工和日期重新刷新当天结果，并回传成功条数。
        mockMvc.perform(post("/api/attendance/daily/recalculate")
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "employeeId", 1,
                    "workDate", "2026-05-18"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successCount").value(1))
            .andExpect(jsonPath("$.data.failedCount").value(0));

        // 范围重算要能按筛选区间批量刷新整个阶段样本。
        mockMvc.perform(post("/api/attendance/daily/recalculate-range")
                .contentType(JSON)
                .content(writeJson(Map.of(
                    "startDate", "2026-05-19",
                    "endDate", "2026-05-21"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successCount").value(3))
            .andExpect(jsonPath("$.data.failedCount").value(0));
    }
}
