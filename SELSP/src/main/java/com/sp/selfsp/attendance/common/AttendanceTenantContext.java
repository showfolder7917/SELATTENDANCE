/*
 * 文件名：AttendanceTenantContext.java
 * 描述：考勤第一阶段单租户上下文常量。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.common;

/**
 * 考勤第一阶段单租户上下文常量。
 *
 * <p>当前项目仍按单租户样例库运行，所以服务和 DAO 统一从这里取固定租户主键。</p>
 */
public final class AttendanceTenantContext {

    /**
     * 第一阶段固定租户主键。
     */
    // 所有考勤主数据和排班查询默认都落在示例租户 1 上，便于本地测试数据复用。
    public static final long DEFAULT_TENANT_ID = 1L;

    private AttendanceTenantContext() {
        // 工具类不允许实例化，避免误把租户上下文当成可变对象使用。
    }
}

