package com.sp.selfsp.uniauth.bootstrap.domain.out;

// 工作台摘要输出负责头部四张统计卡。
public class UniauthBootstrapSummaryOut {

    // moduleCount 让平台管理员快速感知当前已登记的工程模块数量。
    private Integer moduleCount;
    // tenantCount 让平台管理员快速感知当前托管租户规模。
    private Integer tenantCount;
    // userCount 让平台管理员快速感知账号总量。
    private Integer userCount;
    // roleCount 让管理员快速感知授权模型复杂度。
    private Integer roleCount;
    // menuCount 让管理员快速感知导航结构规模。
    private Integer menuCount;

    public Integer getModuleCount() {
        return moduleCount;
    }

    public void setModuleCount(Integer moduleCount) {
        this.moduleCount = moduleCount;
    }

    public Integer getTenantCount() {
        return tenantCount;
    }

    public void setTenantCount(Integer tenantCount) {
        this.tenantCount = tenantCount;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getRoleCount() {
        return roleCount;
    }

    public void setRoleCount(Integer roleCount) {
        this.roleCount = roleCount;
    }

    public Integer getMenuCount() {
        return menuCount;
    }

    public void setMenuCount(Integer menuCount) {
        this.menuCount = menuCount;
    }
}
