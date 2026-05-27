/*
 * AttendanceTenantServiceImpl.java
 * 租户服务实现。
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
 * 租户服务实现。
 */
@Service
public class AttendanceTenantServiceImpl implements AttendanceTenantService {

    // 持久化当前默认租户资料，供租户面板和轻量 bootstrap 共用。
    private final AttendanceTenantDao attendanceTenantDao;

    // 注入租户 DAO，统一负责默认租户的读取与保存。
    public AttendanceTenantServiceImpl(AttendanceTenantDao attendanceTenantDao) {
        // 保存租户 DAO 引用，供当前服务后续全部租户读写动作复用。
        this.attendanceTenantDao = attendanceTenantDao;
    }

    // 读取当前默认租户资料，供轻量首页壳和租户面板初始化直接复用。
    @Override
    public AttendanceOut.TenantOut getCurrentTenant() {
        // 从默认租户上下文读取当前租户，保持首页壳和租户表单来源一致。
        return attendanceTenantDao.selectCurrentTenant(AttendanceTenantContext.DEFAULT_TENANT_ID);
    }

    // 保存当前默认租户资料，供首页租户面板回写基础主数据。
    @Override
    @Transactional
    public AttendanceOut.TenantOut saveTenant(AttendanceIn.TenantSaveIn saveIn) {
        // 校验租户编码，避免首页壳和其他主数据继续绑定无效租户。
        requireText(saveIn == null ? null : saveIn.getTenantCode(), "tenantCode 不能为空");
        // 校验租户名称，避免首页展示和后续业务上下文缺失主名称。
        requireText(saveIn.getTenantName(), "tenantName 不能为空");
        // 当调用方未传时区时补默认值，保持排班与工作台以东京时区运行。
        if (!StringUtils.hasText(saveIn.getTimezone())) {
            // 把默认时区回写到保存入参，确保后续插入或更新都写入统一值。
            saveIn.setTimezone("Asia/Tokyo");
        }
        // 先判断默认租户是否已经存在，决定走更新还是首次插入流程。
        Integer count = attendanceTenantDao.countById(AttendanceTenantContext.DEFAULT_TENANT_ID);
        // 已存在默认租户时直接更新当前记录，保持租户主键稳定。
        if (count != null && count > 0) {
            // 把最新租户资料更新到默认租户记录，供首页壳立即生效。
            attendanceTenantDao.updateCurrentTenant(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        } else {
            // 首次初始化场景下插入默认租户记录，供后续主数据全部归属该租户。
            attendanceTenantDao.insertCurrentTenant(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        }
        // 保存后回读最新租户资料，保证前端面板拿到数据库中的最终值。
        return getCurrentTenant();
    }

    // 统一校验必填文本字段，避免把空主数据写入租户主表。
    private void requireText(String value, String message) {
        // 当文本为空时立即抛出参数异常，阻止错误租户资料继续进入持久化。
        if (!StringUtils.hasText(value)) {
            // 抛出明确的必填提示，供接口层直接返回给调用方修正输入。
            throw new IllegalArgumentException(message);
        }
    }
}
