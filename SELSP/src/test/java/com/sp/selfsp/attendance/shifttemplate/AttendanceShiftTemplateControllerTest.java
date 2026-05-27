package com.sp.selfsp.attendance.shifttemplate;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceShiftTemplateControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证shouldGenerateRecommendedTemplates场景。
     */
    @Test
    public void shouldGenerateRecommendedTemplates() throws Exception {
        mockMvc.perform(post("/api/attendance/shift-templates/recommended"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/api/attendance/shift-templates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(6));
    }

    /**
     * 测试目的：验证shouldCreateUpdateAndDeleteShiftTemplate场景。
     */
    @Test
    public void shouldCreateUpdateAndDeleteShiftTemplate() throws Exception {
        java.util.LinkedHashMap<String, Object> createRequest = new java.util.LinkedHashMap<>();
        createRequest.put("templateCode", "TRAINING");
        createRequest.put("templateName", "培训班次");
        createRequest.put("shiftType", "WORK");
        createRequest.put("startTime", "10:00:00");
        createRequest.put("endTime", "19:00:00");
        createRequest.put("crossDay", false);
        createRequest.put("scheduledBreakMinutes", 45);
        createRequest.put("color", "TEAL");
        createRequest.put("active", true);
        JsonNode createdTemplate = readData(mockMvc.perform(post("/api/attendance/shift-templates")
                .contentType(JSON)
                .content(writeJson(createRequest)))
            .andExpect(status().isOk())
            .andReturn());
        long templateId = createdTemplate.get("id").asLong();
        assertEquals("TRAINING", createdTemplate.get("templateCode").asText());

        java.util.LinkedHashMap<String, Object> updateRequest = new java.util.LinkedHashMap<>();
        updateRequest.put("templateCode", "TRAINING");
        updateRequest.put("templateName", "培训班次-已更新");
        updateRequest.put("shiftType", "WORK");
        updateRequest.put("startTime", "10:00:00");
        updateRequest.put("endTime", "18:30:00");
        updateRequest.put("crossDay", false);
        updateRequest.put("scheduledBreakMinutes", 30);
        updateRequest.put("color", "GREEN");
        updateRequest.put("active", false);
        JsonNode updatedTemplate = readData(mockMvc.perform(put("/api/attendance/shift-templates/{id}", templateId)
                .contentType(JSON)
                .content(writeJson(updateRequest)))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals("培训班次-已更新", updatedTemplate.get("templateName").asText());
        assertEquals(false, updatedTemplate.get("active").asBoolean());

        mockMvc.perform(delete("/api/attendance/shift-templates/{id}", templateId))
            .andExpect(status().isOk());

        JsonNode templatesAfterDelete = readData(mockMvc.perform(get("/api/attendance/shift-templates"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals(6, templatesAfterDelete.size());
    }
}
