package com.sp.selfsp.uniauth.menu.service;

import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.menu.domain.in.UniauthMenuSaveIn;
import com.sp.selfsp.uniauth.menu.domain.out.UniauthMenuItemOut;

// 菜单服务只承接动态导航节点维护。
public interface UniauthMenuService {

    // 菜单保存动作负责新增或更新节点并回写审计日志。
    UniauthMenuItemOut saveMenu(UniauthCurrentUser currentUser, UniauthMenuSaveIn saveIn, String requestPath);
}
