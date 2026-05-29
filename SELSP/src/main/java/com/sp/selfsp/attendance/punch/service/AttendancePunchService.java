package com.sp.selfsp.attendance.punch.service;

import com.sp.selfsp.attendance.punch.domain.in.AttendancePunchIn;
import com.sp.selfsp.attendance.punch.domain.out.AttendancePunchOut;

/**
 * 第三阶段打卡服务接口。
 */
public interface AttendancePunchService {

    // 读取打卡记录列表，供第三阶段打卡页面首屏加载。
    AttendancePunchOut.PunchLogListOut listPunchLogs(AttendancePunchIn.PunchLogQueryIn queryIn);

    // 读取单条打卡详情，供右侧详情和错误处理动作复用。
    AttendancePunchOut.PunchLogDetailOut getPunchLogDetail(Long id);

    // 手动补录一条打卡事实，供管理员处理漏打卡场景。
    AttendancePunchOut.PunchManualResultOut createManualPunch(AttendancePunchIn.PunchManualSaveIn saveIn);

    // 先预览 CSV 解析结果，供用户在正式导入前查看风险。
    AttendancePunchOut.PunchImportPreviewOut previewImport(AttendancePunchIn.PunchImportIn saveIn);

    // 正式导入 CSV 文本，供第三阶段批量落地原始打卡事实。
    AttendancePunchOut.PunchImportResultOut importCsv(AttendancePunchIn.PunchImportIn saveIn);

    // 接收 Webhook 打卡，供外部网关把实时打卡事件推入系统。
    AttendancePunchOut.PunchManualResultOut receiveWebhook(AttendancePunchIn.PunchWebhookIn saveIn);

    // 对未匹配记录绑定系统员工，供用户修复外部编号未映射问题。
    AttendancePunchOut.PunchLogDetailOut bindEmployee(Long id, AttendancePunchIn.PunchBindEmployeeIn saveIn);

    // 忽略误导入或明确不再处理的记录，供列表从待处理流转到已忽略。
    AttendancePunchOut.PunchLogDetailOut ignoreLog(Long id, AttendancePunchIn.PunchIgnoreIn saveIn);

    // 重新处理既有记录，供映射修复后重新匹配员工。
    AttendancePunchOut.PunchLogDetailOut reprocessLog(Long id);
}
