package com.sp.selfsp.attendance.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

public abstract class AttendanceControllerIntegrationSupport extends AttendanceIntegrationSupport {

    @Autowired
    protected ObjectMapper objectMapper;

    protected static final MediaType JSON = MediaType.APPLICATION_JSON;

    /**
     * 辅助目的：为writeJson提供测试支撑。
     */
    protected String writeJson(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    /**
     * 辅助目的：为readData提供测试支撑。
     */
    protected JsonNode readData(MvcResult mvcResult) throws Exception {
        JsonNode response = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        return response.get("data");
    }
}
