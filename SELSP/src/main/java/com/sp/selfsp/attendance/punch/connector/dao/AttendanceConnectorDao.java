package com.sp.selfsp.attendance.punch.connector.dao;

import com.sp.selfsp.attendance.punch.connector.domain.in.AttendanceConnectorIn;
import com.sp.selfsp.attendance.punch.connector.domain.out.AttendanceConnectorOut;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 第八阶段外部接入数据访问接口。
 */
@Mapper
public interface AttendanceConnectorDao {

    // 读取接入配置清单，供第八阶段页面展示当前租户已有连接器。
    List<Map<String, Object>> selectConnectorConfigs(
        @Param("tenantId") Long tenantId,
        @Param("query") AttendanceConnectorIn.ConnectorWorkbenchQueryIn queryIn
    );

    // 按主键读取一条接入配置，供保存后回读和测试连接复用。
    Map<String, Object> selectConnectorConfigById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 按来源系统读取接入配置，供 Webhook 接入和保存后按业务键回读复用。
    Map<String, Object> selectConnectorConfigBySourceSystem(@Param("tenantId") Long tenantId, @Param("sourceSystem") String sourceSystem);

    // 新增接入配置，供第八阶段正式沉淀外部接入定义。
    int insertConnectorConfig(
        @Param("tenantId") Long tenantId,
        @Param("in") AttendanceConnectorIn.ConnectorConfigSaveIn saveIn,
        @Param("configJson") String configJson
    );

    // 更新既有接入配置，供管理员修正密钥、接收方式和备注。
    int updateConnectorConfig(
        @Param("tenantId") Long tenantId,
        @Param("id") Long id,
        @Param("in") AttendanceConnectorIn.ConnectorConfigSaveIn saveIn,
        @Param("configJson") String configJson
    );

    // 读取员工外部映射清单，供第八阶段页面排查外部 ID 是否已绑定。
    List<AttendanceConnectorOut.ConnectorEmployeeMappingOut> selectConnectorMappings(
        @Param("tenantId") Long tenantId,
        @Param("query") AttendanceConnectorIn.ConnectorWorkbenchQueryIn queryIn
    );

    // 读取同步日志清单，供页面展示最近同步结果和失败原因。
    List<AttendanceConnectorOut.ConnectorSyncLogOut> selectConnectorSyncLogs(
        @Param("tenantId") Long tenantId,
        @Param("query") AttendanceConnectorIn.ConnectorWorkbenchQueryIn queryIn
    );

    // 按主键读取单条同步日志，供手工重试复原原始请求。
    AttendanceConnectorOut.ConnectorSyncLogOut selectConnectorSyncLogById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 新增同步日志，供 Webhook 或手工重试链路记录结果。
    int insertConnectorSyncLog(
        @Param("tenantId") Long tenantId,
        @Param("connectorId") Long connectorId,
        @Param("sourceSystem") String sourceSystem,
        @Param("triggerType") String triggerType,
        @Param("externalRequestId") String externalRequestId,
        @Param("syncStatus") String syncStatus,
        @Param("successCount") Integer successCount,
        @Param("failedCount") Integer failedCount,
        @Param("errorMessage") String errorMessage,
        @Param("requestSnapshot") String requestSnapshot,
        @Param("resultSnapshot") String resultSnapshot,
        @Param("retryFlag") Boolean retryFlag,
        @Param("retryCount") Integer retryCount
    );

    // 回写重试后的同步日志状态，供失败批次被修复后更新最终结论。
    int updateConnectorSyncLogRetryResult(
        @Param("tenantId") Long tenantId,
        @Param("id") Long id,
        @Param("syncStatus") String syncStatus,
        @Param("successCount") Integer successCount,
        @Param("failedCount") Integer failedCount,
        @Param("errorMessage") String errorMessage,
        @Param("resultSnapshot") String resultSnapshot
    );
}
