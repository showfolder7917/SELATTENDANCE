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
// 定义 考勤初始化聚合服务，承接当前文件对应的业务职责。
public interface AttendanceBootstrapService {

    /**
     * 读取首页聚合概览。
     *
     * @return 首页聚合结果
     */
    // 执行当前业务步骤，推进本行对应的 服务 处理。
    AttendanceOut.BootstrapSummaryOut getBootstrapSummary();
}

