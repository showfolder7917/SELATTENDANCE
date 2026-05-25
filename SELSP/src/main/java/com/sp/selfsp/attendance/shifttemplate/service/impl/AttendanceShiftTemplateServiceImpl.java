package com.sp.selfsp.attendance.shifttemplate.service.impl;

import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.shifttemplate.dao.AttendanceShiftTemplateDao;
import com.sp.selfsp.attendance.shifttemplate.service.AttendanceShiftTemplateService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

// 把当前类注册为服务实现，负责承接业务编排。
@Service
// 定义 考勤班次模板服务Impl，承接当前文件对应的业务职责。
public class AttendanceShiftTemplateServiceImpl implements AttendanceShiftTemplateService {

    // 声明 考勤班次模板数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceShiftTemplateDao attendanceShiftTemplateDao;

    // 定义 考勤班次模板服务Impl 业务动作，负责承接当前模块的处理流程。
    public AttendanceShiftTemplateServiceImpl(AttendanceShiftTemplateDao attendanceShiftTemplateDao) {
        // 把外部传入结果写入 考勤班次模板数据访问 字段，供后续流程继续使用。
        this.attendanceShiftTemplateDao = attendanceShiftTemplateDao;
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 定义 list班次Templates 业务动作，负责承接当前模块的处理流程。
    public List<AttendanceOut.ShiftTemplateOut> listShiftTemplates() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return attendanceShiftTemplateDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID);
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 新增班次模板 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.ShiftTemplateOut createShiftTemplate(AttendanceIn.ShiftTemplateSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateShiftTemplate(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        normalizeShiftTemplate(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        ensureTemplateCodeUnique(saveIn.getTemplateCode(), null);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceShiftTemplateDao.insert(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return attendanceShiftTemplateDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn.getTemplateCode());
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 更新班次模板 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.ShiftTemplateOut updateShiftTemplate(Long id, AttendanceIn.ShiftTemplateSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateId(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateShiftTemplate(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingShiftTemplate(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        normalizeShiftTemplate(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        ensureTemplateCodeUnique(saveIn.getTemplateCode(), id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceShiftTemplateDao.updateById(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return requireExistingShiftTemplate(id);
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 删除班次模板 业务动作，负责承接当前模块的处理流程。
    public void deleteShiftTemplate(Long id) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateId(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingShiftTemplate(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceShiftTemplateDao.deleteById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 生成推荐班次Templates 业务动作，负责承接当前模块的处理流程。
    public List<AttendanceOut.ShiftTemplateOut> generateRecommendedShiftTemplates() {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        Map<String, AttendanceOut.ShiftTemplateOut> existingMap = listShiftTemplates().stream()
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            .collect(Collectors.toMap(AttendanceOut.ShiftTemplateOut::getTemplateCode, item -> item, (left, right) -> left, LinkedHashMap::new));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        List<AttendanceOut.ShiftTemplateOut> createdList = new ArrayList<>();
        // 遍历当前业务集合，逐条完成对应的数据处理动作。
        for (AttendanceIn.ShiftTemplateSaveIn recommended : buildRecommendedTemplates()) {
            // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
            if (existingMap.containsKey(recommended.getTemplateCode())) {
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                continue;
            }
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            attendanceShiftTemplateDao.insert(AttendanceTenantContext.DEFAULT_TENANT_ID, recommended);
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            createdList.add(attendanceShiftTemplateDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, recommended.getTemplateCode()));
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return createdList;
    }

    // 定义 validate班次模板 业务动作，负责承接当前模块的处理流程。
    private void validateShiftTemplate(AttendanceIn.ShiftTemplateSaveIn saveIn) {
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("shiftTemplateSaveIn 不能为空");
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getTemplateCode(), "templateCode 不能为空");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getTemplateName(), "templateName 不能为空");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getShiftType(), "shiftType 不能为空");
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn.getScheduledBreakMinutes() == null) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            saveIn.setScheduledBreakMinutes(0);
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn.getCrossDay() == null) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            saveIn.setCrossDay(Boolean.FALSE);
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn.getActive() == null) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            saveIn.setActive(Boolean.TRUE);
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (!StringUtils.hasText(saveIn.getColor())) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            saveIn.setColor("BLUE");
        }
    }

    // 定义 normalize班次模板 业务动作，负责承接当前模块的处理流程。
    private void normalizeShiftTemplate(AttendanceIn.ShiftTemplateSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setTemplateCode(saveIn.getTemplateCode().trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setTemplateName(saveIn.getTemplateName().trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setShiftType(saveIn.getShiftType().trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setStartTime(trimToNull(saveIn.getStartTime()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEndTime(trimToNull(saveIn.getEndTime()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setColor(saveIn.getColor().trim());
    }

    // 定义 ensure模板编码Unique 业务动作，负责承接当前模块的处理流程。
    private void ensureTemplateCodeUnique(String templateCode, Long currentId) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.ShiftTemplateOut shiftTemplateOut = attendanceShiftTemplateDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, templateCode.trim());
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (shiftTemplateOut == null) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            return;
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (currentId != null && currentId.equals(shiftTemplateOut.getId())) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            return;
        }
        // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
        throw new IllegalArgumentException("templateCode 已存在");
    }

    // 定义 requireExisting班次模板 业务动作，负责承接当前模块的处理流程。
    private AttendanceOut.ShiftTemplateOut requireExistingShiftTemplate(Long id) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.ShiftTemplateOut shiftTemplateOut = attendanceShiftTemplateDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (shiftTemplateOut == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("班次模板不存在，id=" + id);
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return shiftTemplateOut;
    }

    // 定义 build推荐Templates 业务动作，负责承接当前模块的处理流程。
    private List<AttendanceIn.ShiftTemplateSaveIn> buildRecommendedTemplates() {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        List<AttendanceIn.ShiftTemplateSaveIn> templates = new ArrayList<>();
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        templates.add(buildTemplate("EARLY", "早班 / 早番", "WORK", "09:00:00", "18:00:00", false, 60, "BLUE"));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        templates.add(buildTemplate("LATE", "晚班 / 遅番", "WORK", "13:00:00", "22:00:00", false, 60, "ORANGE"));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        templates.add(buildTemplate("NIGHT", "夜班 / 夜勤", "WORK", "22:00:00", "07:00:00", true, 60, "PURPLE"));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        templates.add(buildTemplate("HALF_AM", "半日早班 / 午前半日", "WORK", "09:00:00", "13:00:00", false, 0, "CYAN"));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        templates.add(buildTemplate("REST", "休息 / 休日", "REST", null, null, false, 0, "GRAY"));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        templates.add(buildTemplate("PAID_LEAVE", "有休 / 有給休暇", "PAID_LEAVE", null, null, false, 0, "GREEN"));
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return templates;
    }

    // 定义 build模板 业务动作，负责承接当前模块的处理流程。
    private AttendanceIn.ShiftTemplateSaveIn buildTemplate(
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        String code,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        String name,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        String shiftType,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        String startTime,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        String endTime,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        boolean crossDay,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        int breakMinutes,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        String color
    // 执行当前业务步骤，推进本行对应的 服务impl 处理。
    ) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceIn.ShiftTemplateSaveIn saveIn = new AttendanceIn.ShiftTemplateSaveIn();
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setTemplateCode(code);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setTemplateName(name);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setShiftType(shiftType);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setStartTime(startTime);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEndTime(endTime);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setCrossDay(crossDay);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setScheduledBreakMinutes(breakMinutes);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setColor(color);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setActive(Boolean.TRUE);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return saveIn;
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

