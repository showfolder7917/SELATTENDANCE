package com.sp.selfsp.uniauth.tenant.domain.out;

// 租户列表项和保存返回统一复用同一输出对象，保证列表与保存口径一致。
public class UniauthTenantItemOut {

    // id 让前端更新租户时能稳定指向目标记录。
    private Long id;
    // tenantCode 是跨系统识别租户的稳定编码。
    private String tenantCode;
    // tenantName 是平台和宿主展示租户的主名称。
    private String tenantName;
    // tenantStatus 控制租户当前是否可继续访问业务工程。
    private String tenantStatus;
    // contactName 记录当前租户的运营联系人。
    private String contactName;
    // contactEmail 记录当前租户的联系邮箱。
    private String contactEmail;
    // contactPhone 记录当前租户的联系电话。
    private String contactPhone;
    // createdAt 让管理员感知租户建立时间。
    private String createdAt;
    // updatedAt 让管理员判断租户信息最近一次修改时间。
    private String updatedAt;

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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
