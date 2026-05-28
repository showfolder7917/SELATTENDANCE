package com.sp.selfsp.attendance.employee.dao;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 把当前接口注册为 MyBatis Mapper，负责数据库读写映射。
@Mapper
// 定义 考勤员工数据访问，承接当前文件对应的业务职责。
public interface AttendanceEmployeeDao {

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    List<AttendanceOut.EmployeeOut> selectList(
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("tenantId") Long tenantId,
        // 给当前参数声明 SQL 命名参数，便于 XML 正确取值。
        @Param("query") AttendanceIn.EmployeeQueryIn queryIn
    );

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    AttendanceOut.EmployeeOut selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    AttendanceOut.EmployeeOut selectByEmployeeNo(@Param("tenantId") Long tenantId, @Param("employeeNo") String employeeNo);

    // 按外部员工编号读取已绑定员工，供第三阶段打卡接收自动匹配员工。
    AttendanceOut.EmployeeOut selectByExternalEmployeeId(
        @Param("tenantId") Long tenantId,
        @Param("sourceSystem") String sourceSystem,
        @Param("externalEmployeeId") String externalEmployeeId
    );

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int insert(@Param("tenantId") Long tenantId, @Param("in") AttendanceIn.EmployeeSaveIn in);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int updateById(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("in") AttendanceIn.EmployeeSaveIn in);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int deleteWorkRuleByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int deleteExternalMappingByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    Integer countWorkRuleByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
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

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
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

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    Integer countExternalMappingByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int insertExternalMapping(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId, @Param("in") AttendanceIn.ExternalMappingSaveIn in);

    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    int updateExternalMapping(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId, @Param("in") AttendanceIn.ExternalMappingSaveIn in);
}
