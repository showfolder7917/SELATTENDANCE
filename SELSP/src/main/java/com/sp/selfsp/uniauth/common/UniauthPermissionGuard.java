package com.sp.selfsp.uniauth.common;

import org.springframework.stereotype.Component;

// 权限守卫只负责校验当前登录者是否具备目标权限码，避免每个服务手工写 contains 判定。
@Component
public class UniauthPermissionGuard {

    // 统一校验当前用户是否拥有目标权限，供 bootstrap 和管理子域复用。
    public void ensurePermission(UniauthCurrentUser currentUser, String permissionCode) {
        // 登录用户为空说明调用方没有先经过鉴权上下文，直接按非法访问阻断。
        if (currentUser == null) {
            // 当前没有用户上下文时无法判断权限，因此直接抛出错误。
            throw new IllegalArgumentException("当前未登录");
        }
        // 当前账号没有目标权限码时直接拒绝，避免越权写平台或租户数据。
        if (!currentUser.getPermissionCodes().contains(permissionCode)) {
            // 错误消息带上权限码，方便联调和运营排查是哪个按钮或接口缺授权。
            throw new IllegalArgumentException("当前账号缺少权限：" + permissionCode);
        }
    }
}
