package com.sp.selfsp.attendance.bootstrap;

import com.sp.selfsp.attendance.support.AttendanceControllerIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceBootstrapControllerTest extends AttendanceControllerIntegrationSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试目的：验证shouldReturnBootstrapSummary场景。
     */
    @Test
    public void shouldReturnBootstrapSummary() throws Exception {
        mockMvc.perform(get("/api/attendance/bootstrap"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.tenant.tenantCode").value("TENANT_DEMO"))
            .andExpect(jsonPath("$.data.steps.length()").value(7))
            .andExpect(jsonPath("$.data.workplaces").doesNotExist())
            .andExpect(jsonPath("$.data.departments").doesNotExist())
            .andExpect(jsonPath("$.data.employees").doesNotExist())
            .andExpect(jsonPath("$.data.shiftTemplates").doesNotExist())
            .andExpect(jsonPath("$.data.steps[5].stepCode").value("schedule"))
            .andExpect(jsonPath("$.data.steps[5].status").value("NEEDS_ACTION"))
            .andExpect(jsonPath("$.data.recommendedNextAction").value("wizard.schedule"));
    }
}
