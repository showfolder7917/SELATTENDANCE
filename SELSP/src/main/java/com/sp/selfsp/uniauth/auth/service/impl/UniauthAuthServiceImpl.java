package com.sp.selfsp.uniauth.auth.service.impl;

import com.sp.selfsp.uniauth.auth.dao.UniauthAuthDao;
import com.sp.selfsp.uniauth.auth.service.UniauthAuthService;
import com.sp.selfsp.uniauth.bootstrap.service.UniauthBootstrapService;
import com.sp.selfsp.uniauth.common.UniauthAuditLogWriter;
import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.common.UniauthCurrentUserFactory;
import com.sp.selfsp.uniauth.common.UniauthValueSupport;
import com.sp.selfsp.uniauth.common.UniauthViewMapper;
import com.sp.selfsp.uniauth.auth.domain.UniauthAuthUser;
import com.sp.selfsp.uniauth.auth.domain.in.UniauthLoginIn;
import com.sp.selfsp.uniauth.auth.domain.out.UniauthLoginOut;
import com.sp.selfsp.uniauth.common.domain.out.UniauthCurrentUserOut;
import com.sp.selfsp.uniauth.security.UniauthJwtSupport;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 认证服务实现只负责编排登录与快照，不再承担管理工作台和保存动作。
@Service
public class UniauthAuthServiceImpl implements UniauthAuthService {

    // 认证 DAO 负责登录链路所需的用户和权限查询。
    private final UniauthAuthDao uniauthAuthDao;
    // JWT 支撑类负责密码摘要与 access token 签发。
    private final UniauthJwtSupport uniauthJwtSupport;
    // 当前用户工厂负责把数据库主数据收成宿主可消费的统一上下文。
    private final UniauthCurrentUserFactory uniauthCurrentUserFactory;
    // 视图映射器负责把上下文对象转成前端 JSON 结构。
    private final UniauthViewMapper uniauthViewMapper;
    // 工作台服务负责登录成功后补齐首屏聚合数据。
    private final UniauthBootstrapService uniauthBootstrapService;
    // 审计日志写入器负责记录登录动作。
    private final UniauthAuditLogWriter uniauthAuditLogWriter;

    // 构造认证服务时注入认证、工作台和审计依赖。
    public UniauthAuthServiceImpl(
        UniauthAuthDao uniauthAuthDao,
        UniauthJwtSupport uniauthJwtSupport,
        UniauthCurrentUserFactory uniauthCurrentUserFactory,
        UniauthViewMapper uniauthViewMapper,
        UniauthBootstrapService uniauthBootstrapService,
        UniauthAuditLogWriter uniauthAuditLogWriter
    ) {
        // 保存认证 DAO，供登录主流程查询用户和权限关系。
        this.uniauthAuthDao = uniauthAuthDao;
        // 保存 JWT 支撑类，供密码摘要和 token 签发复用。
        this.uniauthJwtSupport = uniauthJwtSupport;
        // 保存当前用户工厂，供登录后统一组装上下文。
        this.uniauthCurrentUserFactory = uniauthCurrentUserFactory;
        // 保存视图映射器，供当前用户快照和登录响应复用。
        this.uniauthViewMapper = uniauthViewMapper;
        // 保存工作台服务，供登录成功后一次性下发首屏数据。
        this.uniauthBootstrapService = uniauthBootstrapService;
        // 保存审计日志写入器，供登录动作写审计。
        this.uniauthAuditLogWriter = uniauthAuditLogWriter;
    }

    // 登录流程统一校验账号、签发 token、记录会话并返回首屏工作台。
    @Override
    @Transactional(transactionManager = "uniauthTransactionManager")
    public UniauthLoginOut login(UniauthLoginIn loginIn, String requestPath, String clientIp) {
        // 登录名为空时直接阻断，避免查全表或出现无语义错误。
        UniauthValueSupport.requireText(loginIn == null ? null : loginIn.getLoginName(), "loginName 不能为空");
        // 密码为空时直接阻断，避免空密码被误传入摘要算法。
        UniauthValueSupport.requireText(loginIn == null ? null : loginIn.getPassword(), "password 不能为空");
        // 先按登录名读取用户主数据，判断账号是否存在。
        UniauthAuthUser userRow = uniauthAuthDao.selectUserByLoginName(loginIn.getLoginName().trim());
        // 查不到账号时直接提示账号或密码错误，避免暴露用户是否存在。
        if (userRow == null) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        // 用户状态不是激活时不允许建立新的权限中心会话。
        if (!"ACTIVE".equalsIgnoreCase(UniauthValueSupport.blankToDefault(userRow.getUserStatus(), ""))) {
            throw new IllegalArgumentException("当前账号已停用");
        }
        // 账号被锁定时直接阻断，避免风险账号继续访问。
        if (Boolean.TRUE.equals(userRow.getLockedFlag())) {
            throw new IllegalArgumentException("当前账号已锁定");
        }
        // 把当前输入密码按统一口径摘要，准备和库中的摘要比对。
        String incomingPasswordHash = uniauthJwtSupport.hashPassword(loginIn.getPassword().trim());
        // 密码摘要不一致时直接阻断登录。
        if (!incomingPasswordHash.equals(UniauthValueSupport.blankToDefault(userRow.getPasswordHash(), ""))) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        // 登录成功后把主数据和授权关系组装成标准当前用户上下文。
        UniauthCurrentUser currentUser = uniauthCurrentUserFactory.buildCurrentUser(userRow);
        // 基于当前用户上下文签发 access token，供权限中心与宿主共用。
        String accessToken = uniauthJwtSupport.createAccessToken(currentUser);
        // 记录一次轻量登录会话，方便排查最近登录来源和失效情况。
        uniauthAuthDao.insertLoginSession(
            currentUser.getUserId(),
            uniauthJwtSupport.hashPassword(accessToken),
            "browser",
            clientIp,
            LocalDateTime.now().plusHours(8)
        );
        // 登录成功写审计日志，保证平台级登录动作可追溯。
        uniauthAuditLogWriter.write(currentUser, "login", "user", String.valueOf(currentUser.getUserId()), requestPath, "success");
        // 创建明确登录输出对象，避免 controller 和测试继续依赖匿名键值结构。
        UniauthLoginOut loginOut = new UniauthLoginOut();
        // accessToken 供前端写入统一会话，再带给宿主接口。
        loginOut.setAccessToken(accessToken);
        // currentUser 供前端头部、语言和租户上下文立即消费。
        loginOut.setCurrentUser(uniauthViewMapper.toCurrentUserOut(currentUser));
        // workbench 供前端在登录后直接渲染主工作台，无需额外串行初始化请求。
        loginOut.setWorkbench(uniauthBootstrapService.getWorkbench(currentUser));
        return loginOut;
    }

    // 当前用户快照只负责把 JWT 已解析出的上下文回显给前端。
    @Override
    public UniauthCurrentUserOut getCurrentUserSnapshot(UniauthCurrentUser currentUser) {
        // 直接复用统一视图映射器，保证快照字段和登录响应一致。
        return uniauthViewMapper.toCurrentUserOut(currentUser);
    }
}
