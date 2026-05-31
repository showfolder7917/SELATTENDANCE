package com.sp.selfsp.uniauth.module.service.impl;

import com.sp.selfsp.uniauth.common.UniauthAuditLogWriter;
import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.common.UniauthPermissionGuard;
import com.sp.selfsp.uniauth.common.UniauthValueSupport;
import com.sp.selfsp.uniauth.module.dao.UniauthModuleDao;
import com.sp.selfsp.uniauth.module.domain.in.UniauthModuleSaveIn;
import com.sp.selfsp.uniauth.module.domain.out.UniauthModuleItemOut;
import com.sp.selfsp.uniauth.module.service.UniauthModuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 模块服务实现只负责编排模块主数据保存，不直接耦合菜单、权限和角色关系写入。
@Service
public class UniauthModuleServiceImpl implements UniauthModuleService {

    // 模块 DAO 负责模块主表新增、更新和回查。
    private final UniauthModuleDao uniauthModuleDao;
    // 权限守卫负责按新增或更新动作阻止越权模块维护。
    private final UniauthPermissionGuard uniauthPermissionGuard;
    // 审计日志写入器负责记录模块管理动作来源。
    private final UniauthAuditLogWriter uniauthAuditLogWriter;

    // 构造模块服务时注入模块主表读写和公共守卫能力。
    public UniauthModuleServiceImpl(
        UniauthModuleDao uniauthModuleDao,
        UniauthPermissionGuard uniauthPermissionGuard,
        UniauthAuditLogWriter uniauthAuditLogWriter
    ) {
        // 保存模块 DAO，供保存后回查正式结果复用。
        this.uniauthModuleDao = uniauthModuleDao;
        // 保存权限守卫，供保存前按动作做模块权限拦截。
        this.uniauthPermissionGuard = uniauthPermissionGuard;
        // 保存审计日志写入器，供模块维护成功后记录操作轨迹。
        this.uniauthAuditLogWriter = uniauthAuditLogWriter;
    }

    // 模块保存统一处理新增和更新，并按模块编码回查正式结果。
    @Override
    @Transactional(transactionManager = "uniauthTransactionManager")
    public UniauthModuleItemOut saveModule(UniauthCurrentUser currentUser, UniauthModuleSaveIn saveIn, String requestPath) {
        // 没有模块对象时无法继续保存，因此先阻断空请求。
        if (saveIn == null) {
            // 当前模块区块必须提供完整表单对象，空 payload 直接视为非法调用。
            throw new IllegalArgumentException("module payload 不能为空");
        }
        // 模块编码是权限、菜单和数据范围的共同稳定键，因此不能为空。
        UniauthValueSupport.requireText(saveIn.moduleCode, "moduleCode 不能为空");
        // 模块名称是管理台和角色引用时的主展示字段，因此不能为空。
        UniauthValueSupport.requireText(saveIn.moduleName, "moduleName 不能为空");
        // 新增模块时要求当前账号具备模块创建权限，避免任意账号扩展系统边界。
        if (saveIn.id == null) {
            // 当前新增路径显式要求 create 权限，和细粒度权限码清单保持一致。
            uniauthPermissionGuard.ensurePermission(currentUser, "uniauth.module.create");
        } else {
            // 当前更新路径显式要求 update 权限，避免只有查看权限的账号修改模块定义。
            uniauthPermissionGuard.ensurePermission(currentUser, "uniauth.module.update");
        }
        // 模块类型缺失时默认写成 business，避免新增业务域时每次手工补固定值。
        saveIn.moduleType = UniauthValueSupport.blankToDefault(saveIn.moduleType, "business");
        // 归属系统缺失时默认回到 moduleCode，避免空 ownerSystem 破坏列表展示。
        saveIn.ownerSystem = UniauthValueSupport.blankToDefault(saveIn.ownerSystem, saveIn.moduleCode.trim());
        // 启用标记缺失时默认启用，保证新模块创建后可直接被菜单和角色消费。
        saveIn.enabledFlag = saveIn.enabledFlag == null ? Boolean.TRUE : saveIn.enabledFlag;
        // 没有 id 时按新增路径写入模块主表。
        if (saveIn.id == null) {
            // 新增模块只写模块主表，不在这里隐式创建菜单和权限定义，避免职责混叠。
            uniauthModuleDao.insertModule(saveIn);
        } else {
            // 更新模块时覆盖当前模块的主资料和入口描述。
            uniauthModuleDao.updateModule(saveIn);
        }
        // 按模块编码回查正式结果，保证返回对象和数据库真实状态一致。
        UniauthModuleItemOut moduleRow = uniauthModuleDao.selectModuleByCode(saveIn.moduleCode.trim());
        // 模块保存成功后记录审计日志，方便后续追踪谁改动了模块边界。
        uniauthAuditLogWriter.write(currentUser, "save-module", "module", String.valueOf(moduleRow.getId()), requestPath, "success");
        return moduleRow;
    }
}
