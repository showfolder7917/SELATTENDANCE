package com.sp.selfsp.attendance.punch.controller;

import com.sp.selfsp.attendance.punch.domain.in.AttendancePunchIn;
import com.sp.selfsp.attendance.punch.domain.out.AttendancePunchOut;
import com.sp.selfsp.attendance.punch.service.AttendancePunchService;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第三阶段打卡控制器。
 */
// 把当前类注册为 Spring REST 控制器，负责对外暴露考勤接口。
@RestController
// 给当前控制器绑定统一接口前缀，便于前端按模块访问。
@RequestMapping("/api/attendance/punch")
// 定义 考勤打卡控制器，承接当前文件对应的业务职责。
public class AttendancePunchController {

    // 声明 考勤打卡服务 字段，用来保存当前业务状态或依赖。
    private final AttendancePunchService attendancePunchService;

    // 定义 考勤打卡控制器 接口入口，负责接收前端请求并转发到业务服务。
    public AttendancePunchController(AttendancePunchService attendancePunchService) {
        // 把外部传入结果写入 考勤打卡服务 字段，供后续流程继续使用。
        this.attendancePunchService = attendancePunchService;
    }

    // 把当前方法暴露为查询接口，供前端读取打卡列表。
    @GetMapping("/logs")
    // 定义 查询打卡记录 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendancePunchOut.PunchLogListOut> listPunchLogs(
        @RequestParam(required = false) String dateFrom,
        @RequestParam(required = false) String dateTo,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) String sourceSystem,
        @RequestParam(required = false) String processStatus,
        @RequestParam(required = false) String punchType,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        // 组装查询对象，统一传给第三阶段服务层处理过滤和分页口径。
        AttendancePunchIn.PunchLogQueryIn queryIn = new AttendancePunchIn.PunchLogQueryIn();
        queryIn.setDateFrom(dateFrom);
        queryIn.setDateTo(dateTo);
        queryIn.setEmployeeKeyword(employeeKeyword);
        queryIn.setSourceSystem(sourceSystem);
        queryIn.setProcessStatus(processStatus);
        queryIn.setPunchType(punchType);
        queryIn.setPage(page);
        queryIn.setPageSize(pageSize);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendancePunchService.listPunchLogs(queryIn));
    }

    // 把当前方法暴露为查询接口，供前端读取单条打卡详情。
    @GetMapping("/logs/{id}")
    // 定义 查询打卡详情 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendancePunchOut.PunchLogDetailOut> getPunchLogDetail(@PathVariable Long id) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendancePunchService.getPunchLogDetail(id));
    }

    // 把当前方法暴露为新增接口，供管理员手动补录打卡。
    @PostMapping("/manual")
    // 定义 手动补录打卡 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendancePunchOut.PunchManualResultOut> createManualPunch(@RequestBody AttendancePunchIn.PunchManualSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendancePunchService.createManualPunch(saveIn));
    }

    // 把当前方法暴露为新增接口，供前端先预览 CSV 解析结果。
    @PostMapping("/import-csv/preview")
    // 定义 预览打卡CSV 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendancePunchOut.PunchImportPreviewOut> previewImport(@RequestBody AttendancePunchIn.PunchImportIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendancePunchService.previewImport(saveIn));
    }

    // 把当前方法暴露为新增接口，供前端正式提交 CSV 导入。
    @PostMapping("/import-csv")
    // 定义 导入打卡CSV 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendancePunchOut.PunchImportResultOut> importCsv(@RequestBody AttendancePunchIn.PunchImportIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendancePunchService.importCsv(saveIn));
    }

    // 把当前方法暴露为新增接口，供网关把 Webhook 打卡事件推入系统。
    @PostMapping("/webhook")
    // 定义 接收Webhook打卡 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendancePunchOut.PunchManualResultOut> receiveWebhook(@RequestBody AttendancePunchIn.PunchWebhookIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendancePunchService.receiveWebhook(saveIn));
    }

    // 把当前方法暴露为新增接口，供未匹配记录绑定已有员工。
    @PostMapping("/logs/{id}/bind-employee")
    // 定义 绑定打卡员工 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendancePunchOut.PunchLogDetailOut> bindEmployee(
        @PathVariable Long id,
        @RequestBody AttendancePunchIn.PunchBindEmployeeIn saveIn
    ) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendancePunchService.bindEmployee(id, saveIn));
    }

    // 把当前方法暴露为新增接口，供误导入记录进入忽略状态。
    @PostMapping("/logs/{id}/ignore")
    // 定义 忽略打卡记录 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendancePunchOut.PunchLogDetailOut> ignoreLog(
        @PathVariable Long id,
        @RequestBody(required = false) AttendancePunchIn.PunchIgnoreIn saveIn
    ) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendancePunchService.ignoreLog(id, saveIn));
    }

    // 把当前方法暴露为新增接口，供绑定完成后重新处理既有记录。
    @PostMapping("/logs/{id}/reprocess")
    // 定义 重新处理打卡记录 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendancePunchOut.PunchLogDetailOut> reprocessLog(@PathVariable Long id) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendancePunchService.reprocessLog(id));
    }
}
