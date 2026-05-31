package com.sp.selfsp.uniauth.module.service;

import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.module.domain.in.UniauthModuleSaveIn;
import com.sp.selfsp.uniauth.module.domain.out.UniauthModuleItemOut;

// 模块服务接口只暴露模块管理保存动作，供权限中心模块区块复用。
public interface UniauthModuleService {

    // 保存模块主数据，统一承接新增和更新动作。
    UniauthModuleItemOut saveModule(UniauthCurrentUser currentUser, UniauthModuleSaveIn saveIn, String requestPath);
}
