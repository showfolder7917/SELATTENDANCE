package com.sp.selfsp.uniauth.menu.service.impl;

import com.sp.selfsp.uniauth.common.UniauthAuditLogWriter;
import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.common.UniauthPermissionGuard;
import com.sp.selfsp.uniauth.common.UniauthValueSupport;
import com.sp.selfsp.uniauth.menu.domain.in.UniauthMenuSaveIn;
import com.sp.selfsp.uniauth.menu.domain.out.UniauthMenuItemOut;
import com.sp.selfsp.uniauth.menu.dao.UniauthMenuDao;
import com.sp.selfsp.uniauth.menu.service.UniauthMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 菜单服务实现只负责编排菜单树节点保存。
@Service
public class UniauthMenuServiceImpl implements UniauthMenuService {

    // 菜单 DAO 负责菜单节点主表读写。
    private final UniauthMenuDao uniauthMenuDao;
    // 权限守卫负责校验当前账号是否拥有菜单写权限。
    private final UniauthPermissionGuard uniauthPermissionGuard;
    // 审计日志写入器负责记录菜单树改动。
    private final UniauthAuditLogWriter uniauthAuditLogWriter;

    // 构造菜单服务时注入菜单写入与公共守卫能力。
    public UniauthMenuServiceImpl(
        UniauthMenuDao uniauthMenuDao,
        UniauthPermissionGuard uniauthPermissionGuard,
        UniauthAuditLogWriter uniauthAuditLogWriter
    ) {
        // 保存菜单 DAO，供新增、更新和回查正式结果复用。
        this.uniauthMenuDao = uniauthMenuDao;
        // 保存权限守卫，供保存前先做菜单写权限校验。
        this.uniauthPermissionGuard = uniauthPermissionGuard;
        // 保存审计日志写入器，供保存成功后记录菜单变更来源。
        this.uniauthAuditLogWriter = uniauthAuditLogWriter;
    }

    // 菜单保存统一处理新增、更新和双语标题维护。
    @Override
    @Transactional(transactionManager = "uniauthTransactionManager")
    public UniauthMenuItemOut saveMenu(UniauthCurrentUser currentUser, UniauthMenuSaveIn saveIn, String requestPath) {
        // 菜单维护前必须先具备菜单写权限，避免普通账号改宿主导航。
        uniauthPermissionGuard.ensurePermission(currentUser, "uniauth.menu.write");
        // 菜单编码是前后端稳定键，不能为空。
        UniauthValueSupport.requireText(saveIn == null ? null : saveIn.menuCode, "menuCode 不能为空");
        // 中文标题不能为空，否则中文界面会出现空白节点。
        UniauthValueSupport.requireText(saveIn.titleZh, "titleZh 不能为空");
        // 日文标题不能为空，否则日文界面无法完成双语切换。
        UniauthValueSupport.requireText(saveIn.titleJa, "titleJa 不能为空");
        // 排序缺失时默认给 0，保证新节点仍能进树。
        saveIn.sortOrder = saveIn.sortOrder == null ? 0 : saveIn.sortOrder;
        // 启用标记缺失时默认启用，减少首次建节点时的额外操作。
        saveIn.enabledFlag = saveIn.enabledFlag == null ? Boolean.TRUE : saveIn.enabledFlag;
        // 没有 id 时按新增节点路径写菜单表。
        if (saveIn.id == null) {
            // 新增节点直接写菜单主表，后续按编码回查真实结果。
            uniauthMenuDao.insertMenu(saveIn);
        } else {
            // 带 id 时按更新路径覆盖路由、图标、标题和启停状态。
            uniauthMenuDao.updateMenu(saveIn);
        }
        // 按菜单编码回查正式结果，保证返回值与数据库一致。
        UniauthMenuItemOut menuRow = uniauthMenuDao.selectMenuByCode(saveIn.menuCode.trim());
        // 菜单维护成功后写审计日志，方便定位谁改动了导航树。
        uniauthAuditLogWriter.write(currentUser, "save-menu", "menu", String.valueOf(menuRow.getId()), requestPath, "success");
        return menuRow;
    }
}
