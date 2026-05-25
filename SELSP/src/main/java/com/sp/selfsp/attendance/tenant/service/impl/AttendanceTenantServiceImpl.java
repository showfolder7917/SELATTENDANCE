/*
 * 文件名：AttendanceTenantServiceImpl.java
 * 描述：考勤租户服务实现。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.tenant.service.impl;

import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.tenant.dao.AttendanceTenantDao;
import com.sp.selfsp.attendance.tenant.service.AttendanceTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 考勤租户服务实现。
 */
// 把当前类注册为服务实现，负责承接业务编排。
@Service
// 定义 考勤租户服务Impl，承接当前文件对应的业务职责。
public class AttendanceTenantServiceImpl implements AttendanceTenantService {

    // 声明 考勤租户数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceTenantDao attendanceTenantDao;

    /**
     * 构造考勤租户服务实现。
     *
     * @param attendanceTenantDao 租户数据访问接口
     */
    // 定义 考勤租户服务Impl 业务动作，负责承接当前模块的处理流程。
    public AttendanceTenantServiceImpl(AttendanceTenantDao attendanceTenantDao) {
        // 把外部传入结果写入 考勤租户数据访问 字段，供后续流程继续使用。
        this.attendanceTenantDao = attendanceTenantDao;
    }

    /**
     * 保存当前租户资料。
     *
     * @param saveIn 保存入参
     * @return 最新租户资料
     */
    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 保存租户 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.TenantOut saveTenant(AttendanceIn.TenantSaveIn saveIn) {
        // 第一阶段只有单租户壳，因此租户资料保存始终固定写回主键 1。
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn == null ? null : saveIn.getTenantCode(), "tenantCode 不能为空");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getTenantName(), "tenantName 不能为空");
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (!StringUtils.hasText(saveIn.getTimezone())) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            saveIn.setTimezone("Asia/Tokyo");
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        Integer count = attendanceTenantDao.countById(AttendanceTenantContext.DEFAULT_TENANT_ID);
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (count != null && count > 0) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            attendanceTenantDao.updateCurrentTenant(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        } else {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            attendanceTenantDao.insertCurrentTenant(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return attendanceTenantDao.selectCurrentTenant(AttendanceTenantContext.DEFAULT_TENANT_ID);
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

