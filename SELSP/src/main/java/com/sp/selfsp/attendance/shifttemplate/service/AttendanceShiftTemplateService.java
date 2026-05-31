package com.sp.selfsp.attendance.shifttemplate.service;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import java.util.List;

public interface AttendanceShiftTemplateService {

    List<AttendanceOut.ShiftTemplateOut> listShiftTemplates();

    AttendanceOut.ShiftTemplateOut createShiftTemplate(AttendanceIn.ShiftTemplateSaveIn saveIn);

    AttendanceOut.ShiftTemplateOut updateShiftTemplate(Long id, AttendanceIn.ShiftTemplateSaveIn saveIn);

    void deleteShiftTemplate(Long id);

    List<AttendanceOut.ShiftTemplateOut> generateRecommendedShiftTemplates();
}

