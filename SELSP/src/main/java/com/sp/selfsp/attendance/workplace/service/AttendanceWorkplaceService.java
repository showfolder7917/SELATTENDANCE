package com.sp.selfsp.attendance.workplace.service;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import java.util.List;

public interface AttendanceWorkplaceService {

    List<AttendanceOut.WorkplaceOut> listWorkplaces();

    AttendanceOut.WorkplaceOut createWorkplace(AttendanceIn.WorkplaceSaveIn saveIn);

    AttendanceOut.WorkplaceOut updateWorkplace(Long id, AttendanceIn.WorkplaceSaveIn saveIn);

    void deleteWorkplace(Long id);
}

