package com.sp.selfsp.uniauth.role.domain.in;

import java.util.List;

// 角色保存输入对象只承接角色主资料、权限、菜单和数据范围维护。
public class UniauthRoleSaveIn {

    // id 有值代表更新角色，没有值代表新建角色。
    public Long id;
    // tenantId 为空时可视为平台级角色，否则归属具体租户。
    public Long tenantId;
    // roleCode 是权限排查与脚本绑定的稳定键。
    public String roleCode;
    // roleName 是页面展示给管理员的角色名称。
    public String roleName;
    // roleDesc 用于解释角色职责，减少误授权。
    public String roleDesc;
    // permissionCodes 定义角色拥有的接口和按钮权限。
    public List<String> permissionCodes;
    // menuCodes 定义角色可见的菜单与工程入口。
    public List<String> menuCodes;
    // dataScopeType 定义 attendance 宿主数据范围类型。
    public String dataScopeType;
    // dataScopeValue 补充具体部门、租户或其他范围值。
    public String dataScopeValue;
}
