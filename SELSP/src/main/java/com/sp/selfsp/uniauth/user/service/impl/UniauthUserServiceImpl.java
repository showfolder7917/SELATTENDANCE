package com.sp.selfsp.uniauth.user.service.impl;

import com.sp.selfsp.uniauth.common.UniauthAuditLogWriter;
import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.common.UniauthPermissionGuard;
import com.sp.selfsp.uniauth.common.UniauthValueSupport;
import com.sp.selfsp.uniauth.user.domain.in.UniauthUserSaveIn;
import com.sp.selfsp.uniauth.user.domain.out.UniauthUserItemOut;
import com.sp.selfsp.uniauth.security.UniauthJwtSupport;
import com.sp.selfsp.uniauth.user.dao.UniauthUserDao;
import com.sp.selfsp.uniauth.user.service.UniauthUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 用户服务实现只负责编排账号保存和角色关系重建。
@Service
public class UniauthUserServiceImpl implements UniauthUserService {

    // 用户 DAO 负责账号主表和用户角色关系读写。
    private final UniauthUserDao uniauthUserDao;
    // JWT 支撑类负责在重置密码时生成统一摘要。
    private final UniauthJwtSupport uniauthJwtSupport;
    // 权限守卫负责校验当前账号是否拥有用户写权限。
    private final UniauthPermissionGuard uniauthPermissionGuard;
    // 审计日志写入器负责记录用户维护动作。
    private final UniauthAuditLogWriter uniauthAuditLogWriter;

    // 构造用户服务时注入用户写入和通用权限依赖。
    public UniauthUserServiceImpl(
        UniauthUserDao uniauthUserDao,
        UniauthJwtSupport uniauthJwtSupport,
        UniauthPermissionGuard uniauthPermissionGuard,
        UniauthAuditLogWriter uniauthAuditLogWriter
    ) {
        // 保存用户 DAO，供主表读写和关系重建复用。
        this.uniauthUserDao = uniauthUserDao;
        // 保存 JWT 支撑类，供密码摘要统一口径。
        this.uniauthJwtSupport = uniauthJwtSupport;
        // 保存权限守卫，供保存前做越权拦截。
        this.uniauthPermissionGuard = uniauthPermissionGuard;
        // 保存审计日志写入器，供保存成功后记录操作轨迹。
        this.uniauthAuditLogWriter = uniauthAuditLogWriter;
    }

    // 用户保存统一处理新增、更新、密码重置和角色关系重建。
    @Override
    @Transactional(transactionManager = "uniauthTransactionManager")
    public UniauthUserItemOut saveUser(UniauthCurrentUser currentUser, UniauthUserSaveIn saveIn, String requestPath) {
        // 用户保存前必须先拥有账号维护权限，避免租户管理员误碰平台账号。
        uniauthPermissionGuard.ensurePermission(currentUser, "uniauth.user.write");
        // 登录名是权限中心账号稳定键，不能为空。
        UniauthValueSupport.requireText(saveIn == null ? null : saveIn.getLoginName(), "loginName 不能为空");
        // 展示名是界面和日志主名称，不能为空。
        UniauthValueSupport.requireText(saveIn.getDisplayName(), "displayName 不能为空");
        // locale 缺失时默认使用中文，保证最小闭环界面有稳定文案。
        saveIn.setLocale(UniauthValueSupport.blankToDefault(saveIn.getLocale(), "zh-CN"));
        // userStatus 缺失时默认激活，减少首次创建账号的额外动作。
        saveIn.setUserStatus(UniauthValueSupport.blankToDefault(saveIn.getUserStatus(), "ACTIVE"));
        // 只有显式传入新密码时才重算摘要，避免更新资料时意外覆盖旧密码。
        String passwordHash = saveIn.getPassword() == null || saveIn.getPassword().isBlank()
            ? ""
            : uniauthJwtSupport.hashPassword(saveIn.getPassword().trim());
        // 没有 id 时按新增账号路径写主表。
        if (saveIn.getId() == null) {
            // 新增账号时必须显式提供密码，否则无法形成可登录账号。
            if (passwordHash.isEmpty()) {
                throw new IllegalArgumentException("新增用户时 password 不能为空");
            }
            // 先写入账号主表，再按登录名回查新主键。
            uniauthUserDao.insertUser(saveIn, passwordHash);
            // 把数据库生成的主键写回输入对象，供后续关系表保存复用。
            saveIn.setId(UniauthValueSupport.longValue(uniauthUserDao.selectUserByLoginName(saveIn.getLoginName().trim()).getId()));
        } else {
            // 带 id 时按更新账号路径覆盖资料和可选密码摘要。
            uniauthUserDao.updateUser(saveIn, passwordHash);
        }
        // 先删除旧角色关系，保证本次提交结果就是账号最终角色集合。
        uniauthUserDao.deleteUserRoles(saveIn.getId());
        // 逐个写入当前账号应绑定的角色主键集合。
        for (Long roleId : UniauthValueSupport.nullSafeLongList(saveIn.getRoleIds())) {
            // 每条关系单独写入，表达账号拥有哪些角色授权来源。
            uniauthUserDao.insertUserRole(saveIn.getId(), roleId);
        }
        // 回查正式账号结果，保证返回值和数据库一致。
        UniauthUserItemOut userRow = uniauthUserDao.selectUserById(saveIn.getId());
        // 用户维护成功后写审计日志，方便后续追踪账号改动来源。
        uniauthAuditLogWriter.write(currentUser, "save-user", "user", String.valueOf(saveIn.getId()), requestPath, "success");
        return userRow;
    }
}
