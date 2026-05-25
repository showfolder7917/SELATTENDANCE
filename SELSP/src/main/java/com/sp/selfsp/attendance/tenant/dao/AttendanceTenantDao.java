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
// 把当前接口注册为 MyBatis Mapper，负责数据库读写映射。
@Mapper
// 定义 考勤租户数据访问，承接当前文件对应的业务职责。
public interface AttendanceTenantDao {

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    AttendanceOut.TenantOut selectCurrentTenant(@Param("id") Long id);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    Integer countById(@Param("id") Long id);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int insertCurrentTenant(@Param("id") Long id, @Param("in") AttendanceIn.TenantSaveIn in);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int updateCurrentTenant(@Param("id") Long id, @Param("in") AttendanceIn.TenantSaveIn in);
}

