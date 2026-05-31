package com.sp.selfsp.uniauth.role.service;

import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.role.domain.in.UniauthRoleSaveIn;
import com.sp.selfsp.uniauth.role.domain.out.UniauthRoleItemOut;

// 角色服务只承接角色主资料和授权关系维护。
public interface UniauthRoleService {

    // 角色保存动作负责主表写入、权限菜单范围重建和审计记录。
    UniauthRoleItemOut saveRole(UniauthCurrentUser currentUser, UniauthRoleSaveIn saveIn, String requestPath);
}
