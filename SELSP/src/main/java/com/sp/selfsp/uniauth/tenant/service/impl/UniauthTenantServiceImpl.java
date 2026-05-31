package com.sp.selfsp.uniauth.tenant.service.impl;

import com.sp.selfsp.uniauth.common.UniauthAuditLogWriter;
import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.common.UniauthPermissionGuard;
import com.sp.selfsp.uniauth.common.UniauthValueSupport;
import com.sp.selfsp.uniauth.tenant.domain.in.UniauthTenantSaveIn;
import com.sp.selfsp.uniauth.tenant.domain.out.UniauthTenantItemOut;
import com.sp.selfsp.uniauth.tenant.dao.UniauthTenantDao;
import com.sp.selfsp.uniauth.tenant.service.UniauthTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 租户服务实现只负责编排租户保存，不再关心认证和其他管理子域。
@Service
public class UniauthTenantServiceImpl implements UniauthTenantService {

    // 租户 DAO 负责租户主表读写。
    private final UniauthTenantDao uniauthTenantDao;
    // 权限守卫负责校验当前用户是否拥有租户写权限。
    private final UniauthPermissionGuard uniauthPermissionGuard;
    // 审计日志写入器负责记录租户维护动作。
    private final UniauthAuditLogWriter uniauthAuditLogWriter;

    // 构造租户服务时注入租户读写和通用守卫能力。
    public UniauthTenantServiceImpl(
        UniauthTenantDao uniauthTenantDao,
        UniauthPermissionGuard uniauthPermissionGuard,
        UniauthAuditLogWriter uniauthAuditLogWriter
    ) {
        // 保存租户 DAO，供新增、更新和回查结果复用。
        this.uniauthTenantDao = uniauthTenantDao;
        // 保存权限守卫，供保存前做平台级越权拦截。
        this.uniauthPermissionGuard = uniauthPermissionGuard;
        // 保存审计日志写入器，供保存后记录操作轨迹。
        this.uniauthAuditLogWriter = uniauthAuditLogWriter;
    }

    // 租户保存统一处理新增与更新，并按保存结果回查正式落库数据。
    @Override
    @Transactional(transactionManager = "uniauthTransactionManager")
    public UniauthTenantItemOut saveTenant(UniauthCurrentUser currentUser, UniauthTenantSaveIn saveIn, String requestPath) {
        // 平台管理员必须先拥有租户写权限，才能新增或修改租户资料。
        uniauthPermissionGuard.ensurePermission(currentUser, "uniauth.tenant.write");
        // 租户编码是平台稳定键，不能为空。
        UniauthValueSupport.requireText(saveIn == null ? null : saveIn.tenantCode, "tenantCode 不能为空");
        // 租户名称是平台展示主字段，不能为空。
        UniauthValueSupport.requireText(saveIn.tenantName, "tenantName 不能为空");
        // 没有 id 时按新增路径写租户主表。
        if (saveIn.id == null) {
            // 新增租户直接写主表，后续按编码回查最终结果。
            uniauthTenantDao.insertTenant(saveIn);
        } else {
            // 带 id 时按更新路径覆盖租户资料与状态。
            uniauthTenantDao.updateTenant(saveIn);
        }
        // 按租户编码回查正式结果，保证返回给前端的是数据库真实状态。
        UniauthTenantItemOut tenantRow = uniauthTenantDao.selectTenantByCode(saveIn.tenantCode.trim());
        // 租户维护完成后写审计日志，方便平台追踪谁调整过租户资料。
        uniauthAuditLogWriter.write(currentUser, "save-tenant", "tenant", String.valueOf(tenantRow.getId()), requestPath, "success");
        return tenantRow;
    }
}
