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

    /**
     * 测试目的：验证第七阶段正式规则已经接入日次算法，可稳定输出残业、深夜和法定休日结果。
     */
    @Test
    public void shouldApplyPhase7RuleCalculationToDailyResults() throws Exception {
        // 先加载 7 月日次，让服务自动生成带规则增强字段的结果。
        JsonNode juneListData = readData(mockMvc.perform(get("/api/attendance/daily")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-05")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(3))
            .andReturn());

        // 7 月 3 日应按自动休息和标准工时算出 30 分钟残业。
        JsonNode overtimeDaily = findDailyByWorkDate(juneListData, "2026-07-03");
        mockMvc.perform(get("/api/attendance/daily/{id}", overtimeDaily.get("id").asLong()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.appliedRuleName").value("日本标准规则"))
            .andExpect(jsonPath("$.data.actualWorkMinutes").value(510))
            .andExpect(jsonPath("$.data.overtimeMinutes").value(30))
            .andExpect(jsonPath("$.data.legalOvertimeMinutes").value(30))
            .andExpect(jsonPath("$.data.nightWorkMinutes").value(0))
            .andExpect(jsonPath("$.data.holidayType").isEmpty());

        // 7 月 4 日应命中深夜窗口，把 22:00 之后的分钟沉淀进深夜工时。
        JsonNode nightDaily = findDailyByWorkDate(juneListData, "2026-07-04");
        mockMvc.perform(get("/api/attendance/daily/{id}", nightDaily.get("id").asLong()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.actualWorkMinutes").value(600))
            .andExpect(jsonPath("$.data.overtimeMinutes").value(120))
            .andExpect(jsonPath("$.data.nightWorkMinutes").value(115))
            .andExpect(jsonPath("$.data.status").value("NORMAL"));

        // 7 月 5 日法定休日样本应把全部工时记入休日工时，并标记法定休日类型。
        JsonNode legalHolidayDaily = findDailyByWorkDate(juneListData, "2026-07-05");
        mockMvc.perform(get("/api/attendance/daily/{id}", legalHolidayDaily.get("id").asLong()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("HOLIDAY_WORK"))
            .andExpect(jsonPath("$.data.holidayType").value("LEGAL_HOLIDAY"))
            .andExpect(jsonPath("$.data.holidayWorkMinutes").value(240))
            .andExpect(jsonPath("$.data.legalOvertimeMinutes").value(240));
    }

    // 从当前列表结果里按工作日找到目标日次，避免依赖数据库自增主键顺序。
    private JsonNode findDailyByWorkDate(JsonNode listData, String workDate) {
        for (JsonNode item : listData.get("items")) {
            if (workDate.equals(item.get("workDate").asText())) {
                return item;
            }
        }
        throw new IllegalArgumentException("未找到工作日：" + workDate);
    }
}
