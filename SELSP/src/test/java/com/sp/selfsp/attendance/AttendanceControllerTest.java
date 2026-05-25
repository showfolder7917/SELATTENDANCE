package com.sp.selfsp.attendance;

// ObjectMapper 用于把测试请求对象序列化成 JSON。
import com.fasterxml.jackson.databind.ObjectMapper;
// Test 用于声明接口验证用例。
import org.junit.jupiter.api.Test;
// Autowired 用于注入测试依赖。
import org.springframework.beans.factory.annotation.Autowired;
// AutoConfigureMockMvc 用于注入 MockMvc。
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// SpringBootTest 用于启动完整后端上下文。
import org.springframework.boot.test.context.SpringBootTest;
// MediaType 用于声明 JSON 请求头。
import org.springframework.http.MediaType;
// Sql 用于在测试前重建第一阶段最小数据。
import org.springframework.test.context.jdbc.Sql;
// MockMvc 用于模拟真实 HTTP 请求。
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 考勤第一阶段控制器集成测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnBootstrapSummary() throws Exception {
        // 聚合接口必须同时返回向导步骤和基础主数据。
        mockMvc.perform(get("/api/attendance/bootstrap"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.tenant.tenantName").value("东京第一教室"))
            .andExpect(jsonPath("$.data.steps.length()").value(7))
            .andExpect(jsonPath("$.data.workplaces.length()").value(2))
            .andExpect(jsonPath("$.data.steps[5].stepCode").value("schedule"))
            .andExpect(jsonPath("$.data.steps[5].status").value("NEEDS_ACTION"))
            .andExpect(jsonPath("$.data.recommendedNextAction").value("wizard.schedule"));
    }

    @Test
    public void shouldCreateEmployeeAndDefaultRule() throws Exception {
        // 员工新增主路径要同时落库员工资料和默认勤怠规则。
        Map<String, Object> request = Map.of(
            "employeeNo", "E1001",
            "employeeName", "井上健",
            "employeeNameKana", "イノウエケン",
            "employmentType", "FULL_TIME",
            "workplaceId", 1,
            "departmentId", 1,
            "hireDate", "2026-05-20",
            "status", "ACTIVE"
        );
        mockMvc.perform(post("/api/attendance/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.employeeNo").value("E1001"))
            .andExpect(jsonPath("$.data.externalMappingBound").value(false));
        // 新增后直接拉员工列表，验证主档已经进入当前租户名册。
        mockMvc.perform(get("/api/attendance/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[1].employeeNo").value("E1001"));
    }

    @Test
    public void shouldImportEmployeesFromCsv() throws Exception {
        // 批量导入需要返回成功数并把事业所编码、部门编码解析成主数据主键。
        Map<String, Object> request = Map.of(
            "csvText",
            "employeeNo,employeeName,employeeNameKana,employmentType,workplaceCode,departmentCode,hireDate,email,phone\n"
                + "E1002,中村优子,ナカムラユウコ,PART_TIME,YKH-CLS,YKH-OPS,2026-05-21,yuko.nakamura@example.jp,090-5555-2222"
        );
        mockMvc.perform(post("/api/attendance/employees/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successCount").value(1))
            .andExpect(jsonPath("$.data.failedCount").value(0));
    }

    @Test
    public void shouldGenerateRecommendedTemplates() throws Exception {
        // 当前重置数据里已经有推荐模板，因此接口应表现为幂等并返回空新增列表。
        mockMvc.perform(post("/api/attendance/shift-templates/recommended"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.length()").value(0));
        // 生成后模板列表至少应包含 6 个推荐模板。
        mockMvc.perform(get("/api/attendance/shift-templates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(6));
    }

    @Test
    public void shouldBindExternalMapping() throws Exception {
        // 绑定外部打卡 ID 后，员工列表应能显示已绑定状态。
        Map<String, Object> request = Map.of(
            "sourceSystem", "KING_OF_TIME",
            "externalEmployeeId", "KOT-90001",
            "externalEmployeeNo", "90001",
            "status", "ACTIVE"
        );
        mockMvc.perform(put("/api/attendance/employees/1/external-mapping")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.externalMappingBound").value(true))
            .andExpect(jsonPath("$.data.externalEmployeeId").value("KOT-90001"));
    }

    @Test
    public void shouldCreateScheduleAndReturnBoard() throws Exception {
        // 第二阶段最小主路径要求管理员点击格子后能马上生成单日排班。
        Map<String, Object> request = Map.of(
            "employeeId", 1,
            "workDate", "2026-06-02",
            "shiftTemplateId", 1,
            "remark", "开月首批排班"
        );
        mockMvc.perform(post("/api/attendance/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.employeeId").value(1))
            .andExpect(jsonPath("$.data.templateCode").value("EARLY"));
        // 排班看板要返回日期列、员工行和刚保存的班次明细。
        mockMvc.perform(get("/api/attendance/schedules").param("month", "2026-06"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.month").value("2026-06"))
            .andExpect(jsonPath("$.data.employeeRows.length()").value(1))
            .andExpect(jsonPath("$.data.scheduleItems.length()").value(1))
            .andExpect(jsonPath("$.data.scheduleItems[0].templateName").value("早班"));
    }

    @Test
    public void shouldBatchAssignSchedulesAndCheckUnassigned() throws Exception {
        // 批量排班向导主路径要能一次性给多个日期套模板。
        Map<String, Object> request = Map.of(
            "employeeIds", java.util.List.of(1L),
            "startDate", "2026-06-03",
            "endDate", "2026-06-05",
            "shiftTemplateId", 2,
            "skipExisting", false,
            "overwriteExisting", true,
            "remark", "批量晚班"
        );
        mockMvc.perform(post("/api/attendance/schedules/batch-assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.createdCount").value(3))
            .andExpect(jsonPath("$.data.updatedCount").value(0));
        // 未排班检查要把仍有缺口的员工和日期列出来，帮助管理员继续补班。
        mockMvc.perform(get("/api/attendance/schedules/unassigned-check").param("month", "2026-06"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].employeeNo").value("E0001"))
            .andExpect(jsonPath("$.data[0].unassignedCount").value(27));
    }

    @Test
    public void shouldCopyLastWeekAndExportSchedules() throws Exception {
        // 先准备上一周排班，验证复制上周动作能把相同班次带到目标周。
        mockMvc.perform(post("/api/attendance/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "employeeId", 1,
                    "workDate", "2026-05-25",
                    "shiftTemplateId", 3,
                    "remark", "夜班样板"
                ))))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/attendance/schedules/copy-last-week")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "employeeIds", java.util.List.of(1L),
                    "startDate", "2026-06-01",
                    "endDate", "2026-06-01",
                    "overwriteExisting", true
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.createdCount").value(1));
        // 导出结果要包含当前月份文件名和已复制的模板名称，供前端下载排班表。
        mockMvc.perform(get("/api/attendance/schedules/export").param("month", "2026-06"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.fileName").value("attendance_schedule_2026-06.csv"))
            .andExpect(jsonPath("$.data.content").value(org.hamcrest.Matchers.containsString("夜班")));
    }
}
