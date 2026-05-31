package com.sp.selfsp.uniauth.module.domain.out;

// 模块输出对象负责把模块主数据稳定返回给工作台和模块管理区块。
public class UniauthModuleItemOut {

    // id 负责标识模块主表主键，供编辑和审计复用。
    private Long id;
    // moduleCode 是菜单、权限码和角色数据范围都会引用的稳定键。
    private String moduleCode;
    // moduleName 是前端模块管理表格的主展示标题。
    private String moduleName;
    // moduleType 说明当前模块是业务域、平台域还是主题域。
    private String moduleType;
    // moduleDesc 负责向管理员解释当前模块的职责边界。
    private String moduleDesc;
    // entryProject 告诉宿主该模块最终归属哪个前端工程入口。
    private String entryProject;
    // ownerSystem 告诉管理员当前模块属于 attendance、uniauth 还是 seltheme。
    private String ownerSystem;
    // routeKey 让宿主或管理台回显该模块默认落点。
    private String routeKey;
    // enabledFlag 让列表直接展示该模块是否启用。
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

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public String getModuleDesc() {
        return moduleDesc;
    }

    public void setModuleDesc(String moduleDesc) {
        this.moduleDesc = moduleDesc;
    }

    public String getEntryProject() {
        return entryProject;
    }

    public void setEntryProject(String entryProject) {
        this.entryProject = entryProject;
    }

    public String getOwnerSystem() {
        return ownerSystem;
    }

    public void setOwnerSystem(String ownerSystem) {
        this.ownerSystem = ownerSystem;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }

    public Boolean getEnabledFlag() {
        return enabledFlag;
    }

    public void setEnabledFlag(Boolean enabledFlag) {
        this.enabledFlag = enabledFlag;
    }
}
