package com.sp.selfsp.attendance.casefile.service;

import com.sp.selfsp.attendance.casefile.domain.in.AttendanceCaseIn;
import com.sp.selfsp.attendance.casefile.domain.out.AttendanceCaseOut;

/**
 * 第五阶段异常处理与审批服务接口。
 */
public interface AttendanceCaseService {

    // 查询第五阶段异常处理与审批列表。
    AttendanceCaseOut.CaseListOut listCases(AttendanceCaseIn.CaseQueryIn queryIn);

    // 读取单条处理单详情。
    AttendanceCaseOut.CaseDetailOut getCaseDetail(Long caseId);

    // 基于日次异常创建处理单并提交到审批流。
    AttendanceCaseOut.CaseMutationOut createCase(AttendanceCaseIn.CaseCreateIn createIn);

    // 对单条处理单执行审批动作。
    AttendanceCaseOut.CaseMutationOut applyAction(Long caseId, AttendanceCaseIn.CaseActionIn actionIn);

    // 对多条处理单执行相同审批动作。
    AttendanceCaseOut.CaseMutationOut batchApplyAction(AttendanceCaseIn.CaseBatchActionIn actionIn);
}
