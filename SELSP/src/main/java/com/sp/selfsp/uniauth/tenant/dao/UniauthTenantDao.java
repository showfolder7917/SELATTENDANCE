package com.sp.selfsp.uniauth.tenant.dao;

import com.sp.selfsp.uniauth.tenant.domain.in.UniauthTenantSaveIn;
import com.sp.selfsp.uniauth.tenant.domain.out.UniauthTenantItemOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 租户 DAO 只承接租户主表读写，不再混入用户、角色和菜单 SQL。
@Mapper
public interface UniauthTenantDao {

    // 新增租户主表记录，供平台管理员建立新租户。
    int insertTenant(@Param("in") UniauthTenantSaveIn in);

    // 更新租户主表记录，供平台管理员调整租户资料与状态。
    int updateTenant(@Param("in") UniauthTenantSaveIn in);

    // 按租户编码回查主表结果，供保存后返回真实落库结果。
    UniauthTenantItemOut selectTenantByCode(@Param("tenantCode") String tenantCode);
}
