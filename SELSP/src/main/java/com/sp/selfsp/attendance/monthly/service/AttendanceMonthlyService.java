package com.sp.selfsp.attendance.monthly.service;

import com.sp.selfsp.attendance.monthly.domain.in.AttendanceMonthlyIn;
import com.sp.selfsp.attendance.monthly.domain.out.AttendanceMonthlyOut;

/**
 * 第六阶段月次汇总与月结服务接口。
 */
public interface AttendanceMonthlyService {

    AttendanceMonthlyOut.MonthlyListOut listMonthly(AttendanceMonthlyIn.MonthlyQueryIn queryIn);

    AttendanceMonthlyOut.MonthlyDetailOut getMonthlyDetail(Long monthlyId);

    AttendanceMonthlyOut.MonthlyRecalculateResultOut recalculateMonthly(AttendanceMonthlyIn.MonthlyRecalculateIn recalculateIn);

    AttendanceMonthlyOut.MonthlyCloseResultOut closeMonthly(AttendanceMonthlyIn.MonthlyCloseIn closeIn);

    void reopenMonthly(AttendanceMonthlyIn.MonthlyReopenIn reopenIn);

    AttendanceMonthlyOut.MonthlyExportOut exportMonthly(AttendanceMonthlyIn.MonthlyQueryIn queryIn);
}
