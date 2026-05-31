package com.sp.selfsp.uniauth.user.domain.in;

import java.util.List;

// 用户保存输入对象只承接账号资料与角色关系维护所需字段。
public class UniauthUserSaveIn {

    // id 有值代表更新现有账号，没有值代表创建新账号。
    public Long id;
    // tenantId 决定账号归属的租户边界。
    public Long tenantId;
    // loginName 是权限中心稳定登录标识。
    public String loginName;
    // password 只在新增或显式重置密码时生效。
    public String password;
    // displayName 用于平台和宿主展示当前用户。
    public String displayName;
    // displayNameKana 用于日语场景补充假名显示。
    public String displayNameKana;
    // locale 决定用户默认文案语言。
    public String locale;
    // email 记录用户基础联系邮箱。
    public String email;
    // phone 记录用户基础联系电话。
    public String phone;
    // userStatus 控制账号是否可登录。
    public String userStatus;
    // roleIds 直接承接当前账号应绑定的角色主键集合。
    public List<Long> roleIds;
}
