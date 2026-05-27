package com.sp.selfsp.attendance.department;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceDepartmentControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证shouldManageDepartmentCrud场景。
     */
    @Test
    public void shouldManageDepartmentCrud() throws Exception {
        java.util.LinkedHashMap<String, Object> workplaceRequest = new java.util.LinkedHashMap<>();
        workplaceRequest.put("workplaceCode", "DEP-WP");
        workplaceRequest.put("workplaceName", "部门测试校区");
        workplaceRequest.put("address", "测试地址");
        workplaceRequest.put("phone", "06-0000-0000");
        workplaceRequest.put("status", "ACTIVE");
        JsonNode createdWorkplace = readData(mockMvc.perform(post("/api/attendance/workplaces")
                .contentType(JSON)
                .content(writeJson(workplaceRequest)))
            .andExpect(status().isOk())
            .andReturn());
        long workplaceId = createdWorkplace.get("id").asLong();

        java.util.LinkedHashMap<String, Object> createRequest = new java.util.LinkedHashMap<>();
        createRequest.put("workplaceId", workplaceId);
        createRequest.put("departmentCode", "OSA-OPS");
        createRequest.put("departmentName", "大阪运营组");
        createRequest.put("sortOrder", 10);
        createRequest.put("status", "ACTIVE");
        JsonNode createdDepartment = readData(mockMvc.perform(post("/api/attendance/departments")
                .contentType(JSON)
                .content(writeJson(createRequest)))
            .andExpect(status().isOk())
            .andReturn());
        long departmentId = createdDepartment.get("id").asLong();
        assertEquals("OSA-OPS", createdDepartment.get("departmentCode").asText());

        java.util.LinkedHashMap<String, Object> updateRequest = new java.util.LinkedHashMap<>();
        updateRequest.put("workplaceId", workplaceId);
        updateRequest.put("departmentCode", "OSA-OPS");
        updateRequest.put("departmentName", "大阪运营组-已更新");
        updateRequest.put("sortOrder", 20);
        updateRequest.put("status", "INACTIVE");
        JsonNode updatedDepartment = readData(mockMvc.perform(put("/api/attendance/departments/{id}", departmentId)
                .contentType(JSON)
                .content(writeJson(updateRequest)))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals("大阪运营组-已更新", updatedDepartment.get("departmentName").asText());
        assertEquals(20, updatedDepartment.get("sortOrder").asInt());

        mockMvc.perform(delete("/api/attendance/departments/{id}", departmentId))
            .andExpect(status().isOk());

        JsonNode departments = readData(mockMvc.perform(get("/api/attendance/departments"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(2, departments.size());
    }
}
