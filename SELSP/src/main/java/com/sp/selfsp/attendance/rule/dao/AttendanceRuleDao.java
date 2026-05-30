package com.sp.selfsp.attendance.rule.dao;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttendanceRuleDao {

    // 读取规则列表，供第七阶段规则区块展示正式规则清单。
    List<AttendanceOut.RuleOut> selectRuleList(
        @Param("tenantId") Long tenantId,
        @Param("query") AttendanceIn.RuleWorkbenchQueryIn queryIn
    );

    // 按主键读取单条规则，供编辑和员工适用回填。
    AttendanceOut.RuleOut selectRuleById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // 按编码读取规则，供保存时做唯一性校验。
    AttendanceOut.RuleOut selectRuleByCode(@Param("tenantId") Long tenantId, @Param("ruleCode") String ruleCode);

    // 新增规则主数据，供第七阶段正式创建规则配置。
    int insertRule(@Param("tenantId") Long tenantId, @Param("in") AttendanceIn.RuleSaveIn saveIn);

    // 更新规则主数据，供第七阶段维护既有规则。
    int updateRule(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("in") AttendanceIn.RuleSaveIn saveIn);

    // 读取员工规则适用和月次预警基础数据，供规则页右侧适用列表和提醒看板使用。
    List<AttendanceOut.EmployeeRuleAssignmentOut> selectAssignments(
        @Param("tenantId") Long tenantId,
        @Param("yearMonth") String yearMonth,
        @Param("yearStartMonth") String yearStartMonth,
        @Param("keyword") String keyword
    );

    // 按员工主键读取单条适用关系，供保存后立即回填右侧详情。
    AttendanceOut.EmployeeRuleAssignmentOut selectAssignmentByEmployeeId(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("yearMonth") String yearMonth,
        @Param("yearStartMonth") String yearStartMonth
    );

    // 判断员工当前是否已有规则适用记录，供选择插入还是更新。
    Integer countAssignmentByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    // 新建员工规则适用，并把规则关键口径复制进 employee_work_rule 供后续日次计算复用。
    int insertAssignment(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("ruleId") Long ruleId,
        @Param("workRuleType") String workRuleType,
        @Param("dailyMinutes") Integer dailyMinutes,
        @Param("weeklyMinutes") Integer weeklyMinutes,
        @Param("effectiveStartDate") LocalDate effectiveStartDate,
        @Param("effectiveEndDate") LocalDate effectiveEndDate,
        @Param("note") String note
    );

    // 更新员工规则适用，并同步覆盖 employee_work_rule 中的正式规则口径。
    int updateAssignment(
        @Param("tenantId") Long tenantId,
        @Param("employeeId") Long employeeId,
        @Param("ruleId") Long ruleId,
        @Param("workRuleType") String workRuleType,
        @Param("dailyMinutes") Integer dailyMinutes,
        @Param("weeklyMinutes") Integer weeklyMinutes,
        @Param("effectiveStartDate") LocalDate effectiveStartDate,
        @Param("effectiveEndDate") LocalDate effectiveEndDate,
        @Param("note") String note
    );
}
