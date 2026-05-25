package com.sp.selfsp.attendance.workplace.service;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.util.List;

// 定义 考勤事业所服务，承接当前文件对应的业务职责。
public interface AttendanceWorkplaceService {

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    List<AttendanceOut.WorkplaceOut> listWorkplaces();

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.WorkplaceOut createWorkplace(AttendanceIn.WorkplaceSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.WorkplaceOut updateWorkplace(Long id, AttendanceIn.WorkplaceSaveIn saveIn);

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    void deleteWorkplace(Long id);
}

