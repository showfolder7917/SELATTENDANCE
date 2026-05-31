package com.sp.selfsp.uniauth.role.domain.out;

// 角色列表项和保存返回统一复用同一输出对象，保证授权页字段稳定。
public class UniauthRoleItemOut {

    // id 让前端更新角色时能稳定指向目标记录。
    private Long id;
    // tenantId 为空时表示平台级角色，否则归属具体租户。
    private Long tenantId;
    // roleCode 是权限排查与脚本绑定的稳定键。
    private String roleCode;
    // roleName 是页面展示给管理员的角色名称。
    private String roleName;
    // roleDesc 用于解释角色职责，减少误授权。
    private String roleDesc;
    // builtinFlag 让页面识别系统内置角色是否允许删改。
    private Boolean builtinFlag;
    // roleStatus 控制角色当前是否仍允许分配给账号。
    private String roleStatus;

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

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleDesc() {
        return roleDesc;
    }

    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    public Boolean getBuiltinFlag() {
        return builtinFlag;
    }

    public void setBuiltinFlag(Boolean builtinFlag) {
        this.builtinFlag = builtinFlag;
    }

    public String getRoleStatus() {
        return roleStatus;
    }

    public void setRoleStatus(String roleStatus) {
        this.roleStatus = roleStatus;
    }
}
