package com.sp.selfsp.attendance.workplace.service.impl;

import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.workplace.dao.AttendanceWorkplaceDao;
import com.sp.selfsp.attendance.workplace.service.AttendanceWorkplaceService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AttendanceWorkplaceServiceImpl implements AttendanceWorkplaceService {

    private final AttendanceWorkplaceDao attendanceWorkplaceDao;

    public AttendanceWorkplaceServiceImpl(AttendanceWorkplaceDao attendanceWorkplaceDao) {
        this.attendanceWorkplaceDao = attendanceWorkplaceDao;
    }

    @Override
    public List<AttendanceOut.WorkplaceOut> listWorkplaces() {
        return attendanceWorkplaceDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID);
    }

    @Override
    @Transactional
    public AttendanceOut.WorkplaceOut createWorkplace(AttendanceIn.WorkplaceSaveIn saveIn) {
        validateWorkplace(saveIn);
        normalizeWorkplace(saveIn);
        attendanceWorkplaceDao.insert(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        return attendanceWorkplaceDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn.getWorkplaceCode());
    }

    @Override
    @Transactional
    public AttendanceOut.WorkplaceOut updateWorkplace(Long id, AttendanceIn.WorkplaceSaveIn saveIn) {
        validateId(id);
        validateWorkplace(saveIn);
        requireExisting(id);
        normalizeWorkplace(saveIn);
        attendanceWorkplaceDao.updateById(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        return requireExisting(id);
    }

    @Override
    @Transactional
    public void deleteWorkplace(Long id) {
        validateId(id);
        requireExisting(id);
        if (attendanceWorkplaceDao.countDepartmentsByWorkplaceId(AttendanceTenantContext.DEFAULT_TENANT_ID, id) > 0) {
            throw new IllegalArgumentException("该事业所下仍有部门，无法删除");
        }
        if (attendanceWorkplaceDao.countEmployeesByWorkplaceId(AttendanceTenantContext.DEFAULT_TENANT_ID, id) > 0) {
            throw new IllegalArgumentException("该事业所下仍有员工，无法删除");
        }
        attendanceWorkplaceDao.deleteById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
    }

    private void validateWorkplace(AttendanceIn.WorkplaceSaveIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("workplaceSaveIn 不能为空");
        }
        requireText(saveIn.getWorkplaceCode(), "workplaceCode 不能为空");
        requireText(saveIn.getWorkplaceName(), "workplaceName 不能为空");
        if (!StringUtils.hasText(saveIn.getStatus())) {
            saveIn.setStatus("ACTIVE");
        }
    }

    private void normalizeWorkplace(AttendanceIn.WorkplaceSaveIn saveIn) {
        saveIn.setWorkplaceCode(saveIn.getWorkplaceCode().trim());
        saveIn.setWorkplaceName(saveIn.getWorkplaceName().trim());
        saveIn.setAddress(trimToNull(saveIn.getAddress()));
        saveIn.setPhone(trimToNull(saveIn.getPhone()));
        saveIn.setStatus(saveIn.getStatus().trim());
    }

    private AttendanceOut.WorkplaceOut requireExisting(Long id) {
        AttendanceOut.WorkplaceOut workplaceOut = attendanceWorkplaceDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        if (workplaceOut == null) {
            throw new IllegalArgumentException("事业所不存在，id=" + id);
        }
        return workplaceOut;
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id 必须大于 0");
        }
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

