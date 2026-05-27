/*
 * AttendanceTenantController.java
 * 租户控制器。
 */
package com.sp.selfsp.attendance.tenant.controller;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.tenant.service.AttendanceTenantService;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租户控制器。
 */
@RestController
@RequestMapping("/api/attendance/tenant")
public class AttendanceTenantController {

    // 处理当前租户读写请求，供轻量首页壳和租户面板共用。
    private final AttendanceTenantService attendanceTenantService;

    // 注入租户服务，统一承接当前租户读取和保存用例。
    public AttendanceTenantController(AttendanceTenantService attendanceTenantService) {
        // 保存租户服务引用，供当前控制器全部接口复用。
        this.attendanceTenantService = attendanceTenantService;
    }

    // 提供当前租户读取接口，供轻量 bootstrap 之外的独立租户初始化直接调用。
    @GetMapping("/current")
    public CommonResponse<AttendanceOut.TenantOut> getCurrentTenant() {
        // 直接返回当前租户资料，避免前端为了租户表单再走重聚合接口。
        return CommonResponse.success(attendanceTenantService.getCurrentTenant());
    }

    // 提供当前租户保存接口，供首页租户面板回写基础主数据。
    @PutMapping("/current")
    public CommonResponse<AttendanceOut.TenantOut> saveTenant(@RequestBody AttendanceIn.TenantSaveIn saveIn) {
        // 返回保存后的最新租户资料，保证前端面板与数据库状态一致。
        return CommonResponse.success(attendanceTenantService.saveTenant(saveIn));
    }
}
