/*
 * 文件名：AttendanceTenantController.java
 * 描述：考勤租户控制器。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.tenant.controller;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.tenant.service.AttendanceTenantService;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考勤租户控制器。
 */
// 把当前类注册为 Spring REST 控制器，负责对外暴露考勤接口。
@RestController
// 给当前控制器绑定统一接口前缀，便于前端按模块访问。
@RequestMapping("/api/attendance/tenant")
// 定义 考勤租户控制器，承接当前文件对应的业务职责。
public class AttendanceTenantController {

    // 声明 考勤租户服务 字段，用来保存当前业务状态或依赖。
    private final AttendanceTenantService attendanceTenantService;

    // 定义 考勤租户控制器 接口入口，负责接收前端请求并转发到业务服务。
    public AttendanceTenantController(AttendanceTenantService attendanceTenantService) {
        // 把外部传入结果写入 考勤租户服务 字段，供后续流程继续使用。
        this.attendanceTenantService = attendanceTenantService;
    }

    /**
     * 保存当前租户资料。
     *
     * @param saveIn 保存入参
     * @return 通用响应
     */
    // 把当前方法暴露为更新接口，供前端保存修改结果。
    @PutMapping("/current")
    // 定义 保存租户 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.TenantOut> saveTenant(@RequestBody AttendanceIn.TenantSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceTenantService.saveTenant(saveIn));
    }
}
