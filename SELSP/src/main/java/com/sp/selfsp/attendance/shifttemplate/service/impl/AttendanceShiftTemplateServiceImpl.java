package com.sp.selfsp.attendance.shifttemplate.service.impl;

import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
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

@Service
public class AttendanceShiftTemplateServiceImpl implements AttendanceShiftTemplateService {

    private final AttendanceShiftTemplateDao attendanceShiftTemplateDao;

    public AttendanceShiftTemplateServiceImpl(AttendanceShiftTemplateDao attendanceShiftTemplateDao) {
        this.attendanceShiftTemplateDao = attendanceShiftTemplateDao;
    }

    @Override
    public List<AttendanceOut.ShiftTemplateOut> listShiftTemplates() {
        return attendanceShiftTemplateDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID);
    }

    @Override
    @Transactional
    public AttendanceOut.ShiftTemplateOut createShiftTemplate(AttendanceIn.ShiftTemplateSaveIn saveIn) {
        validateShiftTemplate(saveIn);
        normalizeShiftTemplate(saveIn);
        ensureTemplateCodeUnique(saveIn.getTemplateCode(), null);
        attendanceShiftTemplateDao.insert(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        return attendanceShiftTemplateDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn.getTemplateCode());
    }

    @Override
    @Transactional
    public AttendanceOut.ShiftTemplateOut updateShiftTemplate(Long id, AttendanceIn.ShiftTemplateSaveIn saveIn) {
        validateId(id);
        validateShiftTemplate(saveIn);
        requireExistingShiftTemplate(id);
        normalizeShiftTemplate(saveIn);
        ensureTemplateCodeUnique(saveIn.getTemplateCode(), id);
        attendanceShiftTemplateDao.updateById(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        return requireExistingShiftTemplate(id);
    }

    @Override
    @Transactional
    public void deleteShiftTemplate(Long id) {
        validateId(id);
        requireExistingShiftTemplate(id);
        attendanceShiftTemplateDao.deleteById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
    }

    @Override
    @Transactional
    public List<AttendanceOut.ShiftTemplateOut> generateRecommendedShiftTemplates() {
        Map<String, AttendanceOut.ShiftTemplateOut> existingMap = listShiftTemplates().stream()
            .collect(Collectors.toMap(AttendanceOut.ShiftTemplateOut::getTemplateCode, item -> item, (left, right) -> left, LinkedHashMap::new));
        List<AttendanceOut.ShiftTemplateOut> createdList = new ArrayList<>();
        for (AttendanceIn.ShiftTemplateSaveIn recommended : buildRecommendedTemplates()) {
            if (existingMap.containsKey(recommended.getTemplateCode())) {
                continue;
            }
            attendanceShiftTemplateDao.insert(AttendanceTenantContext.DEFAULT_TENANT_ID, recommended);
            createdList.add(attendanceShiftTemplateDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, recommended.getTemplateCode()));
        }
        return createdList;
    }

    private void validateShiftTemplate(AttendanceIn.ShiftTemplateSaveIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("shiftTemplateSaveIn 不能为空");
        }
        requireText(saveIn.getTemplateCode(), "templateCode 不能为空");
        requireText(saveIn.getTemplateName(), "templateName 不能为空");
        requireText(saveIn.getShiftType(), "shiftType 不能为空");
        if (saveIn.getScheduledBreakMinutes() == null) {
            saveIn.setScheduledBreakMinutes(0);
        }
        if (saveIn.getCrossDay() == null) {
            saveIn.setCrossDay(Boolean.FALSE);
        }
        if (saveIn.getActive() == null) {
            saveIn.setActive(Boolean.TRUE);
        }
        if (!StringUtils.hasText(saveIn.getColor())) {
            saveIn.setColor("BLUE");
        }
    }

    private void normalizeShiftTemplate(AttendanceIn.ShiftTemplateSaveIn saveIn) {
        saveIn.setTemplateCode(saveIn.getTemplateCode().trim());
        saveIn.setTemplateName(saveIn.getTemplateName().trim());
        saveIn.setShiftType(saveIn.getShiftType().trim());
        saveIn.setStartTime(trimToNull(saveIn.getStartTime()));
        saveIn.setEndTime(trimToNull(saveIn.getEndTime()));
        saveIn.setColor(saveIn.getColor().trim());
    }

    private void ensureTemplateCodeUnique(String templateCode, Long currentId) {
        AttendanceOut.ShiftTemplateOut shiftTemplateOut = attendanceShiftTemplateDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, templateCode.trim());
        if (shiftTemplateOut == null) {
            return;
        }
        if (currentId != null && currentId.equals(shiftTemplateOut.getId())) {
            return;
        }
        throw new IllegalArgumentException("templateCode 已存在");
    }

    private AttendanceOut.ShiftTemplateOut requireExistingShiftTemplate(Long id) {
        AttendanceOut.ShiftTemplateOut shiftTemplateOut = attendanceShiftTemplateDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        if (shiftTemplateOut == null) {
            throw new IllegalArgumentException("班次模板不存在，id=" + id);
        }
        return shiftTemplateOut;
    }

    private List<AttendanceIn.ShiftTemplateSaveIn> buildRecommendedTemplates() {
        List<AttendanceIn.ShiftTemplateSaveIn> templates = new ArrayList<>();
        templates.add(buildTemplate("EARLY", "早班 / 早番", "WORK", "09:00:00", "18:00:00", false, 60, "BLUE"));
        templates.add(buildTemplate("LATE", "晚班 / 遅番", "WORK", "13:00:00", "22:00:00", false, 60, "ORANGE"));
        templates.add(buildTemplate("NIGHT", "夜班 / 夜勤", "WORK", "22:00:00", "07:00:00", true, 60, "PURPLE"));
        templates.add(buildTemplate("HALF_AM", "半日早班 / 午前半日", "WORK", "09:00:00", "13:00:00", false, 0, "CYAN"));
        templates.add(buildTemplate("REST", "休息 / 休日", "REST", null, null, false, 0, "GRAY"));
        templates.add(buildTemplate("PAID_LEAVE", "有休 / 有給休暇", "PAID_LEAVE", null, null, false, 0, "GREEN"));
        return templates;
    }

    private AttendanceIn.ShiftTemplateSaveIn buildTemplate(
        String code,
        String name,
        String shiftType,
        String startTime,
        String endTime,
        boolean crossDay,
        int breakMinutes,
        String color
    ) {
        AttendanceIn.ShiftTemplateSaveIn saveIn = new AttendanceIn.ShiftTemplateSaveIn();
        saveIn.setTemplateCode(code);
        saveIn.setTemplateName(name);
        saveIn.setShiftType(shiftType);
        saveIn.setStartTime(startTime);
        saveIn.setEndTime(endTime);
        saveIn.setCrossDay(crossDay);
        saveIn.setScheduledBreakMinutes(breakMinutes);
        saveIn.setColor(color);
        saveIn.setActive(Boolean.TRUE);
        return saveIn;
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id 必须大于 0");
        }
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

