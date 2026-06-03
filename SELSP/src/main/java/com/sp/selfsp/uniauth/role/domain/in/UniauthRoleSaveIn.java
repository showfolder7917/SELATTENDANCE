package com.sp.selfsp.uniauth.role.domain.in;

import java.util.List;

// 角色保存输入对象只承接角色主资料、权限、菜单和数据范围维护。
public class UniauthRoleSaveIn {

    // id 有值代表更新角色，没有值代表新建角色。
    private Long id;
    // tenantId 为空时可视为平台级角色，否则归属具体租户。
    private Long tenantId;
    // roleCode 是权限排查与脚本绑定的稳定键。
    private String roleCode;
    // roleName 是页面展示给管理员的角色名称。
    private String roleName;
    // roleDesc 用于解释角色职责，减少误授权。
    private String roleDesc;
    // permissionCodes 定义角色拥有的接口和按钮权限。
    private List<String> permissionCodes;
    // menuCodes 定义角色可见的菜单与工程入口。
    private List<String> menuCodes;
    // dataScopeType 定义 attendance 宿主数据范围类型。
    private String dataScopeType;
    // dataScopeValue 补充具体部门、租户或其他范围值。
    private String dataScopeValue;

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

    public String getDataScopeType() {
        return dataScopeType;
    }

    public void setDataScopeType(String dataScopeType) {
        this.dataScopeType = dataScopeType;
    }

    public String getDataScopeValue() {
        return dataScopeValue;
    }

    public void setDataScopeValue(String dataScopeValue) {
        this.dataScopeValue = dataScopeValue;
    }
}
