package com.sp.selfsp.attendance.punch.connector.controller;

import com.sp.selfsp.attendance.punch.connector.domain.in.AttendanceConnectorIn;
import com.sp.selfsp.attendance.punch.connector.domain.out.AttendanceConnectorOut;
import com.sp.selfsp.attendance.punch.connector.service.AttendanceConnectorService;
import com.sp.selfsp.attendance.punch.domain.in.AttendancePunchIn;
import com.sp.selfsp.attendance.punch.domain.out.AttendancePunchOut;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第八阶段外部打卡接入控制器。
 */
@RestController
@RequestMapping("/api/attendance/connectors")
public class AttendanceConnectorController {

    private final AttendanceConnectorService attendanceConnectorService;

    public AttendanceConnectorController(AttendanceConnectorService attendanceConnectorService) {
        this.attendanceConnectorService = attendanceConnectorService;
    }

    @GetMapping
    public CommonResponse<AttendanceConnectorOut.ConnectorWorkbenchOut> getWorkbench(
        @RequestParam(required = false) String sourceSystem,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean activeOnly
    ) {
        // 把头部筛选统一组装成第八阶段查询对象，交给服务层一次性返回工作台三块数据。
        AttendanceConnectorIn.ConnectorWorkbenchQueryIn queryIn = new AttendanceConnectorIn.ConnectorWorkbenchQueryIn();
        queryIn.setSourceSystem(sourceSystem);
        queryIn.setKeyword(keyword);
        queryIn.setActiveOnly(activeOnly);
        return CommonResponse.success(attendanceConnectorService.getConnectorWorkbench(queryIn));
    }

    @PostMapping
    public CommonResponse<AttendanceConnectorOut.ConnectorConfigOut> createConnector(@RequestBody AttendanceConnectorIn.ConnectorConfigSaveIn saveIn) {
        return CommonResponse.success(attendanceConnectorService.saveConnector(saveIn));
    }

    @PutMapping("/{id}")
    public CommonResponse<AttendanceConnectorOut.ConnectorConfigOut> updateConnector(
        @PathVariable Long id,
        @RequestBody AttendanceConnectorIn.ConnectorConfigSaveIn saveIn
    ) {
        // 路径主键是当前正在编辑的正式连接器，因此由控制器统一回写到保存对象。
        saveIn.setId(id);
        return CommonResponse.success(attendanceConnectorService.saveConnector(saveIn));
    }

    @PostMapping("/{id}/test")
    public CommonResponse<AttendanceConnectorOut.ConnectorTestResultOut> testConnector(@PathVariable Long id) {
        return CommonResponse.success(attendanceConnectorService.testConnector(id));
    }

    @PostMapping("/webhook/{sourceSystem}")
    public CommonResponse<AttendancePunchOut.PunchManualResultOut> receiveWebhook(
        @PathVariable String sourceSystem,
        @RequestHeader(value = "X-Webhook-Secret", required = false) String webhookSecret,
        @RequestBody AttendancePunchIn.PunchWebhookIn saveIn
    ) {
        // 第八阶段正式 Webhook 入口会同时校验签名、记录同步日志并进入第三阶段原始打卡服务。
        return CommonResponse.success(attendanceConnectorService.receiveWebhook(sourceSystem, webhookSecret, saveIn));
    }

    @PostMapping("/sync-logs/{id}/retry")
    public CommonResponse<AttendanceConnectorOut.ConnectorSyncLogOut> retrySyncLog(@PathVariable Long id) {
        return CommonResponse.success(attendanceConnectorService.retrySyncLog(id));
    }
}
