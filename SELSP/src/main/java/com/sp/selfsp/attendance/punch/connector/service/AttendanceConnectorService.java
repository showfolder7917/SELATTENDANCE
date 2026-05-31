package com.sp.selfsp.attendance.punch.connector.service;

import com.sp.selfsp.attendance.punch.connector.domain.in.AttendanceConnectorIn;
import com.sp.selfsp.attendance.punch.connector.domain.out.AttendanceConnectorOut;
import com.sp.selfsp.attendance.punch.domain.in.AttendancePunchIn;
import com.sp.selfsp.attendance.punch.domain.out.AttendancePunchOut;

/**
 * 第八阶段外部打卡接入服务接口。
 */
public interface AttendanceConnectorService {

    // 读取第八阶段工作台聚合结果，供连接器、映射和同步日志共用同一批数据。
    AttendanceConnectorOut.ConnectorWorkbenchOut getConnectorWorkbench(AttendanceConnectorIn.ConnectorWorkbenchQueryIn queryIn);

    // 保存外部接入配置，供管理员正式登记或修正第三方接入定义。
    AttendanceConnectorOut.ConnectorConfigOut saveConnector(AttendanceConnectorIn.ConnectorConfigSaveIn saveIn);

    // 测试指定连接器是否具备最小可用条件，供页面先验证配置是否完整。
    AttendanceConnectorOut.ConnectorTestResultOut testConnector(Long id);

    // 接收带来源系统路径的正式 Webhook，供第三方平台推送原始打卡时写同步日志并进入第三阶段。
    AttendancePunchOut.PunchManualResultOut receiveWebhook(String sourceSystem, String providedSecret, AttendancePunchIn.PunchWebhookIn saveIn);

    // 手工重试一条失败同步日志，供管理员修正映射或配置后重新进入原始打卡链路。
    AttendanceConnectorOut.ConnectorSyncLogOut retrySyncLog(Long id);
}
