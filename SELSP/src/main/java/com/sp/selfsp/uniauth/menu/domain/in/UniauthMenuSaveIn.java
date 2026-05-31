package com.sp.selfsp.uniauth.menu.domain.in;

// 菜单保存输入对象只承接动态导航节点维护所需字段。
public class UniauthMenuSaveIn {

    // id 有值代表更新菜单节点，没有值代表新建菜单节点。
    public Long id;
    // moduleCode 标识当前菜单属于哪个工程模块。
    public String moduleCode;
    // menuCode 是前后端传递菜单授权的稳定键。
    public String menuCode;
    // parentId 决定菜单树层级关系。
    public Long parentId;
    // menuType 决定节点是分组、页面还是动作。
    public String menuType;
    // routePath 供宿主前端跳转到具体页面。
    public String routePath;
    // componentName 供宿主按组件名装配页面入口。
    public String componentName;
    // iconName 供前端图标系统渲染节点图标。
    public String iconName;
    // sortOrder 决定菜单树稳定排序。
    public Integer sortOrder;
    // titleZh 维护中文展示标题。
    public String titleZh;
    // titleJa 维护日文展示标题。
    public String titleJa;
    // enabledFlag 控制当前节点是否可对前端可见。
    public Boolean enabledFlag;
}
