/*
 * 文件名：AttendanceTenantService.java
 * 描述：考勤租户服务接口。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.tenant.service;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;

/**
 * 考勤租户服务接口。
 */
// 定义 考勤租户服务，承接当前文件对应的业务职责。
public interface AttendanceTenantService {

    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.TenantOut saveTenant(AttendanceIn.TenantSaveIn saveIn);
}

