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
// 把当前接口注册为 MyBatis Mapper，负责数据库读写映射。
@Mapper
// 定义 考勤初始化聚合数据访问，承接当前文件对应的业务职责。
public interface AttendanceBootstrapDao {

    /**
     * 读取首页向导统计。
     *
     * @param tenantId 固定租户主键
     * @return 聚合统计结果
     */
    // 执行当前业务步骤，推进本行对应的 数据访问 处理。
    Map<String, Object> selectCounts(@Param("tenantId") Long tenantId);
}

