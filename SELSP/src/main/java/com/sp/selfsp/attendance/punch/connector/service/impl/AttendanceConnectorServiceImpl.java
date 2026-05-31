package com.sp.selfsp.attendance.punch.connector.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.punch.connector.dao.AttendanceConnectorDao;
import com.sp.selfsp.attendance.punch.connector.domain.in.AttendanceConnectorIn;
import com.sp.selfsp.attendance.punch.connector.domain.out.AttendanceConnectorOut;
import com.sp.selfsp.attendance.punch.connector.service.AttendanceConnectorService;
import com.sp.selfsp.attendance.punch.domain.in.AttendancePunchIn;
import com.sp.selfsp.attendance.punch.domain.out.AttendancePunchOut;
import java.sql.Clob;
import java.sql.Timestamp;
import com.sp.selfsp.attendance.punch.service.AttendancePunchService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 第八阶段外部打卡接入服务实现。
 *
 * <p>负责把接入配置、同步日志和正式 Webhook 入口收口到同一条“先接入、后进入第三阶段原始打卡”的链路。</p>
 */
@Service
public class AttendanceConnectorServiceImpl implements AttendanceConnectorService {

    // 工作台默认用这个基础路径拼接 Webhook 地址，方便管理员复制给第三方平台。
    private static final String WEBHOOK_PATH_PREFIX = "/api/attendance/connectors/webhook/";

    // 接入配置和同步日志都从这组 DAO 读取与回写。
    private final AttendanceConnectorDao attendanceConnectorDao;
    // 第三阶段原始打卡服务继续负责真正落库和员工匹配。
    private final AttendancePunchService attendancePunchService;
    // JSON 工具用于保存配置扩展字段和同步日志快照。
    private final ObjectMapper objectMapper;

    public AttendanceConnectorServiceImpl(
        AttendanceConnectorDao attendanceConnectorDao,
        AttendancePunchService attendancePunchService,
        ObjectMapper objectMapper
    ) {
        this.attendanceConnectorDao = attendanceConnectorDao;
        this.attendancePunchService = attendancePunchService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AttendanceConnectorOut.ConnectorWorkbenchOut getConnectorWorkbench(AttendanceConnectorIn.ConnectorWorkbenchQueryIn queryIn) {
        // 工作台三块数据共用同一组筛选，避免前端自己拼接不同来源的聚合口径。
        AttendanceConnectorIn.ConnectorWorkbenchQueryIn normalizedQuery = normalizeWorkbenchQuery(queryIn);
        List<AttendanceConnectorOut.ConnectorConfigOut> connectors = mapConnectorConfigs(
            attendanceConnectorDao.selectConnectorConfigs(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery)
        );
        List<AttendanceConnectorOut.ConnectorEmployeeMappingOut> mappings =
            attendanceConnectorDao.selectConnectorMappings(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery);
        List<AttendanceConnectorOut.ConnectorSyncLogOut> syncLogs =
            attendanceConnectorDao.selectConnectorSyncLogs(AttendanceTenantContext.DEFAULT_TENANT_ID, normalizedQuery);
        AttendanceConnectorOut.ConnectorWorkbenchOut workbenchOut = new AttendanceConnectorOut.ConnectorWorkbenchOut();
        workbenchOut.setConnectors(connectors);
        workbenchOut.setMappings(mappings);
        workbenchOut.setSyncLogs(syncLogs);
        workbenchOut.setSummary(buildSummary(connectors, mappings, syncLogs));
        return workbenchOut;
    }

    @Override
    @Transactional
    public AttendanceConnectorOut.ConnectorConfigOut saveConnector(AttendanceConnectorIn.ConnectorConfigSaveIn saveIn) {
        // 保存前先校验接入主键、名称和接收方式，避免生成无法解释的连接器配置。
        validateConnectorSaveIn(saveIn);
        normalizeConnectorSaveIn(saveIn);
        String configJson = buildConfigJson(saveIn);
        if (saveIn.getId() == null) {
            // 新建时直接写入一条正式接入配置，供后续 Webhook 或 Pull 引用。
            attendanceConnectorDao.insertConnectorConfig(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn, configJson);
        } else {
            // 编辑时按主键覆盖既有配置，确保管理员改的是当前这条连接器。
            requireConnectorConfigById(saveIn.getId());
            attendanceConnectorDao.updateConnectorConfig(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn.getId(), saveIn, configJson);
        }
        // 保存后按来源系统回读正式配置，保证前端拿到数据库中的最终快照。
        return mapConnectorConfig(requireConnectorConfigBySourceSystem(saveIn.getSourceSystem()));
    }

    @Override
    public AttendanceConnectorOut.ConnectorTestResultOut testConnector(Long id) {
        // 测试连接首先要锁定真实存在的接入配置，否则页面会对着不存在的主键提示成功。
        AttendanceConnectorOut.ConnectorConfigOut configOut = mapConnectorConfig(requireConnectorConfigById(id));
        AttendanceConnectorOut.ConnectorTestResultOut resultOut = new AttendanceConnectorOut.ConnectorTestResultOut();
        resultOut.setWebhookUrl(configOut.getWebhookUrl());
        if (!Boolean.TRUE.equals(configOut.getActiveFlag())) {
            // 未启用配置时先返回明确失败，避免用户误以为测试成功就会开始接数。
            resultOut.setSuccess(false);
            resultOut.setMessage("接入已停用，启用后再测试连接");
            return resultOut;
        }
        if ("PULL".equals(configOut.getReceiveMode())) {
            // Pull 型连接器至少要有 API 地址和 API Key，否则后续无法主动拉取。
            boolean ready = StringUtils.hasText(configOut.getApiBaseUrl()) && !"未配置".equals(configOut.getApiKeyMasked());
            resultOut.setSuccess(ready);
            resultOut.setMessage(ready ? "Pull 接入基础配置已完整，可进入后续定时同步实现" : "Pull 模式至少需要 API 地址和 API Key");
            return resultOut;
        }
        if ("CSV".equals(configOut.getReceiveMode())) {
            // CSV 型接入不依赖远端连通性，只需确认本地接入定义已保存。
            resultOut.setSuccess(true);
            resultOut.setMessage("CSV 接入无需远端握手，当前配置已可用于人工导入");
            return resultOut;
        }
        // Webhook 型接入只要名称和来源系统已保存，就可以生成正式回调地址供管理员配置到第三方平台。
        resultOut.setSuccess(true);
        resultOut.setMessage("Webhook 地址已生成，可复制到第三方平台并开始推送打卡");
        return resultOut;
    }

    @Override
    @Transactional
    public AttendancePunchOut.PunchManualResultOut receiveWebhook(String sourceSystem, String providedSecret, AttendancePunchIn.PunchWebhookIn saveIn) {
        // 按路径来源系统锁定当前接入配置，避免第三方误把请求打到不存在的连接器上。
        Map<String, Object> row = requireConnectorConfigBySourceSystem(sourceSystem);
        AttendanceConnectorOut.ConnectorConfigOut configOut = mapConnectorConfig(row);
        if (!Boolean.TRUE.equals(configOut.getActiveFlag())) {
            throw new IllegalArgumentException("当前接入已停用");
        }
        AttendancePunchIn.PunchWebhookIn normalizedWebhook = normalizeWebhookInput(sourceSystem, saveIn);
        String requestSnapshot = toJsonString(normalizedWebhook);
        String expectedSecret = stringValue(readRowValue(row, "webhookSecret"));
        if (StringUtils.hasText(expectedSecret) && !Objects.equals(expectedSecret, normalizeText(providedSecret))) {
            // 签名失败也要入同步日志，便于管理员知道第三方确实打过来但被本系统拦住了。
            attendanceConnectorDao.insertConnectorSyncLog(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                configOut.getId(),
                configOut.getSourceSystem(),
                "WEBHOOK",
                normalizedWebhook.getSourceEventId(),
                "FAILED",
                0,
                1,
                "Webhook Secret 校验失败",
                requestSnapshot,
                toJsonString(Map.of("message", "Webhook Secret 校验失败")),
                false,
                0
            );
            throw new IllegalArgumentException("Webhook Secret 校验失败");
        }
        // 真正的原始打卡落库仍交给第三阶段服务，保持“外部接入只带数据进来”的阶段边界。
        AttendancePunchOut.PunchManualResultOut resultOut = attendancePunchService.receiveWebhook(normalizedWebhook);
        boolean success = Objects.equals("PROCESSED", resultOut.getProcessStatus());
        attendanceConnectorDao.insertConnectorSyncLog(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            configOut.getId(),
            configOut.getSourceSystem(),
            "WEBHOOK",
            normalizedWebhook.getSourceEventId(),
            success ? "SUCCESS" : "FAILED",
            success ? 1 : 0,
            success ? 0 : 1,
            success ? null : "原始打卡接入后未能完成处理，请检查员工映射或数据格式",
            requestSnapshot,
            toJsonString(Map.of(
                "logId", resultOut.getId(),
                "processStatus", resultOut.getProcessStatus(),
                "messageCode", resultOut.getMessageCode()
            )),
            false,
            0
        );
        return resultOut;
    }

    @Override
    @Transactional
    public AttendanceConnectorOut.ConnectorSyncLogOut retrySyncLog(Long id) {
        // 重试必须建立在一条真实存在的同步日志之上，否则无法复原原始请求。
        AttendanceConnectorOut.ConnectorSyncLogOut logOut = requireSyncLog(id);
        AttendancePunchIn.PunchWebhookIn webhookIn = parseWebhookSnapshot(logOut);
        // 手工重试仍复用第三阶段 Webhook 处理逻辑，只是来源改成管理员主动重放。
        AttendancePunchOut.PunchManualResultOut resultOut = attendancePunchService.receiveWebhook(webhookIn);
        boolean success = Objects.equals("PROCESSED", resultOut.getProcessStatus());
        attendanceConnectorDao.updateConnectorSyncLogRetryResult(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            id,
            success ? "SUCCESS" : "FAILED",
            success ? 1 : 0,
            success ? 0 : 1,
            success ? null : "重试后仍未完成处理，请继续检查映射或事件去重状态",
            toJsonString(Map.of(
                "logId", resultOut.getId(),
                "processStatus", resultOut.getProcessStatus(),
                "messageCode", resultOut.getMessageCode()
            ))
        );
        return requireSyncLog(id);
    }

    private AttendanceConnectorIn.ConnectorWorkbenchQueryIn normalizeWorkbenchQuery(AttendanceConnectorIn.ConnectorWorkbenchQueryIn queryIn) {
        AttendanceConnectorIn.ConnectorWorkbenchQueryIn normalized = queryIn == null
            ? new AttendanceConnectorIn.ConnectorWorkbenchQueryIn()
            : queryIn;
        normalized.setSourceSystem(normalizeText(normalized.getSourceSystem()));
        normalized.setKeyword(normalizeText(normalized.getKeyword()));
        return normalized;
    }

    private void validateConnectorSaveIn(AttendanceConnectorIn.ConnectorConfigSaveIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("connectorSaveIn 不能为空");
        }
        requireText(saveIn.getSourceSystem(), "sourceSystem 不能为空");
        requireText(saveIn.getConnectorName(), "connectorName 不能为空");
        requireText(saveIn.getProviderType(), "providerType 不能为空");
        requireText(saveIn.getReceiveMode(), "receiveMode 不能为空");
    }

    private void normalizeConnectorSaveIn(AttendanceConnectorIn.ConnectorConfigSaveIn saveIn) {
        saveIn.setSourceSystem(saveIn.getSourceSystem().trim().toUpperCase(Locale.ROOT));
        saveIn.setConnectorName(saveIn.getConnectorName().trim());
        saveIn.setProviderType(saveIn.getProviderType().trim().toUpperCase(Locale.ROOT));
        saveIn.setReceiveMode(saveIn.getReceiveMode().trim().toUpperCase(Locale.ROOT));
        saveIn.setApiBaseUrl(normalizeText(saveIn.getApiBaseUrl()));
        saveIn.setApiKey(normalizeText(saveIn.getApiKey()));
        saveIn.setApiSecret(normalizeText(saveIn.getApiSecret()));
        saveIn.setWebhookSecret(normalizeText(saveIn.getWebhookSecret()));
        saveIn.setSyncCron(normalizeText(saveIn.getSyncCron()));
        saveIn.setNote(normalizeText(saveIn.getNote()));
        if (saveIn.getActiveFlag() == null) {
            saveIn.setActiveFlag(Boolean.TRUE);
        }
    }

    private String buildConfigJson(AttendanceConnectorIn.ConnectorConfigSaveIn saveIn) {
        Map<String, Object> config = new LinkedHashMap<>();
        // 这些扩展字段当前还没有正式拆成独立列，因此统一收在 config_json 里保留结构化语义。
        config.put("providerType", saveIn.getProviderType());
        config.put("receiveMode", saveIn.getReceiveMode());
        config.put("syncCron", saveIn.getSyncCron());
        config.put("workplaceId", saveIn.getWorkplaceId());
        config.put("note", saveIn.getNote());
        return toJsonString(config);
    }

    private List<AttendanceConnectorOut.ConnectorConfigOut> mapConnectorConfigs(List<Map<String, Object>> rows) {
        List<AttendanceConnectorOut.ConnectorConfigOut> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(mapConnectorConfig(row));
        }
        return result;
    }

    private AttendanceConnectorOut.ConnectorConfigOut mapConnectorConfig(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        Map<String, Object> config = parseConfigJson(stringValue(readRowValue(row, "configJson")));
        AttendanceConnectorOut.ConnectorConfigOut out = new AttendanceConnectorOut.ConnectorConfigOut();
        out.setId(longValue(readRowValue(row, "id")));
        out.setSourceSystem(stringValue(readRowValue(row, "sourceSystem")));
        out.setConnectorName(stringValue(readRowValue(row, "connectorName")));
        out.setProviderType(defaultText(stringValue(config.get("providerType")), "CUSTOM_WEBHOOK"));
        out.setReceiveMode(defaultText(stringValue(config.get("receiveMode")), "WEBHOOK"));
        out.setApiBaseUrl(stringValue(readRowValue(row, "apiBaseUrl")));
        out.setApiKeyMasked(maskSecret(stringValue(readRowValue(row, "apiKey"))));
        out.setApiSecretMasked(maskSecret(stringValue(readRowValue(row, "apiSecret"))));
        out.setWebhookSecretMasked(maskSecret(stringValue(readRowValue(row, "webhookSecret"))));
        out.setSyncCron(stringValue(config.get("syncCron")));
        out.setWorkplaceId(longValue(config.get("workplaceId")));
        out.setActiveFlag(booleanValue(readRowValue(row, "activeFlag")));
        out.setNote(stringValue(config.get("note")));
        out.setWebhookUrl(buildWebhookUrl(out.getSourceSystem()));
        out.setCreatedAt(localDateTimeValue(readRowValue(row, "createdAt")));
        out.setUpdatedAt(localDateTimeValue(readRowValue(row, "updatedAt")));
        return out;
    }

    private AttendanceConnectorOut.ConnectorSummaryOut buildSummary(
        List<AttendanceConnectorOut.ConnectorConfigOut> connectors,
        List<AttendanceConnectorOut.ConnectorEmployeeMappingOut> mappings,
        List<AttendanceConnectorOut.ConnectorSyncLogOut> syncLogs
    ) {
        int activeConnectorCount = 0;
        for (AttendanceConnectorOut.ConnectorConfigOut connector : connectors) {
            if (Boolean.TRUE.equals(connector.getActiveFlag())) {
                activeConnectorCount += 1;
            }
        }
        int failedSyncCount = 0;
        LocalDateTime latestSyncAt = null;
        for (AttendanceConnectorOut.ConnectorSyncLogOut logOut : syncLogs) {
            if (!Objects.equals("SUCCESS", logOut.getSyncStatus())) {
                failedSyncCount += 1;
            }
            if (latestSyncAt == null || (logOut.getCreatedAt() != null && logOut.getCreatedAt().isAfter(latestSyncAt))) {
                latestSyncAt = logOut.getCreatedAt();
            }
        }
        AttendanceConnectorOut.ConnectorSummaryOut summaryOut = new AttendanceConnectorOut.ConnectorSummaryOut();
        summaryOut.setActiveConnectorCount(activeConnectorCount);
        summaryOut.setMappedEmployeeCount(mappings.size());
        summaryOut.setFailedSyncCount(failedSyncCount);
        summaryOut.setLatestSyncAt(latestSyncAt);
        return summaryOut;
    }

    private Map<String, Object> requireConnectorConfigById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("connectorId 非法");
        }
        Map<String, Object> row = attendanceConnectorDao.selectConnectorConfigById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("接入配置不存在");
        }
        return row;
    }

    private Map<String, Object> requireConnectorConfigBySourceSystem(String sourceSystem) {
        requireText(sourceSystem, "sourceSystem 不能为空");
        Map<String, Object> row = attendanceConnectorDao.selectConnectorConfigBySourceSystem(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            sourceSystem.trim().toUpperCase(Locale.ROOT)
        );
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("接入配置不存在");
        }
        return row;
    }

    private AttendanceConnectorOut.ConnectorSyncLogOut requireSyncLog(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("syncLogId 非法");
        }
        AttendanceConnectorOut.ConnectorSyncLogOut logOut =
            attendanceConnectorDao.selectConnectorSyncLogById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        if (logOut == null) {
            throw new IllegalArgumentException("同步日志不存在");
        }
        return logOut;
    }

    private AttendancePunchIn.PunchWebhookIn normalizeWebhookInput(String sourceSystem, AttendancePunchIn.PunchWebhookIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("webhook 请求不能为空");
        }
        AttendancePunchIn.PunchWebhookIn normalized = new AttendancePunchIn.PunchWebhookIn();
        // 路径来源系统是第八阶段正式接入的业务键，因此不允许由 body 覆盖。
        normalized.setSourceSystem(sourceSystem.trim().toUpperCase(Locale.ROOT));
        normalized.setTenantCode(normalizeText(saveIn.getTenantCode()));
        normalized.setSourceEventId(normalizeText(saveIn.getSourceEventId()));
        normalized.setExternalEmployeeId(normalizeText(saveIn.getExternalEmployeeId()));
        normalized.setPunchTime(normalizeText(saveIn.getPunchTime()));
        normalized.setPunchType(normalizeText(saveIn.getPunchType()));
        normalized.setDeviceId(normalizeText(saveIn.getDeviceId()));
        normalized.setDeviceName(normalizeText(saveIn.getDeviceName()));
        normalized.setRawData(saveIn.getRawData());
        return normalized;
    }

    private AttendancePunchIn.PunchWebhookIn parseWebhookSnapshot(AttendanceConnectorOut.ConnectorSyncLogOut logOut) {
        try {
            AttendancePunchIn.PunchWebhookIn webhookIn = objectMapper.readValue(
                defaultText(logOut.getRequestSnapshot(), "{}"),
                AttendancePunchIn.PunchWebhookIn.class
            );
            webhookIn.setSourceSystem(defaultText(webhookIn.getSourceSystem(), logOut.getSourceSystem()));
            return webhookIn;
        } catch (JsonProcessingException error) {
            throw new IllegalArgumentException("同步日志快照无法解析，无法重试");
        }
    }

    private Map<String, Object> parseConfigJson(String configJson) {
        if (!StringUtils.hasText(configJson)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException error) {
            throw new IllegalArgumentException("接入配置 JSON 无法解析");
        }
    }

    private String buildWebhookUrl(String sourceSystem) {
        return WEBHOOK_PATH_PREFIX + defaultText(sourceSystem, "UNKNOWN");
    }

    private String maskSecret(String value) {
        if (!StringUtils.hasText(value)) {
            return "未配置";
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    private String toJsonString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException error) {
            throw new IllegalArgumentException("JSON 序列化失败");
        }
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Clob clob) {
            try {
                // H2 会把 CLOB 字段回成 Clob 对象，这里必须读正文而不是对象描述。
                return clob.getSubString(1, (int) clob.length());
            } catch (Exception error) {
                throw new IllegalArgumentException("CLOB 字段读取失败");
            }
        }
        return String.valueOf(value);
    }

    private Object readRowValue(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private LocalDateTime localDateTimeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
    }

    private Boolean booleanValue(Object value) {
        if (value == null) {
            return Boolean.FALSE;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return "true".equalsIgnoreCase(String.valueOf(value)) || "1".equals(String.valueOf(value));
    }
}
