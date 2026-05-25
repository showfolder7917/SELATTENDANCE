/*
 * 文件名：AttendanceTenantContext.java
 * 描述：考勤第一阶段单租户上下文常量。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.common;

/**
 * 考勤第一阶段单租户上下文常量。
 */
// 定义 考勤租户Context，承接当前文件对应的业务职责。
public final class AttendanceTenantContext {

    /**
     * 第一阶段固定租户主键。
     */
    // 声明 DEFAULT租户ID 字段，用来保存当前业务状态或依赖。
    public static final long DEFAULT_TENANT_ID = 1L;

    // 定义 考勤租户Context 处理入口，承接当前业务动作。
    private AttendanceTenantContext() {
    }
}

