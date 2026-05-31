package com.sp.selfsp.uniauth.role.service.impl;

import com.sp.selfsp.uniauth.common.UniauthAuditLogWriter;
import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.common.UniauthPermissionGuard;
import com.sp.selfsp.uniauth.common.UniauthValueSupport;
import com.sp.selfsp.uniauth.role.domain.in.UniauthRoleSaveIn;
import com.sp.selfsp.uniauth.role.domain.out.UniauthRoleItemOut;
import com.sp.selfsp.uniauth.role.dao.UniauthRoleDao;
import com.sp.selfsp.uniauth.role.service.UniauthRoleService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 角色服务实现只负责编排角色主资料与授权关系保存。
@Service
public class UniauthRoleServiceImpl implements UniauthRoleService {

    // 角色 DAO 负责角色主表和三类关系表读写。
    private final UniauthRoleDao uniauthRoleDao;
    // 权限守卫负责校验当前账号是否拥有角色写权限。
    private final UniauthPermissionGuard uniauthPermissionGuard;
    // 审计日志写入器负责记录角色维护动作。
    private final UniauthAuditLogWriter uniauthAuditLogWriter;

    // 构造角色服务时注入角色写入与公共守卫能力。
    public UniauthRoleServiceImpl(
        UniauthRoleDao uniauthRoleDao,
        UniauthPermissionGuard uniauthPermissionGuard,
        UniauthAuditLogWriter uniauthAuditLogWriter
    ) {
        // 保存角色 DAO，供主表读写和关系重建复用。
        this.uniauthRoleDao = uniauthRoleDao;
        // 保存权限守卫，供保存前先做授权校验。
        this.uniauthPermissionGuard = uniauthPermissionGuard;
        // 保存审计日志写入器，供保存完成后记录角色改动来源。
        this.uniauthAuditLogWriter = uniauthAuditLogWriter;
    }

    // 角色保存统一处理新增、更新和三类授权关系重建。
    @Override
    @Transactional(transactionManager = "uniauthTransactionManager")
    public UniauthRoleItemOut saveRole(UniauthCurrentUser currentUser, UniauthRoleSaveIn saveIn, String requestPath) {
        // 角色维护前必须先具备角色写权限，避免普通账号越权改授权模型。
        uniauthPermissionGuard.ensurePermission(currentUser, "uniauth.role.write");
        // 角色编码是稳定排查键，不能为空。
        UniauthValueSupport.requireText(saveIn == null ? null : saveIn.roleCode, "roleCode 不能为空");
        // 角色名称是前端展示主字段，不能为空。
        UniauthValueSupport.requireText(saveIn.roleName, "roleName 不能为空");
        // 没有 id 时按新增角色路径写主表。
        if (saveIn.id == null) {
            // 先插入角色主表，再按编码回查真实主键。
            uniauthRoleDao.insertRole(saveIn);
            // 把数据库生成的角色主键写回输入对象，供关系表写入复用。
            saveIn.id = UniauthValueSupport.longValue(uniauthRoleDao.selectRoleByCode(saveIn.roleCode.trim()).getId());
        } else {
            // 带 id 时按更新路径覆盖角色主资料。
            uniauthRoleDao.updateRole(saveIn);
        }
        // 先清掉旧权限关系，保证当前提交结果就是最终授权集合。
        uniauthRoleDao.deleteRolePermissions(saveIn.id);
        // 先清掉旧菜单关系，保证菜单显隐立即与本次保存一致。
        uniauthRoleDao.deleteRoleMenus(saveIn.id);
        // 先清掉旧数据范围，避免历史范围残留造成宿主越权。
        uniauthRoleDao.deleteRoleDataScopes(saveIn.id);
        // 权限码列表统一转空安全集合，避免空列表拼出非法 SQL。
        List<String> permissionCodes = UniauthValueSupport.nullSafeStringList(saveIn.permissionCodes);
        // 权限码有值时才查询主键并重建权限关系。
        for (Long permissionId : permissionCodes.isEmpty() ? List.<Long>of() : uniauthRoleDao.selectPermissionIdsByCodes(permissionCodes)) {
            // 每条关系单独写入，表达角色拥有的单个权限定义。
            uniauthRoleDao.insertRolePermission(saveIn.id, permissionId);
        }
        // 菜单码列表统一转空安全集合，避免空 IN 条件。
        List<String> menuCodes = UniauthValueSupport.nullSafeStringList(saveIn.menuCodes);
        // 菜单码有值时才查询主键并重建菜单关系。
        for (Long menuId : menuCodes.isEmpty() ? List.<Long>of() : uniauthRoleDao.selectMenuIdsByCodes(menuCodes)) {
            // 每条关系单独写入，表达角色可见的单个菜单节点。
            uniauthRoleDao.insertRoleMenu(saveIn.id, menuId);
        }
        // 数据范围类型缺失时默认给租户范围，确保宿主至少不越租户。
        String scopeType = UniauthValueSupport.blankToDefault(saveIn.dataScopeType, "tenant");
        // 数据范围值为空时优先落真实租户主键，只有都没有时才回退示例值。
        String scopeValue = UniauthValueSupport.blankToDefault(saveIn.dataScopeValue, saveIn.tenantId == null ? "1" : String.valueOf(saveIn.tenantId));
        // 写入 attendance 模块数据范围，供宿主查询层后续消费。
        uniauthRoleDao.insertRoleDataScope(saveIn.id, "attendance", scopeType, scopeValue);
        // 回查角色主表结果，保证返回值与数据库真实状态一致。
        UniauthRoleItemOut roleRow = uniauthRoleDao.selectRoleById(saveIn.id);
        // 角色维护成功后写审计日志，方便回溯谁改动了授权模型。
        uniauthAuditLogWriter.write(currentUser, "save-role", "role", String.valueOf(saveIn.id), requestPath, "success");
        return roleRow;
    }
}
