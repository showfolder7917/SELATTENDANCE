/*
 * 文件名：AttendanceTenantDao.java
 * 描述：考勤租户数据访问接口。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.tenant.dao;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 考勤租户数据访问接口。
 */
@Mapper
public interface AttendanceTenantDao {

    AttendanceOut.TenantOut selectCurrentTenant(@Param("id") Long id);

    Integer countById(@Param("id") Long id);

    int insertCurrentTenant(@Param("id") Long id, @Param("in") AttendanceIn.TenantSaveIn in);

    int updateCurrentTenant(@Param("id") Long id, @Param("in") AttendanceIn.TenantSaveIn in);
}

