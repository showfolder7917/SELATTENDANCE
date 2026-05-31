package com.sp.selfsp.uniauth.bootstrap.domain.out;

// 权限定义列表项输出负责向角色页提供稳定的权限元数据。
public class UniauthPermissionItemOut {

    // id 让前端在调试和排序时保留权限定义的稳定主键。
    private Long id;
    // moduleCode 标识权限归属哪个工程模块。
    private String moduleCode;
    // permissionCode 是角色授权与后端守卫共用的稳定键。
    private String permissionCode;
    // permissionName 让管理员理解当前权限的业务含义。
    private String permissionName;
    // permissionType 区分菜单、按钮、接口或数据权限。
    private String permissionType;
    // resourceKey 标识权限作用的业务资源。
    private String resourceKey;
    // actionKey 标识权限允许执行的动作。
    private String actionKey;
    // scopeType 标识当前权限适用的作用域类型。
    private String scopeType;
    // enabledFlag 控制当前权限定义是否仍可分配给角色。
    private Boolean enabledFlag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getActionKey() {
        return actionKey;
    }

    public void setActionKey(String actionKey) {
        this.actionKey = actionKey;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public Boolean getEnabledFlag() {
        return enabledFlag;
    }

    public void setEnabledFlag(Boolean enabledFlag) {
        this.enabledFlag = enabledFlag;
    }
}
