/*
 * 文件名：AttendanceBootstrapController.java
 * 描述：考勤首页聚合控制器。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.bootstrap.controller;

import com.sp.selfsp.attendance.bootstrap.service.AttendanceBootstrapService;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考勤首页聚合控制器。
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceBootstrapController {

    private final AttendanceBootstrapService attendanceBootstrapService;

    /**
     * 构造考勤首页聚合控制器。
     *
     * @param attendanceBootstrapService 首页聚合服务
     */
    public AttendanceBootstrapController(AttendanceBootstrapService attendanceBootstrapService) {
        this.attendanceBootstrapService = attendanceBootstrapService;
    }

    /**
     * 读取首页聚合概览。
     *
     * @return 通用响应
     */
    @GetMapping("/bootstrap")
    public CommonResponse<AttendanceOut.BootstrapSummaryOut> bootstrap() {
        return CommonResponse.success(attendanceBootstrapService.getBootstrapSummary());
    }
}

