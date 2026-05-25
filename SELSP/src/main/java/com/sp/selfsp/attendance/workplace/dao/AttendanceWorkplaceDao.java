/*
 * 文件名：AttendanceWorkplaceDao.java
 * 描述：考勤事业所数据访问接口。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.workplace.dao;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 考勤事业所数据访问接口。
 */
// 把当前接口注册为 MyBatis Mapper，负责数据库读写映射。
@Mapper
// 定义 考勤事业所数据访问，承接当前文件对应的业务职责。
public interface AttendanceWorkplaceDao {

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    List<AttendanceOut.WorkplaceOut> selectList(@Param("tenantId") Long tenantId);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    AttendanceOut.WorkplaceOut selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    AttendanceOut.WorkplaceOut selectByCode(@Param("tenantId") Long tenantId, @Param("workplaceCode") String workplaceCode);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int insert(@Param("tenantId") Long tenantId, @Param("in") AttendanceIn.WorkplaceSaveIn in);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int updateById(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("in") AttendanceIn.WorkplaceSaveIn in);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    Long countEmployeesByWorkplaceId(@Param("tenantId") Long tenantId, @Param("workplaceId") Long workplaceId);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    Long countDepartmentsByWorkplaceId(@Param("tenantId") Long tenantId, @Param("workplaceId") Long workplaceId);
}

