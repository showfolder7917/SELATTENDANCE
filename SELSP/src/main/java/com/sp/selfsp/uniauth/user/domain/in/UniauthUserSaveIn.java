package com.sp.selfsp.uniauth.user.domain.in;

import java.util.List;

// 用户保存输入对象只承接账号资料与角色关系维护所需字段。
public class UniauthUserSaveIn {

    // id 有值代表更新现有账号，没有值代表创建新账号。
    private Long id;
    // tenantId 决定账号归属的租户边界。
    private Long tenantId;
    // loginName 是权限中心稳定登录标识。
    private String loginName;
    // password 只在新增或显式重置密码时生效。
    private String password;
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
    // userStatus 控制账号是否可登录。
    private String userStatus;
    // roleIds 直接承接当前账号应绑定的角色主键集合。
    private List<Long> roleIds;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
