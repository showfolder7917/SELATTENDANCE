package com.sp.selfsp.uniauth.menu.domain.in;

// 菜单保存输入对象只承接动态导航节点维护所需字段。
public class UniauthMenuSaveIn {

    // id 有值代表更新菜单节点，没有值代表新建菜单节点。
    private Long id;
    // moduleCode 标识当前菜单属于哪个工程模块。
    private String moduleCode;
    // menuCode 是前后端传递菜单授权的稳定键。
    private String menuCode;
    // parentId 决定菜单树层级关系。
    private Long parentId;
    // menuType 决定节点是分组、页面还是动作。
    private String menuType;
    // routePath 供宿主前端跳转到具体页面。
    private String routePath;
    // componentName 供宿主按组件名装配页面入口。
    private String componentName;
    // iconName 供前端图标系统渲染节点图标。
    private String iconName;
    // sortOrder 决定菜单树稳定排序。
    private Integer sortOrder;
    // titleZh 维护中文展示标题。
    private String titleZh;
    // titleJa 维护日文展示标题。
    private String titleJa;
    // enabledFlag 控制当前节点是否可对前端可见。
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

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getTitleZh() {
        return titleZh;
    }

    public void setTitleZh(String titleZh) {
        this.titleZh = titleZh;
    }

    public String getTitleJa() {
        return titleJa;
    }

    public void setTitleJa(String titleJa) {
        this.titleJa = titleJa;
    }

    public Boolean getEnabledFlag() {
        return enabledFlag;
    }

    public void setEnabledFlag(Boolean enabledFlag) {
        this.enabledFlag = enabledFlag;
    }
}
