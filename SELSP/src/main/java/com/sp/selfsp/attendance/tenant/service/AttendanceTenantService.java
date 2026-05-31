/*
 * AttendanceTenantService.java
 * 租户服务接口。
 */
package com.sp.selfsp.attendance.tenant.service;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;

/**
 * 租户服务接口。
 */
public interface AttendanceTenantService {

    // 读取当前租户资料，供轻量首页壳和租户面板初始化直接复用。
    AttendanceOut.TenantOut getCurrentTenant();

    // 保存当前租户资料，供首页租户面板回写基础主数据。
    AttendanceOut.TenantOut saveTenant(AttendanceIn.TenantSaveIn saveIn);
}
