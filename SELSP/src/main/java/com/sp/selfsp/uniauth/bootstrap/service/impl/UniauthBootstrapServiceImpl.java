package com.sp.selfsp.uniauth.bootstrap.service.impl;

import com.sp.selfsp.uniauth.bootstrap.dao.UniauthBootstrapDao;
import com.sp.selfsp.uniauth.bootstrap.service.UniauthBootstrapService;
import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.common.UniauthPermissionGuard;
import com.sp.selfsp.uniauth.common.UniauthViewMapper;
import com.sp.selfsp.uniauth.bootstrap.domain.out.UniauthBootstrapOut;
import com.sp.selfsp.uniauth.bootstrap.domain.out.UniauthBootstrapSummaryOut;
import com.sp.selfsp.uniauth.bootstrap.domain.out.UniauthPermissionItemOut;
import com.sp.selfsp.uniauth.menu.domain.out.UniauthMenuItemOut;
import com.sp.selfsp.uniauth.module.domain.out.UniauthModuleItemOut;
import com.sp.selfsp.uniauth.role.domain.out.UniauthRoleItemOut;
import com.sp.selfsp.uniauth.tenant.domain.out.UniauthTenantItemOut;
import com.sp.selfsp.uniauth.user.domain.out.UniauthUserItemOut;
import java.util.List;
import org.springframework.stereotype.Service;

// 工作台服务实现只负责聚合展示数据，不再承担登录校验和管理保存动作。
@Service
public class UniauthBootstrapServiceImpl implements UniauthBootstrapService {

    // 工作台 DAO 负责读取列表型展示数据与摘要来源。
    private final UniauthBootstrapDao uniauthBootstrapDao;
    // 权限守卫负责校验当前用户是否允许读取权限中心工作台。
    private final UniauthPermissionGuard uniauthPermissionGuard;
    // 视图映射器负责把当前用户上下文转换成统一前端 JSON 结构。
    private final UniauthViewMapper uniauthViewMapper;

    // 构造工作台服务时注入聚合查询和通用权限依赖。
    public UniauthBootstrapServiceImpl(
        UniauthBootstrapDao uniauthBootstrapDao,
        UniauthPermissionGuard uniauthPermissionGuard,
        UniauthViewMapper uniauthViewMapper
    ) {
        // 保存工作台 DAO，供读取列表和摘要复用。
        this.uniauthBootstrapDao = uniauthBootstrapDao;
        // 保存权限守卫，供接口读取前先校验 bootstrap 读取权限。
        this.uniauthPermissionGuard = uniauthPermissionGuard;
        // 保存视图映射器，供 currentUser 输出复用。
        this.uniauthViewMapper = uniauthViewMapper;
    }

    // 工作台读取负责把模块、租户、用户、角色、菜单和权限一次性聚合给前端。
    @Override
    public UniauthBootstrapOut getWorkbench(UniauthCurrentUser currentUser) {
        // 读取权限中心管理工作台前必须先确认当前用户拥有 bootstrap 读取权限。
        uniauthPermissionGuard.ensurePermission(currentUser, "uniauth.bootstrap.read");
        // 模块列表供管理员维护工程主数据、入口和归属系统。
        List<UniauthModuleItemOut> modules = uniauthBootstrapDao.selectModuleList();
        // 租户列表供平台管理员查看当前多租户状态。
        List<UniauthTenantItemOut> tenants = uniauthBootstrapDao.selectTenantList();
        // 用户列表供账号管理区块直接渲染。
        List<UniauthUserItemOut> users = uniauthBootstrapDao.selectUserList();
        // 角色列表供授权管理区块直接渲染。
        List<UniauthRoleItemOut> roles = uniauthBootstrapDao.selectRoleList();
        // 菜单列表供动态导航树与工程入口配置展示。
        List<UniauthMenuItemOut> menus = uniauthBootstrapDao.selectMenuList();
        // 权限定义列表供角色编辑时选择按钮和接口权限码。
        List<UniauthPermissionItemOut> permissions = uniauthBootstrapDao.selectPermissionList();
        // 创建标准工作台输出对象，统一承接首屏全部列表与摘要字段。
        UniauthBootstrapOut workbench = new UniauthBootstrapOut();
        // 当前用户快照用于头部、语言、菜单和租户显示。
        workbench.setCurrentUser(uniauthViewMapper.toCurrentUserOut(currentUser));
        // 模块列表用于模块管理表格和权限中心第五个正式区块。
        workbench.setModules(modules);
        // 租户列表用于租户管理表格。
        workbench.setTenants(tenants);
        // 用户列表用于用户管理表格。
        workbench.setUsers(users);
        // 角色列表用于角色管理表格。
        workbench.setRoles(roles);
        // 菜单列表用于菜单树与菜单管理表格。
        workbench.setMenus(menus);
        // 权限列表用于角色授权配置与调试。
        workbench.setPermissions(permissions);
        // 创建摘要对象，避免工作台头部继续靠匿名 Map 取统计值。
        UniauthBootstrapSummaryOut summaryOut = new UniauthBootstrapSummaryOut();
        // 模块数量让管理员快速感知当前统一权限中心已托管多少工程模块。
        summaryOut.setModuleCount(modules.size());
        // 租户数量让平台管理员快速感知当前托管租户规模。
        summaryOut.setTenantCount(tenants.size());
        // 用户数量让管理员快速感知账号规模。
        summaryOut.setUserCount(users.size());
        // 角色数量让管理员快速感知授权模型规模。
        summaryOut.setRoleCount(roles.size());
        // 菜单数量让管理员快速感知导航结构规模。
        summaryOut.setMenuCount(menus.size());
        // 把摘要对象挂到工作台响应，供头部统计卡直接消费。
        workbench.setSummary(summaryOut);
        return workbench;
    }
}
