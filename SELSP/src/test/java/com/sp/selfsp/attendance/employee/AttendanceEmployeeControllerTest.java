package com.sp.selfsp.attendance.employee;

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
public class AttendanceEmployeeControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证shouldCreateEmployeeAndDefaultRule场景。
     */
    @Test
    public void shouldCreateEmployeeAndDefaultRule() throws Exception {
        java.util.LinkedHashMap<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("employeeNo", "E1001");
        request.put("employeeName", "伊藤健");
        request.put("employeeNameKana", "イトウケン");
        request.put("employmentType", "FULL_TIME");
        request.put("workplaceId", 1);
        request.put("departmentId", 1);
        request.put("hireDate", "2026-05-20");
        request.put("status", "ACTIVE");
        mockMvc.perform(post("/api/attendance/employees")
                .contentType(JSON)
                .content(writeJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.employeeNo").value("E1001"))
            .andExpect(jsonPath("$.data.externalMappingBound").value(false));

        mockMvc.perform(get("/api/attendance/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[1].employeeNo").value("E1001"));
    }

    /**
     * 测试目的：验证shouldImportEmployeesFromCsv场景。
     */
    @Test
    public void shouldImportEmployeesFromCsv() throws Exception {
        mockMvc.perform(post("/api/attendance/employees/import")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "csvText",
                    "employeeNo,employeeName,employeeNameKana,employmentType,workplaceCode,departmentCode,hireDate,email,phone\n"
                        + "E1002,中村优子,ナカムラユウコ,PART_TIME,YKH-CLS,YKH-OPS,2026-05-21,yuko.nakamura@example.jp,090-5555-2222"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successCount").value(1))
            .andExpect(jsonPath("$.data.failedCount").value(0));
    }

    /**
     * 测试目的：验证shouldBindExternalMapping场景。
     */
    @Test
    public void shouldBindExternalMapping() throws Exception {
        mockMvc.perform(put("/api/attendance/employees/1/external-mapping")
                .contentType(JSON)
                .content(writeJson(java.util.Map.of(
                    "sourceSystem", "KING_OF_TIME",
                    "externalEmployeeId", "KOT-90001",
                    "externalEmployeeNo", "90001",
                    "status", "ACTIVE"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.externalMappingBound").value(true))
            .andExpect(jsonPath("$.data.externalEmployeeId").value("KOT-90001"));
    }

    /**
     * 测试目的：验证shouldUpdateExportAndDeleteEmployee场景。
     */
    @Test
    public void shouldUpdateExportAndDeleteEmployee() throws Exception {
        java.util.LinkedHashMap<String, Object> updateRequest = new java.util.LinkedHashMap<>();
        updateRequest.put("employeeNo", "E0001");
        updateRequest.put("employeeName", "山田太郎-更新");
        updateRequest.put("employeeNameKana", "ヤマダタロウ");
        updateRequest.put("employmentType", "PART_TIME");
        updateRequest.put("workplaceId", 1);
        updateRequest.put("departmentId", 1);
        updateRequest.put("hireDate", "2026-04-01");
        updateRequest.put("status", "SUSPENDED");
        JsonNode updatedEmployee = readData(mockMvc.perform(put("/api/attendance/employees/1")
                .contentType(JSON)
                .content(writeJson(updateRequest)))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals("山田太郎-更新", updatedEmployee.get("employeeName").asText());
        assertEquals("SUSPENDED", updatedEmployee.get("status").asText());

        JsonNode filteredEmployees = readData(mockMvc.perform(get("/api/attendance/employees")
                .param("keyword", "更新")
                .param("status", "SUSPENDED"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(1, filteredEmployees.size());

        JsonNode employeeExport = readData(mockMvc.perform(get("/api/attendance/employees/export"))
            .andExpect(status().isOk())
            .andReturn());
        assertFalse(employeeExport.get("fileName").asText().isBlank());
        assertFalse(!employeeExport.get("content").asText().contains("E0001"));

        mockMvc.perform(delete("/api/attendance/employees/1"))
            .andExpect(status().isOk());

        JsonNode employeesAfterDelete = readData(mockMvc.perform(get("/api/attendance/employees"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(0, employeesAfterDelete.size());
    }
}
