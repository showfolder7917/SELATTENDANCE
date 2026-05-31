package com.sp.selfsp.attendance.shifttemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.shifttemplate.service.AttendanceShiftTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceShiftTemplateServiceBoundaryTest {

    @Autowired
    private AttendanceShiftTemplateService attendanceShiftTemplateService;

    /**
     * 测试目的：验证shouldCreateTemplateWithDefaultsAndRejectDuplicateCode场景。
     */
    @Test
    void shouldCreateTemplateWithDefaultsAndRejectDuplicateCode() {
        AttendanceOut.ShiftTemplateOut templateOut = attendanceShiftTemplateService.createShiftTemplate(shiftTemplateSaveIn(" FLEX ", " Flexible Shift ", "WORK", "08:00:00", "16:00:00", null, null, "", null));

        assertEquals("FLEX", templateOut.getTemplateCode());
        assertEquals(0, templateOut.getScheduledBreakMinutes());
        assertFalse(Boolean.TRUE.equals(templateOut.getCrossDay()));
        assertEquals("BLUE", templateOut.getColor());
        assertTrue(Boolean.TRUE.equals(templateOut.getActive()));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceShiftTemplateService.createShiftTemplate(shiftTemplateSaveIn("EARLY", "Dup", "WORK", "09:00:00", "18:00:00", false, 60, "BLUE", true))
        );
        assertEquals("templateCode 已存在", exception.getMessage());
    }

    /**
     * 测试目的：验证shouldRegenerateMissingRecommendedTemplateAndSkipExistingOnSecondRun场景。
     */
    @Test
    void shouldRegenerateMissingRecommendedTemplateAndSkipExistingOnSecondRun() {
        attendanceShiftTemplateService.deleteShiftTemplate(6L);

        java.util.List<AttendanceOut.ShiftTemplateOut> created = attendanceShiftTemplateService.generateRecommendedShiftTemplates();
        assertEquals(1, created.size());
        assertEquals("PAID_LEAVE", created.get(0).getTemplateCode());

        java.util.List<AttendanceOut.ShiftTemplateOut> secondRun = attendanceShiftTemplateService.generateRecommendedShiftTemplates();
        assertEquals(0, secondRun.size());
    }

    /**
     * 测试辅助目的：构造班次模板保存入参，统一复用模板新增和推荐模板测试。
     */
    private AttendanceIn.ShiftTemplateSaveIn shiftTemplateSaveIn(
        String code,
        String name,
        String shiftType,
        String startTime,
        String endTime,
        Boolean crossDay,
        Integer breakMinutes,
        String color,
        Boolean active
    ) {
        AttendanceIn.ShiftTemplateSaveIn saveIn = new AttendanceIn.ShiftTemplateSaveIn();
        saveIn.setTemplateCode(code);
        saveIn.setTemplateName(name);
        saveIn.setShiftType(shiftType);
        saveIn.setStartTime(startTime);
        saveIn.setEndTime(endTime);
        saveIn.setCrossDay(crossDay);
        saveIn.setScheduledBreakMinutes(breakMinutes);
        saveIn.setColor(color);
        saveIn.setActive(active);
        return saveIn;
    }
}
