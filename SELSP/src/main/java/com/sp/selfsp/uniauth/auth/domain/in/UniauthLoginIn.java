package com.sp.selfsp.uniauth.auth.domain.in;

// 登录输入对象只承接权限中心登录链路需要的账号和密码。
public class UniauthLoginIn {

    // 登录名用于定位权限中心用户主数据。
    public String loginName;
    // 密码用于和库里的摘要值做一致性比对。
    public String password;
}
