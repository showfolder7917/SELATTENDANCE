package com.sp.selfsp.uniauth.user.service;

import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.user.domain.in.UniauthUserSaveIn;
import com.sp.selfsp.uniauth.user.domain.out.UniauthUserItemOut;

// 用户服务只承接账号资料和用户角色关系维护。
public interface UniauthUserService {

    // 用户保存动作负责主表写入、角色关系重建和审计记录。
    UniauthUserItemOut saveUser(UniauthCurrentUser currentUser, UniauthUserSaveIn saveIn, String requestPath);
}
