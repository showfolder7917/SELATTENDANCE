package com.sp.selfsp.attendance.department.service.impl;

import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.department.dao.AttendanceDepartmentDao;
import com.sp.selfsp.attendance.department.service.AttendanceDepartmentService;
import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.workplace.dao.AttendanceWorkplaceDao;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AttendanceDepartmentServiceImpl implements AttendanceDepartmentService {

    private final AttendanceDepartmentDao attendanceDepartmentDao;
    private final AttendanceWorkplaceDao attendanceWorkplaceDao;

    public AttendanceDepartmentServiceImpl(
        AttendanceDepartmentDao attendanceDepartmentDao,
        AttendanceWorkplaceDao attendanceWorkplaceDao
    ) {
        this.attendanceDepartmentDao = attendanceDepartmentDao;
        this.attendanceWorkplaceDao = attendanceWorkplaceDao;
    }

    @Override
    public List<AttendanceOut.DepartmentOut> listDepartments() {
        return attendanceDepartmentDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID);
    }

    @Override
    @Transactional
    public AttendanceOut.DepartmentOut createDepartment(AttendanceIn.DepartmentSaveIn saveIn) {
        validateDepartment(saveIn);
        requireExistingWorkplace(saveIn.getWorkplaceId());
        normalizeDepartment(saveIn);
        attendanceDepartmentDao.insert(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        return attendanceDepartmentDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn.getDepartmentCode());
    }

    @Override
    @Transactional
    public AttendanceOut.DepartmentOut updateDepartment(Long id, AttendanceIn.DepartmentSaveIn saveIn) {
        validateId(id);
        validateDepartment(saveIn);
        requireExistingDepartment(id);
        requireExistingWorkplace(saveIn.getWorkplaceId());
        normalizeDepartment(saveIn);
        attendanceDepartmentDao.updateById(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        return requireExistingDepartment(id);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        validateId(id);
        requireExistingDepartment(id);
        if (attendanceDepartmentDao.countEmployeesByDepartmentId(AttendanceTenantContext.DEFAULT_TENANT_ID, id) > 0) {
            throw new IllegalArgumentException("该部门下仍有员工，无法删除");
        }
        attendanceDepartmentDao.deleteById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
    }

    private void validateDepartment(AttendanceIn.DepartmentSaveIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("departmentSaveIn 不能为空");
        }
        if (saveIn.getWorkplaceId() == null || saveIn.getWorkplaceId() <= 0) {
            throw new IllegalArgumentException("workplaceId 不能为空");
        }
        requireText(saveIn.getDepartmentCode(), "departmentCode 不能为空");
        requireText(saveIn.getDepartmentName(), "departmentName 不能为空");
        if (saveIn.getSortOrder() == null) {
            saveIn.setSortOrder(0);
        }
        if (!StringUtils.hasText(saveIn.getStatus())) {
            saveIn.setStatus("ACTIVE");
        }
    }

    private void normalizeDepartment(AttendanceIn.DepartmentSaveIn saveIn) {
        saveIn.setDepartmentCode(saveIn.getDepartmentCode().trim());
        saveIn.setDepartmentName(saveIn.getDepartmentName().trim());
        saveIn.setStatus(saveIn.getStatus().trim());
    }

    private void requireExistingWorkplace(Long workplaceId) {
        if (attendanceWorkplaceDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, workplaceId) == null) {
            throw new IllegalArgumentException("事业所不存在，id=" + workplaceId);
        }
    }

    private AttendanceOut.DepartmentOut requireExistingDepartment(Long id) {
        AttendanceOut.DepartmentOut departmentOut = attendanceDepartmentDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        if (departmentOut == null) {
            throw new IllegalArgumentException("部门不存在，id=" + id);
        }
        return departmentOut;
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
}

