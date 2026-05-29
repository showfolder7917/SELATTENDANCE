/*
 * 文件名：AttendanceBootstrapService.java
 * 描述：考勤首页聚合服务接口。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.bootstrap.service;

import com.sp.selfsp.attendance.domain.out.AttendanceOut;

/**
 * 考勤首页聚合服务接口。
 */
public interface AttendanceBootstrapService {

    /**
     * 读取首页聚合概览。
     *
     * @return 首页聚合结果
     */
    AttendanceOut.BootstrapSummaryOut getBootstrapSummary();
}

