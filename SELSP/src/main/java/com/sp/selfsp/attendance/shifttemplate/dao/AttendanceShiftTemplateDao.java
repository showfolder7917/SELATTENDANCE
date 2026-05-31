package com.sp.selfsp.attendance.shifttemplate.dao;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttendanceShiftTemplateDao {

    List<AttendanceOut.ShiftTemplateOut> selectList(@Param("tenantId") Long tenantId);

    AttendanceOut.ShiftTemplateOut selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    AttendanceOut.ShiftTemplateOut selectByCode(@Param("tenantId") Long tenantId, @Param("templateCode") String templateCode);

    int insert(@Param("tenantId") Long tenantId, @Param("in") AttendanceIn.ShiftTemplateSaveIn in);

    int updateById(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("in") AttendanceIn.ShiftTemplateSaveIn in);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

