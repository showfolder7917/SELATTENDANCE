package com.sp.selfsp.attendance.punch.connector.domain.in;

/**
 * 第八阶段外部打卡接入输入对象集合。
 */
public final class AttendanceConnectorIn {

    private AttendanceConnectorIn() {
    }

    /**
     * 外部接入工作台查询入参。
     */
    public static class ConnectorWorkbenchQueryIn {

        // 来源系统用于把接入配置、映射和同步日志统一缩小到某一条连接器链路。
        private String sourceSystem;
        // 关键字用于按接入名称、来源系统、员工编号或外部编号快速定位问题对象。
        private String keyword;
        // 仅看启用接入用于让管理员先处理当前仍在生产中使用的连接器。
        private Boolean activeOnly;

        public String getSourceSystem() {
            return sourceSystem;
        }

        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public Boolean getActiveOnly() {
            return activeOnly;
        }

        public void setActiveOnly(Boolean activeOnly) {
            this.activeOnly = activeOnly;
        }
    }

    /**
     * 外部接入配置保存入参。
     */
    public static class ConnectorConfigSaveIn {

        // 主键存在时表示编辑既有连接器，不存在时表示新建接入配置。
        private Long id;
        // 来源系统编码作为第三方打卡进入系统后的统一来源标识。
        private String sourceSystem;
        // 接入名称用于页面清单、日志抬头和复制说明展示。
        private String connectorName;
        // 平台类型用于告诉管理员当前配置对应哪一类第三方平台。
        private String providerType;
        // 接收方式用于区分这条接入是 Webhook、Pull 还是 CSV。
        private String receiveMode;
        // API 地址用于 Pull 或测试连接时确定远端入口。
        private String apiBaseUrl;
        // API Key 用于后续 Pull 或供应商鉴权。
        private String apiKey;
        // API Secret 用于后续 Pull 或供应商签名校验。
        private String apiSecret;
        // Webhook Secret 用于校验第三方推送签名。
        private String webhookSecret;
        // 同步表达式用于预留 Pull 任务调度频率。
        private String syncCron;
        // 默认事业所用于外部平台只提供员工编号时的归属说明。
        private Long workplaceId;
        // 是否启用用于控制这条连接器是否真的参与接入链路。
        private Boolean activeFlag;
        // 备注用于记录这条接入的业务背景或注意事项。
        private String note;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSourceSystem() {
            return sourceSystem;
        }

        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }

        public String getConnectorName() {
            return connectorName;
        }

        public void setConnectorName(String connectorName) {
            this.connectorName = connectorName;
        }

        public String getProviderType() {
            return providerType;
        }

        public void setProviderType(String providerType) {
            this.providerType = providerType;
        }

        public String getReceiveMode() {
            return receiveMode;
        }

        public void setReceiveMode(String receiveMode) {
            this.receiveMode = receiveMode;
        }

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }

        public String getSyncCron() {
            return syncCron;
        }

        public void setSyncCron(String syncCron) {
            this.syncCron = syncCron;
        }

        public Long getWorkplaceId() {
            return workplaceId;
        }

        public void setWorkplaceId(Long workplaceId) {
            this.workplaceId = workplaceId;
        }

        public Boolean getActiveFlag() {
            return activeFlag;
        }

        public void setActiveFlag(Boolean activeFlag) {
            this.activeFlag = activeFlag;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}
