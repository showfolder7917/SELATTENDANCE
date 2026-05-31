package com.sp.selfsp.uniauth.bootstrap.service;

import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.bootstrap.domain.out.UniauthBootstrapOut;

// 工作台服务只承接权限中心管理工作台读取，不再混入登录和保存动作。
public interface UniauthBootstrapService {

    // 工作台读取动作负责一次性返回租户、用户、角色、菜单和权限聚合结果。
    UniauthBootstrapOut getWorkbench(UniauthCurrentUser currentUser);
}
