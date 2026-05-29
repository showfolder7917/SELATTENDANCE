/*
 * 文件名：AttendanceBootstrapDao.java
 * 描述：考勤首页聚合数据访问接口。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.bootstrap.dao;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 考勤首页聚合数据访问接口。
 */
@Mapper
public interface AttendanceBootstrapDao {

    /**
     * 读取首页向导统计。
     *
     * @param tenantId 固定租户主键
     * @return 聚合统计结果
     */
    Map<String, Object> selectCounts(@Param("tenantId") Long tenantId);
}

