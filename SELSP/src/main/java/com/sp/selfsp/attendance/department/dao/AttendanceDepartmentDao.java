package com.sp.selfsp.attendance.department.dao;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 把当前接口注册为 MyBatis Mapper，负责数据库读写映射。
@Mapper
// 定义 考勤部门数据访问，承接当前文件对应的业务职责。
public interface AttendanceDepartmentDao {

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    List<AttendanceOut.DepartmentOut> selectList(@Param("tenantId") Long tenantId);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    AttendanceOut.DepartmentOut selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    AttendanceOut.DepartmentOut selectByCode(@Param("tenantId") Long tenantId, @Param("departmentCode") String departmentCode);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int insert(@Param("tenantId") Long tenantId, @Param("in") AttendanceIn.DepartmentSaveIn in);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int updateById(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("in") AttendanceIn.DepartmentSaveIn in);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    Long countEmployeesByDepartmentId(@Param("tenantId") Long tenantId, @Param("departmentId") Long departmentId);
}

