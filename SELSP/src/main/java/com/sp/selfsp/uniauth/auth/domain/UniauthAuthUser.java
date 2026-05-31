package com.sp.selfsp.uniauth.auth.domain;

// 登录查询领域对象只承接认证链路需要的用户主数据和租户状态，不直接暴露给外层接口。
public class UniauthAuthUser {

    // 用户主键用于生成当前用户上下文和后续审计日志。
    private Long id;
    // tenantId 用于宿主硬隔离和默认数据范围。
    private Long tenantId;
    // loginName 是权限中心账号稳定键。
    private String loginName;
    // passwordHash 用于登录时比对密码摘要。
    private String passwordHash;
    // displayName 用于页面和审计日志友好展示。
    private String displayName;
    // displayNameKana 用于日语场景展示假名。
    private String displayNameKana;
    // locale 是当前用户默认语言偏好。
    private String locale;
    // email 记录账号基础邮箱。
    private String email;
    // phone 记录账号基础电话。
    private String phone;
    // userStatus 用于判定账号是否已停用。
    private String userStatus;
    // lockedFlag 用于判定账号是否已锁定。
    private Boolean lockedFlag;
    // tenantCode 是跨系统展示租户的稳定编码。
    private String tenantCode;
    // tenantStatus 用于判定租户是否已停用。
    private String tenantStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayNameKana() {
        return displayNameKana;
    }

    public void setDisplayNameKana(String displayNameKana) {
        this.displayNameKana = displayNameKana;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public Boolean getLockedFlag() {
        return lockedFlag;
    }

    public void setLockedFlag(Boolean lockedFlag) {
        this.lockedFlag = lockedFlag;
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
}
