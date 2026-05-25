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

// 把当前类注册为服务实现，负责承接业务编排。
@Service
// 定义 考勤事业所服务Impl，承接当前文件对应的业务职责。
public class AttendanceWorkplaceServiceImpl implements AttendanceWorkplaceService {

    // 声明 考勤事业所数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceWorkplaceDao attendanceWorkplaceDao;

    // 定义 考勤事业所服务Impl 业务动作，负责承接当前模块的处理流程。
    public AttendanceWorkplaceServiceImpl(AttendanceWorkplaceDao attendanceWorkplaceDao) {
        // 把外部传入结果写入 考勤事业所数据访问 字段，供后续流程继续使用。
        this.attendanceWorkplaceDao = attendanceWorkplaceDao;
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 定义 listWorkplaces 业务动作，负责承接当前模块的处理流程。
    public List<AttendanceOut.WorkplaceOut> listWorkplaces() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return attendanceWorkplaceDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID);
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 新增事业所 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.WorkplaceOut createWorkplace(AttendanceIn.WorkplaceSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateWorkplace(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        normalizeWorkplace(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceWorkplaceDao.insert(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return attendanceWorkplaceDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn.getWorkplaceCode());
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 更新事业所 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.WorkplaceOut updateWorkplace(Long id, AttendanceIn.WorkplaceSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateId(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateWorkplace(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExisting(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        normalizeWorkplace(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceWorkplaceDao.updateById(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return requireExisting(id);
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 删除事业所 业务动作，负责承接当前模块的处理流程。
    public void deleteWorkplace(Long id) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateId(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExisting(id);
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (attendanceWorkplaceDao.countDepartmentsByWorkplaceId(AttendanceTenantContext.DEFAULT_TENANT_ID, id) > 0) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("该事业所下仍有部门，无法删除");
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (attendanceWorkplaceDao.countEmployeesByWorkplaceId(AttendanceTenantContext.DEFAULT_TENANT_ID, id) > 0) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("该事业所下仍有员工，无法删除");
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceWorkplaceDao.deleteById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
    }

    // 定义 validate事业所 业务动作，负责承接当前模块的处理流程。
    private void validateWorkplace(AttendanceIn.WorkplaceSaveIn saveIn) {
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("workplaceSaveIn 不能为空");
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getWorkplaceCode(), "workplaceCode 不能为空");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getWorkplaceName(), "workplaceName 不能为空");
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (!StringUtils.hasText(saveIn.getStatus())) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            saveIn.setStatus("ACTIVE");
        }
    }

    // 定义 normalize事业所 业务动作，负责承接当前模块的处理流程。
    private void normalizeWorkplace(AttendanceIn.WorkplaceSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setWorkplaceCode(saveIn.getWorkplaceCode().trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setWorkplaceName(saveIn.getWorkplaceName().trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setAddress(trimToNull(saveIn.getAddress()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setPhone(trimToNull(saveIn.getPhone()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setStatus(saveIn.getStatus().trim());
    }

    // 定义 requireExisting 业务动作，负责承接当前模块的处理流程。
    private AttendanceOut.WorkplaceOut requireExisting(Long id) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.WorkplaceOut workplaceOut = attendanceWorkplaceDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (workplaceOut == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("事业所不存在，id=" + id);
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return workplaceOut;
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

    // 定义 trimToNull 业务动作，负责承接当前模块的处理流程。
    private String trimToNull(String value) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

