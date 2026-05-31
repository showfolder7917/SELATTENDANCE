package com.sp.selfsp.uniauth.user.domain.out;

// 用户列表项和保存返回统一复用同一输出对象，避免列表和详情字段漂移。
public class UniauthUserItemOut {

    // id 让前端更新用户时能稳定指向目标账号。
    private Long id;
    // tenantId 决定当前账号归属哪个租户。
    private Long tenantId;
    // tenantCode 让管理员快速识别账号所属租户编码。
    private String tenantCode;
    // loginName 是权限中心稳定登录标识。
    private String loginName;
    // displayName 用于平台和宿主展示当前用户。
    private String displayName;
    // displayNameKana 用于日语场景补充假名显示。
    private String displayNameKana;
    // locale 决定用户默认文案语言。
    private String locale;
    // email 记录用户基础联系邮箱。
    private String email;
    // phone 记录用户基础联系电话。
    private String phone;
    // userStatus 控制账号是否可继续登录。
    private String userStatus;
    // lockedFlag 让管理员识别账号是否被锁定。
    private Boolean lockedFlag;

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

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
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
}
