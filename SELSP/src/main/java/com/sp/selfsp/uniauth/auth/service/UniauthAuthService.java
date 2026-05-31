package com.sp.selfsp.uniauth.auth.service;

import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.auth.domain.in.UniauthLoginIn;
import com.sp.selfsp.uniauth.auth.domain.out.UniauthLoginOut;
import com.sp.selfsp.uniauth.common.domain.out.UniauthCurrentUserOut;

// 认证服务只承接登录和当前用户快照，不再混入租户、用户、角色和菜单管理动作。
public interface UniauthAuthService {

    // 登录动作负责把账号密码换成 JWT 和首屏工作台数据。
    UniauthLoginOut login(UniauthLoginIn loginIn, String requestPath, String clientIp);

    // 当前用户快照动作负责把 JWT 已解析的上下文映射给前端。
    UniauthCurrentUserOut getCurrentUserSnapshot(UniauthCurrentUser currentUser);
}
