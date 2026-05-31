package com.sp.selfsp.uniauth.common;

import java.util.ArrayList;
import java.util.List;

// 当前用户上下文对象承接 JWT 里已经解析出的身份、权限和数据范围，避免 controller 重新查库。
public class UniauthCurrentUser {

    // userId 是平台用户主键，供审计日志和按钮权限判断复用。
    private Long userId;
    // username 是登录账号，供日志和页面展示复用。
    private String username;
    // displayName 是业务页面更友好的显示名称。
    private String displayName;
    // locale 记录当前用户语言偏好，供宿主决定默认展示语言。
    private String locale;
    // tenantId 是宿主业务查询默认隔离维度。
    private Long tenantId;
    // tenantCode 作为跨系统租户稳定键暴露给宿主。
    private String tenantCode;
    // tenantStatus 允许宿主在租户停用时尽早阻断访问。
    private String tenantStatus;
    // permissionCodes 用于 controller/service 判断接口和按钮权限。
    private List<String> permissionCodes = new ArrayList<>();
    // menuCodes 用于宿主前端动态渲染菜单。
    private List<String> menuCodes = new ArrayList<>();
    // dataScopes 用于宿主生成部门、本人与租户过滤条件。
    private List<String> dataScopes = new ArrayList<>();

    // 读取当前用户主键，供宿主审计和业务操作人字段复用。
    public Long getUserId() {
        return userId;
    }

    // 回填解析后的用户主键，保证上下文能正确代表当前登录者。
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // 读取登录账号，供宿主识别当前登录人。
    public String getUsername() {
        return username;
    }

    // 回填登录账号，保证上下文完整承接 JWT 主体信息。
    public void setUsername(String username) {
        this.username = username;
    }

    // 读取展示名，供前端右上角和审计日志展示。
    public String getDisplayName() {
        return displayName;
    }

    // 回填展示名，保证宿主不需要再去用户表查中文或日文名称。
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    // 读取用户语言偏好，供前端决定默认文案语言。
    public String getLocale() {
        return locale;
    }

    // 回填用户语言偏好，让 JWT 能直接承接语言上下文。
    public void setLocale(String locale) {
        this.locale = locale;
    }

    // 读取租户主键，供 attendance 宿主作为默认数据隔离条件。
    public Long getTenantId() {
        return tenantId;
    }

    // 回填租户主键，保证业务侧能拿到硬隔离所需主键。
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    // 读取租户编码，供外部日志和页面展示。
    public String getTenantCode() {
        return tenantCode;
    }

    // 回填租户编码，保证上下文具备稳定的跨系统租户标识。
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    // 读取租户状态，供宿主在租户停用时快速阻断请求。
    public String getTenantStatus() {
        return tenantStatus;
    }

    // 回填租户状态，避免宿主每次还要去权限中心单独查询租户是否启用。
    public void setTenantStatus(String tenantStatus) {
        this.tenantStatus = tenantStatus;
    }

    // 读取权限码列表，供控制器和服务层做权限判断。
    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    // 回填权限码列表，保证 JWT 解析后能直接用于接口权限校验。
    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes == null ? new ArrayList<>() : new ArrayList<>(permissionCodes);
    }

    // 读取菜单码列表，供前端动态渲染导航。
    public List<String> getMenuCodes() {
        return menuCodes;
    }

    // 回填菜单码列表，让宿主页面在不查库时也能决定显示哪些导航。
    public void setMenuCodes(List<String> menuCodes) {
        this.menuCodes = menuCodes == null ? new ArrayList<>() : new ArrayList<>(menuCodes);
    }

    // 读取数据范围列表，供 attendance 侧叠加部门或本人过滤。
    public List<String> getDataScopes() {
        return dataScopes;
    }

    // 回填数据范围，保证宿主后续扩展数据权限时不必更换上下文结构。
    public void setDataScopes(List<String> dataScopes) {
        this.dataScopes = dataScopes == null ? new ArrayList<>() : new ArrayList<>(dataScopes);
    }
}
