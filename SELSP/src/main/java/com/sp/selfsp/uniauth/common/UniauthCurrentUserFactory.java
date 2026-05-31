package com.sp.selfsp.uniauth.common;

import com.sp.selfsp.uniauth.auth.dao.UniauthAuthDao;
import com.sp.selfsp.uniauth.auth.domain.UniauthAuthUser;
import org.springframework.stereotype.Component;

// 当前用户工厂统一把数据库主数据和权限关系拼成 JWT 与宿主共用的用户上下文。
@Component
public class UniauthCurrentUserFactory {

    // 认证 DAO 负责读取登录人主数据、权限码、菜单码和数据范围。
    private final UniauthAuthDao uniauthAuthDao;

    // 构造工厂时注入认证 DAO，保证登录链和 bootstrap 链使用同一套上下文组装逻辑。
    public UniauthCurrentUserFactory(UniauthAuthDao uniauthAuthDao) {
        // 保存认证 DAO，供 buildCurrentUser 方法统一查库。
        this.uniauthAuthDao = uniauthAuthDao;
    }

    // 把用户主数据和关联关系组装成标准当前用户上下文，供 JWT 签发与宿主消费复用。
    public UniauthCurrentUser buildCurrentUser(UniauthAuthUser userRow) {
        // 创建新的上下文对象，准备承接当前账号的身份和授权快照。
        UniauthCurrentUser currentUser = new UniauthCurrentUser();
        // 用户主键是后续所有操作日志和宿主桥接的基础标识。
        currentUser.setUserId(userRow.getId());
        // 登录名用于 token 主体和调试输出。
        currentUser.setUsername(userRow.getLoginName());
        // 展示名用于页面头部与审计日志显示。
        currentUser.setDisplayName(userRow.getDisplayName());
        // 语言偏好决定权限中心与宿主默认使用哪套文案。
        currentUser.setLocale(UniauthValueSupport.blankToDefault(userRow.getLocale(), "zh-CN"));
        // tenantId 为空时回退到示例租户，保证本地最小闭环仍可稳定联调。
        currentUser.setTenantId(userRow.getTenantId() == null || userRow.getTenantId() == 0L ? 1L : userRow.getTenantId());
        // tenantCode 供跨系统页面与日志展示当前租户。
        currentUser.setTenantCode(UniauthValueSupport.blankToDefault(userRow.getTenantCode(), "DEFAULT"));
        // tenantStatus 让宿主和权限中心都能及时感知租户启停状态。
        currentUser.setTenantStatus(UniauthValueSupport.blankToDefault(userRow.getTenantStatus(), "enabled"));
        // 权限码列表供接口鉴权和按钮显隐复用。
        currentUser.setPermissionCodes(uniauthAuthDao.selectPermissionCodesByUserId(currentUser.getUserId()));
        // 菜单码列表供宿主工程和权限中心动态导航消费。
        currentUser.setMenuCodes(uniauthAuthDao.selectMenuCodesByUserId(currentUser.getUserId()));
        // 数据范围列表供 attendance 宿主后续叠加部门或本人过滤。
        currentUser.setDataScopes(uniauthAuthDao.selectDataScopesByUserId(currentUser.getUserId()));
        return currentUser;
    }
}
