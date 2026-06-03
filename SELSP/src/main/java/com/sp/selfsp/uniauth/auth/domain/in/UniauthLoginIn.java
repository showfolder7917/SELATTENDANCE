package com.sp.selfsp.uniauth.auth.domain.in;

// 登录输入对象只承接权限中心登录链路需要的账号和密码。
public class UniauthLoginIn {

    // 登录名用于定位权限中心用户主数据。
    private String loginName;
    // 密码用于和库里的摘要值做一致性比对。
    private String password;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
