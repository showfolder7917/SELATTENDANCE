/*
 * 文件名：AttendanceBootstrapController.java
 * 描述：考勤首页聚合控制器。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.bootstrap.controller;

import com.sp.selfsp.attendance.bootstrap.service.AttendanceBootstrapService;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考勤首页聚合控制器。
 */
// 把当前类注册为 Spring REST 控制器，负责对外暴露考勤接口。
@RestController
// 给当前控制器绑定统一接口前缀，便于前端按模块访问。
@RequestMapping("/api/attendance")
// 定义 考勤初始化聚合控制器，承接当前文件对应的业务职责。
public class AttendanceBootstrapController {

    // 声明 考勤初始化聚合服务 字段，用来保存当前业务状态或依赖。
    private final AttendanceBootstrapService attendanceBootstrapService;

    /**
     * 构造考勤首页聚合控制器。
     *
     * @param attendanceBootstrapService 首页聚合服务
     */
    // 定义 考勤初始化聚合控制器 接口入口，负责接收前端请求并转发到业务服务。
    public AttendanceBootstrapController(AttendanceBootstrapService attendanceBootstrapService) {
        // 把外部传入结果写入 考勤初始化聚合服务 字段，供后续流程继续使用。
        this.attendanceBootstrapService = attendanceBootstrapService;
    }

    /**
     * 读取首页聚合概览。
     *
     * @return 通用响应
     */
    // 把当前方法暴露为查询接口，供前端读取业务数据。
    @GetMapping("/bootstrap")
    // 定义 初始化聚合 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.BootstrapSummaryOut> bootstrap() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceBootstrapService.getBootstrapSummary());
    }
}

