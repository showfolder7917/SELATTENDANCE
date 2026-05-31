package com.sp.selfsp.uniauth.tenant.service;

import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.tenant.domain.in.UniauthTenantSaveIn;
import com.sp.selfsp.uniauth.tenant.domain.out.UniauthTenantItemOut;

// 租户服务只承接平台租户资料维护，不再混入用户、角色和菜单动作。
public interface UniauthTenantService {

    // 租户保存动作负责新增或更新租户主资料并回写审计日志。
    UniauthTenantItemOut saveTenant(UniauthCurrentUser currentUser, UniauthTenantSaveIn saveIn, String requestPath);
}
