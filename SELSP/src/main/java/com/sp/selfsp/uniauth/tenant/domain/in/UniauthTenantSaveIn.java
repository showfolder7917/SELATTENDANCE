package com.sp.selfsp.uniauth.tenant.domain.in;

// 租户保存输入对象只承接平台管理员维护租户资料所需字段。
public class UniauthTenantSaveIn {

    // id 有值代表更新，没有值代表新增租户。
    public Long id;
    // tenantCode 是跨系统识别租户的稳定编码。
    public String tenantCode;
    // tenantName 是平台与宿主展示租户的主名称。
    public String tenantName;
    // tenantStatus 控制租户是否可继续登录和访问业务工程。
    public String tenantStatus;
    // contactName 记录当前租户的运营联系人。
    public String contactName;
    // contactEmail 记录联系邮箱，供运营通知和售后定位。
    public String contactEmail;
    // contactPhone 记录联系电话，供人工支援场景使用。
    public String contactPhone;
}
