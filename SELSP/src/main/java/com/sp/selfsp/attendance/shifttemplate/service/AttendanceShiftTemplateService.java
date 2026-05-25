package com.sp.selfsp.attendance.shifttemplate.service;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.util.List;

// 定义 考勤班次模板服务，承接当前文件对应的业务职责。
public interface AttendanceShiftTemplateService {

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    List<AttendanceOut.ShiftTemplateOut> listShiftTemplates();

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.ShiftTemplateOut createShiftTemplate(AttendanceIn.ShiftTemplateSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.ShiftTemplateOut updateShiftTemplate(Long id, AttendanceIn.ShiftTemplateSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    void deleteShiftTemplate(Long id);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    List<AttendanceOut.ShiftTemplateOut> generateRecommendedShiftTemplates();
}

