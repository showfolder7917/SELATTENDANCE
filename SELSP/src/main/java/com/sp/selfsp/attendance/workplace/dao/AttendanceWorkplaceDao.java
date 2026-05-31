/*
 * 文件名：AttendanceWorkplaceDao.java
 * 描述：考勤事业所数据访问接口。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.workplace.dao;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 考勤事业所数据访问接口。
 */
@Mapper
public interface AttendanceWorkplaceDao {

    List<AttendanceOut.WorkplaceOut> selectList(@Param("tenantId") Long tenantId);

    AttendanceOut.WorkplaceOut selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    AttendanceOut.WorkplaceOut selectByCode(@Param("tenantId") Long tenantId, @Param("workplaceCode") String workplaceCode);

    int insert(@Param("tenantId") Long tenantId, @Param("in") AttendanceIn.WorkplaceSaveIn in);

    int updateById(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("in") AttendanceIn.WorkplaceSaveIn in);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    Long countEmployeesByWorkplaceId(@Param("tenantId") Long tenantId, @Param("workplaceId") Long workplaceId);

    Long countDepartmentsByWorkplaceId(@Param("tenantId") Long tenantId, @Param("workplaceId") Long workplaceId);
}

