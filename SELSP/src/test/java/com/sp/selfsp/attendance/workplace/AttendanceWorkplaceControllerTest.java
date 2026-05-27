package com.sp.selfsp.attendance.workplace;

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
public class AttendanceWorkplaceControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证shouldManageWorkplaceCrud场景。
     */
    @Test
    public void shouldManageWorkplaceCrud() throws Exception {
        java.util.LinkedHashMap<String, Object> createRequest = new java.util.LinkedHashMap<>();
        createRequest.put("workplaceCode", "OSA-BR");
        createRequest.put("workplaceName", "大阪分校");
        createRequest.put("address", "大阪市中央区1-2-3");
        createRequest.put("phone", "06-1000-2000");
        createRequest.put("status", "ACTIVE");
        JsonNode createdWorkplace = readData(mockMvc.perform(post("/api/attendance/workplaces")
                .contentType(JSON)
                .content(writeJson(createRequest)))
            .andExpect(status().isOk())
            .andReturn());
        long workplaceId = createdWorkplace.get("id").asLong();
        assertEquals("OSA-BR", createdWorkplace.get("workplaceCode").asText());

        java.util.LinkedHashMap<String, Object> updateRequest = new java.util.LinkedHashMap<>();
        updateRequest.put("workplaceCode", "OSA-BR");
        updateRequest.put("workplaceName", "大阪分校-已更新");
        updateRequest.put("address", "大阪市中央区9-9-9");
        updateRequest.put("phone", "06-1111-2222");
        updateRequest.put("status", "INACTIVE");
        JsonNode updatedWorkplace = readData(mockMvc.perform(put("/api/attendance/workplaces/{id}", workplaceId)
                .contentType(JSON)
                .content(writeJson(updateRequest)))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals("大阪分校-已更新", updatedWorkplace.get("workplaceName").asText());
        assertEquals("INACTIVE", updatedWorkplace.get("status").asText());

        mockMvc.perform(delete("/api/attendance/workplaces/{id}", workplaceId))
            .andExpect(status().isOk());

        JsonNode workplaces = readData(mockMvc.perform(get("/api/attendance/workplaces"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(2, workplaces.size());
    }
}
