package com.sp.selfsp.uniauth.module.domain.in;

// 模块保存入参负责承接权限中心里的工程模块主数据编辑请求。
public class UniauthModuleSaveIn {

    // id 为 null 时表示新增模块，带值时表示更新已有模块。
    private Long id;
    // moduleCode 是菜单、权限码和数据范围共同引用的稳定模块键。
    private String moduleCode;
    // moduleName 是管理后台对模块的主展示名称。
    private String moduleName;
    // moduleType 用来区分业务工程、权限中心或主题工程等类别。
    private String moduleType;
    // moduleDesc 负责补充当前模块的职责说明。
    private String moduleDesc;
    // entryProject 标识该模块归属哪个前端 project 入口。
    private String entryProject;
    // ownerSystem 标识模块最终归属 attendance、uniauth 或 seltheme 哪个系统。
    private String ownerSystem;
    // routeKey 让前端工作台知道该模块默认要跳转哪个路由或 section。
    private String routeKey;
    // enabledFlag 控制模块是否可以继续被菜单、角色和宿主工程消费。
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
