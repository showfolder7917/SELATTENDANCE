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
     * 测试辅助目的：把请求对象序列化成 JSON 文本，供控制器测试直接提交请求体。
     */
    protected String writeJson(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    /**
     * 测试辅助目的：从统一响应壳中解析 data 节点，便于控制器测试直接断言业务数据。
     */
    protected JsonNode readData(MvcResult mvcResult) throws Exception {
        JsonNode response = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        return response.get("data");
    }
}
