package com.sp.selfsp.uniauth.bootstrap.domain.out;

import com.sp.selfsp.uniauth.common.domain.out.UniauthCurrentUserOut;
import com.sp.selfsp.uniauth.menu.domain.out.UniauthMenuItemOut;
import com.sp.selfsp.uniauth.module.domain.out.UniauthModuleItemOut;
import com.sp.selfsp.uniauth.role.domain.out.UniauthRoleItemOut;
import com.sp.selfsp.uniauth.tenant.domain.out.UniauthTenantItemOut;
import com.sp.selfsp.uniauth.user.domain.out.UniauthUserItemOut;
import java.util.List;

// 工作台聚合输出负责一次性返回模块、租户、用户、角色、菜单和权限定义列表。
public class UniauthBootstrapOut {

    // currentUser 让工作台头部同步消费登录上下文和语言偏好。
    private UniauthCurrentUserOut currentUser;
    // modules 让模块管理区块直接渲染工程主数据列表。
    private List<UniauthModuleItemOut> modules;
    // tenants 让租户管理区块直接渲染多租户数据。
    private List<UniauthTenantItemOut> tenants;
    // users 让账号管理区块直接渲染用户列表。
    private List<UniauthUserItemOut> users;
    // roles 让授权管理区块直接渲染角色列表。
    private List<UniauthRoleItemOut> roles;
    // menus 让动态导航区块直接渲染菜单树和菜单表格。
    private List<UniauthMenuItemOut> menus;
    // permissions 让角色授权区块直接拿到权限元数据列表。
    private List<UniauthPermissionItemOut> permissions;
    // summary 让头部统计卡不必继续自己计算数量。
    private UniauthBootstrapSummaryOut summary;

    public UniauthCurrentUserOut getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UniauthCurrentUserOut currentUser) {
        this.currentUser = currentUser;
    }

    public List<UniauthModuleItemOut> getModules() {
        return modules;
    }

    public void setModules(List<UniauthModuleItemOut> modules) {
        this.modules = modules;
    }

    public List<UniauthTenantItemOut> getTenants() {
        return tenants;
    }

    public void setTenants(List<UniauthTenantItemOut> tenants) {
        this.tenants = tenants;
    }

    public List<UniauthUserItemOut> getUsers() {
        return users;
    }

    public void setUsers(List<UniauthUserItemOut> users) {
        this.users = users;
    }

    public List<UniauthRoleItemOut> getRoles() {
        return roles;
    }

    public void setRoles(List<UniauthRoleItemOut> roles) {
        this.roles = roles;
    }

    public List<UniauthMenuItemOut> getMenus() {
        return menus;
    }

    public void setMenus(List<UniauthMenuItemOut> menus) {
        this.menus = menus;
    }

    public List<UniauthPermissionItemOut> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<UniauthPermissionItemOut> permissions) {
        this.permissions = permissions;
    }

    public UniauthBootstrapSummaryOut getSummary() {
        return summary;
    }

    public void setSummary(UniauthBootstrapSummaryOut summary) {
        this.summary = summary;
    }
}
