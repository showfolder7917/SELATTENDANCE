package com.sp.selfsp.attendance.employee.dao;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttendanceEmployeeDao {

    List<AttendanceOut.EmployeeOut> selectList(
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("tenantId") Long tenantId,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("query") AttendanceIn.EmployeeQueryIn queryIn
    );

    AttendanceOut.EmployeeOut selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    AttendanceOut.EmployeeOut selectByEmployeeNo(@Param("tenantId") Long tenantId, @Param("employeeNo") String employeeNo);

    // 按外部员工编号读取已绑定员工，供第三阶段打卡接收自动匹配员工。
    AttendanceOut.EmployeeOut selectByExternalEmployeeId(
        @Param("tenantId") Long tenantId,
        @Param("sourceSystem") String sourceSystem,
        @Param("externalEmployeeId") String externalEmployeeId
    );

    int insert(@Param("tenantId") Long tenantId, @Param("in") AttendanceIn.EmployeeSaveIn in);

    int updateById(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("in") AttendanceIn.EmployeeSaveIn in);

    int deleteWorkRuleByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    int deleteExternalMappingByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    Integer countWorkRuleByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    int insertWorkRule(
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("tenantId") Long tenantId,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("employeeId") Long employeeId,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("workRuleType") String workRuleType,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("dailyMinutes") Integer dailyMinutes,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("weeklyMinutes") Integer weeklyMinutes,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("effectiveStartDate") LocalDate effectiveStartDate
    );

    int updateWorkRule(
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("tenantId") Long tenantId,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("employeeId") Long employeeId,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("workRuleType") String workRuleType,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("dailyMinutes") Integer dailyMinutes,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("weeklyMinutes") Integer weeklyMinutes,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("effectiveStartDate") LocalDate effectiveStartDate
    );

    Integer countExternalMappingByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    int insertExternalMapping(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId, @Param("in") AttendanceIn.ExternalMappingSaveIn in);

    int updateExternalMapping(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId, @Param("in") AttendanceIn.ExternalMappingSaveIn in);
}
