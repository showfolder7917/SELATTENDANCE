package com.sp.selfsp.attendance.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public abstract class AttendanceIntegrationSupport {

    protected static final Long TENANT_ID = 1L;
}
