package com.sp.selfsp.uniauth.common.domain.out;

import java.util.List;

// 当前用户快照输出负责承接权限中心和 attendance 宿主共用的登录上下文。
public class UniauthCurrentUserOut {

    // userId 让宿主写操作日志时能稳定标记当前操作人。
    private Long userId;
    // username 让平台页头、日志和调试界面展示稳定账号名。
    private String username;
    // displayName 让中日双语界面显示更友好的登录者名称。
    private String displayName;
    // locale 让前端决定默认输出中文还是日文文案。
    private String locale;
    // tenantId 让宿主做租户级数据隔离。
    private Long tenantId;
    // tenantCode 让跨系统展示和日志关联使用稳定租户编码。
    private String tenantCode;
    // tenantStatus 让宿主在租户停用时快速阻断访问。
    private String tenantStatus;
    // permissionCodes 让按钮显隐和接口权限控制消费稳定权限码集合。
    private List<String> permissionCodes;
    // menuCodes 让宿主按权限动态显隐工程入口和菜单树。
    private List<String> menuCodes;
    // dataScopes 让宿主后续叠加本人、部门或租户级数据过滤。
    private List<String> dataScopes;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getTenantStatus() {
        return tenantStatus;
    }

    public void setTenantStatus(String tenantStatus) {
        this.tenantStatus = tenantStatus;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }

    public List<String> getMenuCodes() {
        return menuCodes;
    }

    public void setMenuCodes(List<String> menuCodes) {
        this.menuCodes = menuCodes;
    }

    public List<String> getDataScopes() {
        return dataScopes;
    }

    public void setDataScopes(List<String> dataScopes) {
        this.dataScopes = dataScopes;
    }
}
