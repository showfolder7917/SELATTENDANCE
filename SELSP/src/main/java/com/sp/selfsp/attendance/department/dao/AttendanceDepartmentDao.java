package com.sp.selfsp.attendance.department.dao;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttendanceDepartmentDao {

    List<AttendanceOut.DepartmentOut> selectList(@Param("tenantId") Long tenantId);

    AttendanceOut.DepartmentOut selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    AttendanceOut.DepartmentOut selectByCode(@Param("tenantId") Long tenantId, @Param("departmentCode") String departmentCode);

    int insert(@Param("tenantId") Long tenantId, @Param("in") AttendanceIn.DepartmentSaveIn in);

    int updateById(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("in") AttendanceIn.DepartmentSaveIn in);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    Long countEmployeesByDepartmentId(@Param("tenantId") Long tenantId, @Param("departmentId") Long departmentId);
}

