package com.sp.selfsp.attendance.punch.domain.in;

import java.util.Map;

/**
 * 第三阶段打卡模块输入对象集合。
 */
// 定义 打卡输入集合，承接当前文件对应的业务职责。
public final class AttendancePunchIn {

    // 定义 打卡输入集合 处理入口，承接当前业务动作。
    private AttendancePunchIn() {
    }

    /**
     * 打卡记录查询入参。
     */
    // 定义 打卡记录查询In，承接当前文件对应的业务职责。
    public static class PunchLogQueryIn {

        // 开始日期用于缩小打卡查询区间，避免首页默认拉全历史。
        private String dateFrom;
        // 结束日期用于缩小打卡查询区间，保证列表和统计口径一致。
        private String dateTo;
        // 员工关键字用于按员工编号或姓名快速定位目标记录。
        private String employeeKeyword;
        // 来源系统用于区分手动补录、CSV 导入和 Webhook 接收。
        private String sourceSystem;
        // 处理状态用于让管理员先看未匹配、失败或重复记录。
        private String processStatus;
        // 打卡类型用于只看出勤、退勤或休息事件。
        private String punchType;
        // 页码用于控制列表分页，避免打卡记录增多后一次拉太多。
        private Integer page;
        // 每页数量用于控制列表返回量，保持第三阶段页面加载稳定。
        private Integer pageSize;

        public String getDateFrom() {
            return dateFrom;
        }

        public void setDateFrom(String dateFrom) {
            this.dateFrom = dateFrom;
        }

        public String getDateTo() {
            return dateTo;
        }

        public void setDateTo(String dateTo) {
            this.dateTo = dateTo;
        }

        public String getEmployeeKeyword() {
            return employeeKeyword;
        }

        public void setEmployeeKeyword(String employeeKeyword) {
            this.employeeKeyword = employeeKeyword;
        }

        public String getSourceSystem() {
            return sourceSystem;
        }

        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }

        public String getProcessStatus() {
            return processStatus;
        }

        public void setProcessStatus(String processStatus) {
            this.processStatus = processStatus;
        }

        public String getPunchType() {
            return punchType;
        }

        public void setPunchType(String punchType) {
            this.punchType = punchType;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }
    }

    /**
     * 手动补录打卡入参。
     */
    // 定义 手动打卡保存In，承接当前文件对应的业务职责。
    public static class PunchManualSaveIn {

        // 员工主键用于把补录打卡直接归属到系统员工。
        private Long employeeId;
        // 打卡时间用于记录真实的出勤事实发生时间。
        private String punchTime;
        // 打卡类型用于标识这条事实是上班、下班还是休息动作。
        private String punchType;
        // 来源系统用于在手动补录时保留来源标签，默认会回落到 MANUAL。
        private String sourceSystem;
        // 设备名称用于在详情里说明这条补录记录从哪里补入。
        private String deviceName;
        // 备注用于解释为什么需要人工补录这条记录。
        private String note;

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getPunchTime() {
            return punchTime;
        }

        public void setPunchTime(String punchTime) {
            this.punchTime = punchTime;
        }

        public String getPunchType() {
            return punchType;
        }

        public void setPunchType(String punchType) {
            this.punchType = punchType;
        }

        public String getSourceSystem() {
            return sourceSystem;
        }

        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    /**
     * CSV 导入请求入参。
     */
    // 定义 打卡导入In，承接当前文件对应的业务职责。
    public static class PunchImportIn {

        // 文件名用于回显批次来源和导入结果摘要。
        private String fileName;
        // CSV 文本用于直接在第一版页面里粘贴或上传后透传给后端解析。
        private String csvText;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getCsvText() {
            return csvText;
        }

        public void setCsvText(String csvText) {
            this.csvText = csvText;
        }
    }

    /**
     * 未匹配记录绑定员工入参。
     */
    // 定义 打卡绑定员工In，承接当前文件对应的业务职责。
    public static class PunchBindEmployeeIn {

        // 员工主键用于把未匹配记录重新归属到系统内已有员工。
        private Long employeeId;

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }
    }

    /**
     * 忽略记录入参。
     */
    // 定义 打卡忽略In，承接当前文件对应的业务职责。
    public static class PunchIgnoreIn {

        // 忽略原因用于说明为什么这条记录不再进入后续日次计算。
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * Webhook 接收入参。
     */
    // 定义 Webhook打卡In，承接当前文件对应的业务职责。
    public static class PunchWebhookIn {

        // 租户编码用于未来多租户扩展时判断这条 Webhook 属于谁。
        private String tenantCode;
        // 来源系统用于区分是哪个外部终端或网关推来的打卡事实。
        private String sourceSystem;
        // 来源事件编号用于做去重。
        private String sourceEventId;
        // 外部员工编号用于匹配系统内员工。
        private String externalEmployeeId;
        // 打卡时间用于记录 Webhook 事实发生时间。
        private String punchTime;
        // 打卡类型用于区分上下班和休息动作。
        private String punchType;
        // 设备编号用于保留来源终端标识。
        private String deviceId;
        // 设备名称用于列表和详情展示接收来源。
        private String deviceName;
        // 原始额外字段用于把外部系统 payload 明细保留下来。
        private Map<String, Object> rawData;

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        public String getSourceSystem() {
            return sourceSystem;
        }

        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }

        public String getSourceEventId() {
            return sourceEventId;
        }

        public void setSourceEventId(String sourceEventId) {
            this.sourceEventId = sourceEventId;
        }

        public String getExternalEmployeeId() {
            return externalEmployeeId;
        }

        public void setExternalEmployeeId(String externalEmployeeId) {
            this.externalEmployeeId = externalEmployeeId;
        }

        public String getPunchTime() {
            return punchTime;
        }

        public void setPunchTime(String punchTime) {
            this.punchTime = punchTime;
        }

        public String getPunchType() {
            return punchType;
        }

        public void setPunchType(String punchType) {
            this.punchType = punchType;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public Map<String, Object> getRawData() {
            return rawData;
        }

        public void setRawData(Map<String, Object> rawData) {
            this.rawData = rawData;
        }
    }
}
