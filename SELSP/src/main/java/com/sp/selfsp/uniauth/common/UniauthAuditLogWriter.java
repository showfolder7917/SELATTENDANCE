package com.sp.selfsp.uniauth.common;

import com.sp.selfsp.uniauth.bootstrap.dao.UniauthBootstrapDao;
import org.springframework.stereotype.Component;

// 审计日志写入器统一收口平台权限动作日志，避免每个子域都手动拼同一套审计字段。
@Component
public class UniauthAuditLogWriter {

    // 审计落库依赖 bootstrap 侧公共写接口，因为审计本身不属于单一管理子域。
    private final UniauthBootstrapDao uniauthBootstrapDao;

    // 构造写入器时注入公共 DAO，供所有管理动作复用同一落库路径。
    public UniauthAuditLogWriter(UniauthBootstrapDao uniauthBootstrapDao) {
        // 保存公共 DAO，后续登录、租户、用户、角色和菜单动作都走这里。
        this.uniauthBootstrapDao = uniauthBootstrapDao;
    }

    // 统一记录一次权限中心动作，保持操作模块、对象类型和请求路径口径一致。
    public void write(UniauthCurrentUser currentUser, String actionType, String targetType, String targetId, String requestPath, String resultStatus) {
        // 把日志统一标记为 uniauth 模块，便于后续按模块筛选平台动作。
        uniauthBootstrapDao.insertAuditLog(
            currentUser.getTenantId(),
            currentUser.getUserId(),
            currentUser.getDisplayName(),
            "uniauth",
            actionType,
            targetType,
            targetId,
            resultStatus,
            requestPath,
            currentUser.getUsername() + " executed " + actionType
        );
    }
}
