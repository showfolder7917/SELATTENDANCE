package com.sp.selfsp.attendance.shifttemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.shifttemplate.dao.AttendanceShiftTemplateDao;
import com.sp.selfsp.attendance.support.AttendanceMapperIntegrationSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AttendanceShiftTemplateMapperTest extends AttendanceMapperIntegrationSupport {

    @Autowired
    private AttendanceShiftTemplateDao attendanceShiftTemplateDao;

    /**
     * 测试目的：验证shouldReadShiftTemplateFlagsAndOrdering场景。
     */
    @Test
    void shouldReadShiftTemplateFlagsAndOrdering() {
        List<AttendanceOut.ShiftTemplateOut> templates = attendanceShiftTemplateDao.selectList(TENANT_ID);
        AttendanceOut.ShiftTemplateOut nightTemplate = attendanceShiftTemplateDao.selectByCode(TENANT_ID, "NIGHT");

        assertEquals(6, templates.size());
        assertEquals("EARLY", templates.get(0).getTemplateCode());
        assertTrue(Boolean.TRUE.equals(nightTemplate.getCrossDay()));
        assertEquals("WORK", nightTemplate.getShiftType());
    }
}
