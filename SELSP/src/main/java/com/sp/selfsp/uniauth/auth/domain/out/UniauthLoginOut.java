package com.sp.selfsp.uniauth.auth.domain.out;

import com.sp.selfsp.uniauth.bootstrap.domain.out.UniauthBootstrapOut;
import com.sp.selfsp.uniauth.common.domain.out.UniauthCurrentUserOut;

// 登录输出负责一次性下发 token、当前用户快照和权限中心首屏工作台。
public class UniauthLoginOut {

    // accessToken 让前端后续请求都能带上统一的 Bearer 登录态。
    private String accessToken;
    // currentUser 让前端在登录返回后立即拿到当前用户上下文。
    private UniauthCurrentUserOut currentUser;
    // workbench 让登录后首屏不需要再额外发一次初始化请求。
    private UniauthBootstrapOut workbench;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public UniauthCurrentUserOut getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UniauthCurrentUserOut currentUser) {
        this.currentUser = currentUser;
    }

    public UniauthBootstrapOut getWorkbench() {
        return workbench;
    }

    public void setWorkbench(UniauthBootstrapOut workbench) {
        this.workbench = workbench;
    }
}
