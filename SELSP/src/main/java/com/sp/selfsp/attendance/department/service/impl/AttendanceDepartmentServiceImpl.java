package com.sp.selfsp.attendance.department.service.impl;

import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.department.dao.AttendanceDepartmentDao;
import com.sp.selfsp.attendance.department.service.AttendanceDepartmentService;
import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.workplace.dao.AttendanceWorkplaceDao;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

// 把当前类注册为服务实现，负责承接业务编排。
@Service
// 定义 考勤部门服务Impl，承接当前文件对应的业务职责。
public class AttendanceDepartmentServiceImpl implements AttendanceDepartmentService {

    // 声明 考勤部门数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceDepartmentDao attendanceDepartmentDao;
    // 声明 考勤事业所数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceWorkplaceDao attendanceWorkplaceDao;

    // 定义 考勤部门服务Impl 业务动作，负责承接当前模块的处理流程。
    public AttendanceDepartmentServiceImpl(
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceDepartmentDao attendanceDepartmentDao,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceWorkplaceDao attendanceWorkplaceDao
    // 执行当前业务步骤，推进本行对应的 服务impl 处理。
    ) {
        // 把外部传入结果写入 考勤部门数据访问 字段，供后续流程继续使用。
        this.attendanceDepartmentDao = attendanceDepartmentDao;
        // 把外部传入结果写入 考勤事业所数据访问 字段，供后续流程继续使用。
        this.attendanceWorkplaceDao = attendanceWorkplaceDao;
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 定义 listDepartments 业务动作，负责承接当前模块的处理流程。
    public List<AttendanceOut.DepartmentOut> listDepartments() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return attendanceDepartmentDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID);
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 新增部门 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.DepartmentOut createDepartment(AttendanceIn.DepartmentSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateDepartment(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingWorkplace(saveIn.getWorkplaceId());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        normalizeDepartment(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceDepartmentDao.insert(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return attendanceDepartmentDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn.getDepartmentCode());
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 更新部门 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.DepartmentOut updateDepartment(Long id, AttendanceIn.DepartmentSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateId(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateDepartment(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingDepartment(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingWorkplace(saveIn.getWorkplaceId());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        normalizeDepartment(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceDepartmentDao.updateById(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return requireExistingDepartment(id);
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 删除部门 业务动作，负责承接当前模块的处理流程。
    public void deleteDepartment(Long id) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateId(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingDepartment(id);
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (attendanceDepartmentDao.countEmployeesByDepartmentId(AttendanceTenantContext.DEFAULT_TENANT_ID, id) > 0) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("该部门下仍有员工，无法删除");
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceDepartmentDao.deleteById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
    }

    // 定义 validate部门 业务动作，负责承接当前模块的处理流程。
    private void validateDepartment(AttendanceIn.DepartmentSaveIn saveIn) {
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("departmentSaveIn 不能为空");
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn.getWorkplaceId() == null || saveIn.getWorkplaceId() <= 0) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("workplaceId 不能为空");
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getDepartmentCode(), "departmentCode 不能为空");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getDepartmentName(), "departmentName 不能为空");
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn.getSortOrder() == null) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            saveIn.setSortOrder(0);
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (!StringUtils.hasText(saveIn.getStatus())) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            saveIn.setStatus("ACTIVE");
        }
    }

    // 定义 normalize部门 业务动作，负责承接当前模块的处理流程。
    private void normalizeDepartment(AttendanceIn.DepartmentSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setDepartmentCode(saveIn.getDepartmentCode().trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setDepartmentName(saveIn.getDepartmentName().trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setStatus(saveIn.getStatus().trim());
    }

    // 定义 requireExisting事业所 业务动作，负责承接当前模块的处理流程。
    private void requireExistingWorkplace(Long workplaceId) {
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (attendanceWorkplaceDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, workplaceId) == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("事业所不存在，id=" + workplaceId);
        }
    }

    // 定义 requireExisting部门 业务动作，负责承接当前模块的处理流程。
    private AttendanceOut.DepartmentOut requireExistingDepartment(Long id) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.DepartmentOut departmentOut = attendanceDepartmentDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (departmentOut == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("部门不存在，id=" + id);
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return departmentOut;
    }

    // 定义 validateId 业务动作，负责承接当前模块的处理流程。
    private void validateId(Long id) {
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (id == null || id <= 0) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("id 必须大于 0");
        }
    }

    // 定义 requireText 业务动作，负责承接当前模块的处理流程。
    private void requireText(String value, String message) {
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (!StringUtils.hasText(value)) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException(message);
        }
    }
}

