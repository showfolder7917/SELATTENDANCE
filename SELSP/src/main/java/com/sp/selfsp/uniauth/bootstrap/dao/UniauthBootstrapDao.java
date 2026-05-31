package com.sp.selfsp.uniauth.bootstrap.dao;

import com.sp.selfsp.uniauth.bootstrap.domain.out.UniauthPermissionItemOut;
import com.sp.selfsp.uniauth.menu.domain.out.UniauthMenuItemOut;
import com.sp.selfsp.uniauth.module.domain.out.UniauthModuleItemOut;
import com.sp.selfsp.uniauth.role.domain.out.UniauthRoleItemOut;
import com.sp.selfsp.uniauth.tenant.domain.out.UniauthTenantItemOut;
import com.sp.selfsp.uniauth.user.domain.out.UniauthUserItemOut;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 工作台 DAO 只承接聚合展示和审计写入所需 SQL，避免把登录和保存动作揉进同一个 DAO。
@Mapper
public interface UniauthBootstrapDao {

    // 查询模块列表，供模块管理区块维护工程主数据。
    List<UniauthModuleItemOut> selectModuleList();

    // 查询租户列表，供平台管理员浏览当前多租户状态。
    List<UniauthTenantItemOut> selectTenantList();

    // 查询用户列表，供权限中心用户管理表直接渲染。
    List<UniauthUserItemOut> selectUserList();

    // 查询角色列表，供角色授权区块直接渲染。
    List<UniauthRoleItemOut> selectRoleList();

    // 查询菜单列表，供菜单树和工程入口预览复用。
    List<UniauthMenuItemOut> selectMenuList();

    // 查询权限定义列表，供角色编辑时选择权限码。
    List<UniauthPermissionItemOut> selectPermissionList();

    // 审计日志写入由多个子域共用，因此收口在工作台侧公共 DAO。
    int insertAuditLog(
        @Param("tenantId") Long tenantId,
        @Param("operatorUserId") Long operatorUserId,
        @Param("operatorName") String operatorName,
        @Param("moduleCode") String moduleCode,
        @Param("actionType") String actionType,
        @Param("targetType") String targetType,
        @Param("targetId") String targetId,
        @Param("resultStatus") String resultStatus,
        @Param("requestPath") String requestPath,
        @Param("detailText") String detailText
    );
}
