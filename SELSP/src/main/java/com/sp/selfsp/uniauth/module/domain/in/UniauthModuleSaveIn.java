package com.sp.selfsp.uniauth.module.domain.in;

// 模块保存入参负责承接权限中心里的工程模块主数据编辑请求。
public class UniauthModuleSaveIn {

    // id 为 null 时表示新增模块，带值时表示更新已有模块。
    public Long id;
    // moduleCode 是菜单、权限码和数据范围共同引用的稳定模块键。
    public String moduleCode;
    // moduleName 是管理后台对模块的主展示名称。
    public String moduleName;
    // moduleType 用来区分业务工程、权限中心或主题工程等类别。
    public String moduleType;
    // moduleDesc 负责补充当前模块的职责说明。
    public String moduleDesc;
    // entryProject 标识该模块归属哪个前端 project 入口。
    public String entryProject;
    // ownerSystem 标识模块最终归属 attendance、uniauth 或 seltheme 哪个系统。
    public String ownerSystem;
    // routeKey 让前端工作台知道该模块默认要跳转哪个路由或 section。
    public String routeKey;
    // enabledFlag 控制模块是否可以继续被菜单、角色和宿主工程消费。
    public Boolean enabledFlag;
}
