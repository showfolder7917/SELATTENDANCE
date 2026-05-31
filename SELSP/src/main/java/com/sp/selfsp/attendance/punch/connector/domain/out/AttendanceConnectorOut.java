package com.sp.selfsp.attendance.punch.connector.domain.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 第八阶段外部打卡接入输出对象集合。
 */
public final class AttendanceConnectorOut {

    private AttendanceConnectorOut() {
    }

    /**
     * 外部接入工作台聚合输出。
     */
    public static class ConnectorWorkbenchOut {

        // 接入配置列表用于展示当前租户所有正式接入定义。
        private List<ConnectorConfigOut> connectors;
        // 员工映射列表用于告诉管理员哪些人已经能被第三方打卡识别。
        private List<ConnectorEmployeeMappingOut> mappings;
        // 同步日志列表用于定位最近哪条同步成功、失败或需要重试。
        private List<ConnectorSyncLogOut> syncLogs;
        // 汇总卡用于头部先看启用数量、映射数量和失败风险。
        private ConnectorSummaryOut summary;

        public List<ConnectorConfigOut> getConnectors() {
            return connectors;
        }

        public void setConnectors(List<ConnectorConfigOut> connectors) {
            this.connectors = connectors;
        }

        public List<ConnectorEmployeeMappingOut> getMappings() {
            return mappings;
        }

        public void setMappings(List<ConnectorEmployeeMappingOut> mappings) {
            this.mappings = mappings;
        }

        public List<ConnectorSyncLogOut> getSyncLogs() {
            return syncLogs;
        }

        public void setSyncLogs(List<ConnectorSyncLogOut> syncLogs) {
            this.syncLogs = syncLogs;
        }

        public ConnectorSummaryOut getSummary() {
            return summary;
        }

        public void setSummary(ConnectorSummaryOut summary) {
            this.summary = summary;
        }
    }

    /**
     * 接入配置输出。
     */
    public static class ConnectorConfigOut {

        // 主键用于页面锁定当前编辑的是哪条接入配置。
        private Long id;
        // 来源系统编码用于和原始打卡、员工映射以及日志串联。
        private String sourceSystem;
        // 接入名称用于页面清单和日志描述。
        private String connectorName;
        // 平台类型用于说明这是自定义 Webhook 还是供应商 API。
        private String providerType;
        // 接收方式用于提示管理员这条接入靠什么方式进入系统。
        private String receiveMode;
        // API 地址用于页面回显和测试连接说明。
        private String apiBaseUrl;
        // API Key 掩码只回显前端可确认是否已配置，避免泄露明文。
        private String apiKeyMasked;
        // API Secret 掩码只回显是否已经保存密钥。
        private String apiSecretMasked;
        // Webhook Secret 掩码只回显是否已经保存签名密钥。
        private String webhookSecretMasked;
        // Pull 表达式用于提示管理员这条接入计划何时自动同步。
        private String syncCron;
        // 默认事业所主键用于和场所主数据串联。
        private Long workplaceId;
        // 默认事业所名称用于页面无需额外查表即可看懂归属。
        private String workplaceName;
        // 启用状态用于控制这条接入是否正式参与生产同步。
        private Boolean activeFlag;
        // 备注用于补充第三方接入背景。
        private String note;
        // Webhook URL 用于让管理员直接复制给第三方平台。
        private String webhookUrl;
        // 创建时间用于审计接入配置何时建立。
        private LocalDateTime createdAt;
        // 更新时间用于说明最近一次改配置是什么时候。
        private LocalDateTime updatedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getSourceSystem() { return sourceSystem; }
        public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
        public String getConnectorName() { return connectorName; }
        public void setConnectorName(String connectorName) { this.connectorName = connectorName; }
        public String getProviderType() { return providerType; }
        public void setProviderType(String providerType) { this.providerType = providerType; }
        public String getReceiveMode() { return receiveMode; }
        public void setReceiveMode(String receiveMode) { this.receiveMode = receiveMode; }
        public String getApiBaseUrl() { return apiBaseUrl; }
        public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }
        public String getApiKeyMasked() { return apiKeyMasked; }
        public void setApiKeyMasked(String apiKeyMasked) { this.apiKeyMasked = apiKeyMasked; }
        public String getApiSecretMasked() { return apiSecretMasked; }
        public void setApiSecretMasked(String apiSecretMasked) { this.apiSecretMasked = apiSecretMasked; }
        public String getWebhookSecretMasked() { return webhookSecretMasked; }
        public void setWebhookSecretMasked(String webhookSecretMasked) { this.webhookSecretMasked = webhookSecretMasked; }
        public String getSyncCron() { return syncCron; }
        public void setSyncCron(String syncCron) { this.syncCron = syncCron; }
        public Long getWorkplaceId() { return workplaceId; }
        public void setWorkplaceId(Long workplaceId) { this.workplaceId = workplaceId; }
        public String getWorkplaceName() { return workplaceName; }
        public void setWorkplaceName(String workplaceName) { this.workplaceName = workplaceName; }
        public Boolean getActiveFlag() { return activeFlag; }
        public void setActiveFlag(Boolean activeFlag) { this.activeFlag = activeFlag; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * 员工外部映射输出。
     */
    public static class ConnectorEmployeeMappingOut {
        private Long employeeId;
        private String employeeNo;
        private String employeeName;
        private String workplaceName;
        private String departmentName;
        private String sourceSystem;
        private String externalEmployeeId;
        private String externalEmployeeNo;
        private String status;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getEmployeeNo() { return employeeNo; }
        public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public String getWorkplaceName() { return workplaceName; }
        public void setWorkplaceName(String workplaceName) { this.workplaceName = workplaceName; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
        public String getSourceSystem() { return sourceSystem; }
        public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
        public String getExternalEmployeeId() { return externalEmployeeId; }
        public void setExternalEmployeeId(String externalEmployeeId) { this.externalEmployeeId = externalEmployeeId; }
        public String getExternalEmployeeNo() { return externalEmployeeNo; }
        public void setExternalEmployeeNo(String externalEmployeeNo) { this.externalEmployeeNo = externalEmployeeNo; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * 同步日志输出。
     */
    public static class ConnectorSyncLogOut {
        private Long id;
        private Long connectorId;
        private String sourceSystem;
        private String connectorName;
        private String triggerType;
        private String externalRequestId;
        private String syncStatus;
        private Integer successCount;
        private Integer failedCount;
        private String errorMessage;
        private String requestSnapshot;
        private String resultSnapshot;
        private Boolean retryFlag;
        private Integer retryCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getConnectorId() { return connectorId; }
        public void setConnectorId(Long connectorId) { this.connectorId = connectorId; }
        public String getSourceSystem() { return sourceSystem; }
        public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
        public String getConnectorName() { return connectorName; }
        public void setConnectorName(String connectorName) { this.connectorName = connectorName; }
        public String getTriggerType() { return triggerType; }
        public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
        public String getExternalRequestId() { return externalRequestId; }
        public void setExternalRequestId(String externalRequestId) { this.externalRequestId = externalRequestId; }
        public String getSyncStatus() { return syncStatus; }
        public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
        public Integer getSuccessCount() { return successCount; }
        public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
        public Integer getFailedCount() { return failedCount; }
        public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getRequestSnapshot() { return requestSnapshot; }
        public void setRequestSnapshot(String requestSnapshot) { this.requestSnapshot = requestSnapshot; }
        public String getResultSnapshot() { return resultSnapshot; }
        public void setResultSnapshot(String resultSnapshot) { this.resultSnapshot = resultSnapshot; }
        public Boolean getRetryFlag() { return retryFlag; }
        public void setRetryFlag(Boolean retryFlag) { this.retryFlag = retryFlag; }
        public Integer getRetryCount() { return retryCount; }
        public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * 工作台头部摘要输出。
     */
    public static class ConnectorSummaryOut {
        private Integer activeConnectorCount;
        private Integer mappedEmployeeCount;
        private Integer failedSyncCount;
        private LocalDateTime latestSyncAt;

        public Integer getActiveConnectorCount() { return activeConnectorCount; }
        public void setActiveConnectorCount(Integer activeConnectorCount) { this.activeConnectorCount = activeConnectorCount; }
        public Integer getMappedEmployeeCount() { return mappedEmployeeCount; }
        public void setMappedEmployeeCount(Integer mappedEmployeeCount) { this.mappedEmployeeCount = mappedEmployeeCount; }
        public Integer getFailedSyncCount() { return failedSyncCount; }
        public void setFailedSyncCount(Integer failedSyncCount) { this.failedSyncCount = failedSyncCount; }
        public LocalDateTime getLatestSyncAt() { return latestSyncAt; }
        public void setLatestSyncAt(LocalDateTime latestSyncAt) { this.latestSyncAt = latestSyncAt; }
    }

    /**
     * 测试连接输出。
     */
    public static class ConnectorTestResultOut {
        private Boolean success;
        private String message;
        private String webhookUrl;

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    }
}
