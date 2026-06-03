package com.sp.selfsp.uniauth.tenant.domain.in;

// 租户保存输入对象只承接平台管理员维护租户资料所需字段。
public class UniauthTenantSaveIn {

    // id 有值代表更新，没有值代表新增租户。
    private Long id;
    // tenantCode 是跨系统识别租户的稳定编码。
    private String tenantCode;
    // tenantName 是平台与宿主展示租户的主名称。
    private String tenantName;
    // tenantStatus 控制租户是否可继续登录和访问业务工程。
    private String tenantStatus;
    // contactName 记录当前租户的运营联系人。
    private String contactName;
    // contactEmail 记录联系邮箱，供运营通知和售后定位。
    private String contactEmail;
    // contactPhone 记录联系电话，供人工支援场景使用。
    private String contactPhone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantStatus() {
        return tenantStatus;
    }

    public void setTenantStatus(String tenantStatus) {
        this.tenantStatus = tenantStatus;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
