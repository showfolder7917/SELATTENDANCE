package com.sp.selfsp.attendance.daily.service;

import com.sp.selfsp.attendance.daily.domain.in.AttendanceDailyIn;
import com.sp.selfsp.attendance.daily.domain.out.AttendanceDailyOut;

/**
 * 第四阶段日次勤怠服务接口。
 */
public interface AttendanceDailyService {

    // 查询当前筛选范围内的日次结果列表，并在必要时补算缺失结果。
    AttendanceDailyOut.DailyListOut listDailyResults(AttendanceDailyIn.DailyQueryIn queryIn);

    // 读取单条日次详情，供右侧详情抽屉查看计算过程和异常解释。
    AttendanceDailyOut.DailyDetailOut getDailyDetail(Long dailyId);

    // 重算指定员工指定日期的日次结果，供异常处理和补卡后回算。
    AttendanceDailyOut.RecalculateResultOut recalculateDaily(AttendanceDailyIn.DailyRecalculateIn recalculateIn);

    // 按日期范围批量重算日次结果，供列表页顶部批量刷新使用。
    AttendanceDailyOut.RecalculateResultOut recalculateRange(AttendanceDailyIn.DailyRecalculateRangeIn recalculateRangeIn);

    // 第五阶段管理员锁定指定日次结果，避免后续审批完成结果被再次任意改动。
    void lockDaily(Long dailyId);

    // 第五阶段管理员解锁指定日次结果，供审批修正后重新开放少量调整窗口。
    void unlockDaily(Long dailyId);
}
