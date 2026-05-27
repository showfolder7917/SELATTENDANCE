package com.sp.selfsp.attendance.tenant;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceTenantControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证shouldGetAndUpdateCurrentTenant场景。
     */
    @Test
    public void shouldGetAndUpdateCurrentTenant() throws Exception {
        JsonNode currentTenant = readData(mockMvc.perform(get("/api/attendance/tenant/current"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals("TENANT_DEMO", currentTenant.get("tenantCode").asText());
        assertEquals("Asia/Tokyo", currentTenant.get("timezone").asText());

        java.util.LinkedHashMap<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("tenantCode", "TENANT_DEMO");
        request.put("tenantName", "东京第一学校-更新");
        request.put("contactName", "系统管理员");
        request.put("contactPhone", "03-9999-8888");
        request.put("contactEmail", "ops@tokyo-school.jp");
        request.put("timezone", "Asia/Shanghai");
        JsonNode savedTenant = readData(mockMvc.perform(put("/api/attendance/tenant/current")
                .contentType(JSON)
                .content(writeJson(request)))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals("东京第一学校-更新", savedTenant.get("tenantName").asText());
        assertEquals("Asia/Shanghai", savedTenant.get("timezone").asText());

        JsonNode reloadedTenant = readData(mockMvc.perform(get("/api/attendance/tenant/current"))
            .andExpect(status().isOk())
            .andReturn());
        assertEquals("ops@tokyo-school.jp", reloadedTenant.get("contactEmail").asText());
    }
}
