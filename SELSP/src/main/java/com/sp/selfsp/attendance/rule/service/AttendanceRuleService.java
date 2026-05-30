package com.sp.selfsp.attendance.rule.service;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;

public interface AttendanceRuleService {

    // 读取第七阶段规则工作台所需的规则、适用和预警聚合结果。
    AttendanceOut.RuleWorkbenchOut loadWorkbench(AttendanceIn.RuleWorkbenchQueryIn queryIn);

    // 新增一条正式规则配置，供管理员沉淀日本勤怠口径。
    AttendanceOut.RuleOut createRule(AttendanceIn.RuleSaveIn saveIn);

    // 更新既有规则配置，供管理员修正规则口径。
    AttendanceOut.RuleOut updateRule(Long id, AttendanceIn.RuleSaveIn saveIn);

    // 给指定员工绑定正式规则，供第七阶段员工适用页落地。
    AttendanceOut.EmployeeRuleAssignmentOut assignRule(Long employeeId, AttendanceIn.RuleAssignmentSaveIn saveIn);
}
