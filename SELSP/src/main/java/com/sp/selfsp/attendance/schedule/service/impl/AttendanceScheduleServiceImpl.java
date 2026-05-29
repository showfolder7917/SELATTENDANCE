package com.sp.selfsp.attendance.schedule.service.impl;

import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.employee.dao.AttendanceEmployeeDao;
import com.sp.selfsp.attendance.schedule.dao.AttendanceScheduleDao;
import com.sp.selfsp.attendance.schedule.domain.in.AttendanceScheduleIn;
import com.sp.selfsp.attendance.schedule.domain.out.AttendanceScheduleOut;
import com.sp.selfsp.attendance.schedule.service.AttendanceScheduleService;
import com.sp.selfsp.attendance.shifttemplate.dao.AttendanceShiftTemplateDao;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 第二阶段排班服务实现。
 */
@Service
public class AttendanceScheduleServiceImpl implements AttendanceScheduleService {

    // 日期格式统一使用年月字符串，保证前后端 month 参数稳定。
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM", Locale.ROOT);

    private final AttendanceEmployeeDao attendanceEmployeeDao;
    private final AttendanceShiftTemplateDao attendanceShiftTemplateDao;
    private final AttendanceScheduleDao attendanceScheduleDao;

    public AttendanceScheduleServiceImpl(
        AttendanceEmployeeDao attendanceEmployeeDao,
        AttendanceShiftTemplateDao attendanceShiftTemplateDao,
        AttendanceScheduleDao attendanceScheduleDao
    ) {
        this.attendanceEmployeeDao = attendanceEmployeeDao;
        this.attendanceShiftTemplateDao = attendanceShiftTemplateDao;
        this.attendanceScheduleDao = attendanceScheduleDao;
    }

    @Override
    public AttendanceScheduleOut.ScheduleBoardOut getScheduleBoard(AttendanceScheduleIn.ScheduleBoardQueryIn queryIn) {
        // 先把月份解析为明确区间，保证列表、未排班计算和导出都围绕同一月展开。
        YearMonth yearMonth = requireMonth(queryIn == null ? null : queryIn.getMonth());
        // 当前月的第一天作为日历左边界。
        LocalDate startDate = yearMonth.atDay(1);
        // 当前月的最后一天作为日历右边界。
        LocalDate endDate = yearMonth.atEndOfMonth();
        // 先拿员工范围，再据此查询当前月排班，避免把无关人的记录返回给前端。
        List<AttendanceOut.EmployeeOut> employeeList = listScheduleEmployees(queryIn);
        // 把员工主键抽出来作为排班查询范围。
        List<Long> employeeIds = employeeList.stream().map(AttendanceOut.EmployeeOut::getId).toList();
        // 当前月排班为空时也要让页面正常展示空状态，因此这里允许返回空列表。
        List<AttendanceScheduleOut.ScheduleItemOut> scheduleItems = employeeIds.isEmpty()
            ? Collections.emptyList()
            : attendanceScheduleDao.selectScheduleItems(AttendanceTenantContext.DEFAULT_TENANT_ID, startDate, endDate, employeeIds);
        // 先按员工和日期索引排班，便于后面计算未排班天数。
        Map<Long, Set<LocalDate>> assignedDateMap = buildAssignedDateMap(scheduleItems);
        // 左侧员工固定列需要带出未排班天数，方便管理员优先处理缺口。
        List<AttendanceScheduleOut.ScheduleEmployeeRowOut> employeeRows = buildEmployeeRows(employeeList, assignedDateMap, startDate, endDate);
        // 如果用户只看未排班，则员工行和排班明细都要同步裁剪。
        if (Boolean.TRUE.equals(queryIn.getOnlyUnassigned())) {
            // 只保留仍有缺口的员工行，减少页面噪音。
            employeeRows = employeeRows.stream()
                .filter(item -> item.getUnassignedCount() != null && item.getUnassignedCount() > 0)
                .toList();
            // 重新抽取可见员工主键，保证排班格子与左侧固定列一致。
            Set<Long> visibleEmployeeIds = employeeRows.stream().map(AttendanceScheduleOut.ScheduleEmployeeRowOut::getEmployeeId).collect(Collectors.toCollection(LinkedHashSet::new));
            // 同步裁掉隐藏员工的排班格子。
            scheduleItems = scheduleItems.stream()
                .filter(item -> visibleEmployeeIds.contains(item.getEmployeeId()))
                .toList();
        }
        // 当前月每天都要明确给前端，避免前端再自行推导日期列。
        List<LocalDate> dates = buildDates(startDate, endDate);
        // 右侧模板面板直接复用第一阶段模板列表，减少额外接口往返。
        List<AttendanceOut.ShiftTemplateOut> shiftTemplates = attendanceShiftTemplateDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID);
        // 组装最终看板结果。
        AttendanceScheduleOut.ScheduleBoardOut boardOut = new AttendanceScheduleOut.ScheduleBoardOut();
        boardOut.setMonth(yearMonth.format(MONTH_FORMATTER));
        boardOut.setStartDate(startDate);
        boardOut.setEndDate(endDate);
        boardOut.setDates(dates);
        boardOut.setEmployeeRows(employeeRows);
        boardOut.setScheduleItems(scheduleItems);
        boardOut.setShiftTemplates(shiftTemplates);
        return boardOut;
    }

    @Override
    @Transactional
    public AttendanceScheduleOut.ScheduleItemOut createSchedule(AttendanceScheduleIn.ScheduleSaveIn saveIn) {
        // 保存前先校验单日排班是否完整，避免空模板或空日期直接入库。
        validateScheduleSaveIn(saveIn);
        // 员工必须真实存在，否则排班会指向无效对象。
        AttendanceOut.EmployeeOut employeeOut = requireEmployee(saveIn.getEmployeeId());
        // 模板必须真实存在，否则后续打卡和展示都无法还原班次含义。
        AttendanceOut.ShiftTemplateOut shiftTemplateOut = requireShiftTemplate(saveIn.getShiftTemplateId());
        // 同一个员工同一天只能有一条排班记录，所以新增前先看是否已经存在。
        AttendanceScheduleOut.ScheduleItemOut existing = attendanceScheduleDao.selectByEmployeeAndDate(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            saveIn.getEmployeeId(),
            saveIn.getWorkDate()
        );
        // 已存在时直接走覆盖更新，让前端点击格子替换班次时不需要再区分新增或修改接口。
        if (existing != null) {
            return updateSchedule(existing.getId(), saveIn);
        }
        // 把模板时间展开到具体日期，给后续打卡匹配保留明确时间窗。
        ScheduleRuntime scheduleRuntime = buildRuntime(saveIn.getWorkDate(), shiftTemplateOut, saveIn.getRemark());
        // 写入第二阶段的计划事实，让日历格子能够立即看到结果。
        attendanceScheduleDao.insert(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeOut.getId(),
            saveIn.getWorkDate(),
            shiftTemplateOut.getId(),
            scheduleRuntime.scheduledStartTime(),
            scheduleRuntime.scheduledEndTime(),
            scheduleRuntime.scheduledBreakMinutes(),
            scheduleRuntime.workDayType(),
            scheduleRuntime.remark()
        );
        // 重新按员工和日期读取刚写入的排班，确保返回结构与列表查询一致。
        return attendanceScheduleDao.selectByEmployeeAndDate(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeOut.getId(),
            saveIn.getWorkDate()
        );
    }

    @Override
    @Transactional
    public AttendanceScheduleOut.ScheduleItemOut updateSchedule(Long id, AttendanceScheduleIn.ScheduleSaveIn saveIn) {
        // 修改前先校验主键和新值。
        validateId(id);
        validateScheduleSaveIn(saveIn);
        // 先确认旧排班存在，避免修改不存在的数据。
        AttendanceScheduleOut.ScheduleItemOut existing = requireSchedule(id);
        // 再校验新员工和新模板，确保覆盖后的记录仍然有效。
        requireEmployee(saveIn.getEmployeeId());
        AttendanceOut.ShiftTemplateOut shiftTemplateOut = requireShiftTemplate(saveIn.getShiftTemplateId());
        // 如果当前排班换了员工或日期，需要先拦住唯一索引冲突。
        AttendanceScheduleOut.ScheduleItemOut duplicate = attendanceScheduleDao.selectByEmployeeAndDate(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            saveIn.getEmployeeId(),
            saveIn.getWorkDate()
        );
        if (duplicate != null && !Objects.equals(duplicate.getId(), existing.getId())) {
            throw new IllegalArgumentException("该员工在当前日期已存在排班");
        }
        // 根据新模板重新展开时间，保证替换班次后时间窗同步刷新。
        ScheduleRuntime scheduleRuntime = buildRuntime(saveIn.getWorkDate(), shiftTemplateOut, saveIn.getRemark());
        // 如果员工或日期都没有变化，直接原地更新即可。
        if (Objects.equals(existing.getEmployeeId(), saveIn.getEmployeeId()) && Objects.equals(existing.getWorkDate(), saveIn.getWorkDate())) {
            attendanceScheduleDao.updateById(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                id,
                shiftTemplateOut.getId(),
                scheduleRuntime.scheduledStartTime(),
                scheduleRuntime.scheduledEndTime(),
                scheduleRuntime.scheduledBreakMinutes(),
                scheduleRuntime.workDayType(),
                scheduleRuntime.remark()
            );
            return requireSchedule(id);
        }
        // 员工或日期发生变化时，删除旧记录并重建新记录，避免和唯一键缠绕。
        attendanceScheduleDao.deleteById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        attendanceScheduleDao.insert(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            saveIn.getEmployeeId(),
            saveIn.getWorkDate(),
            shiftTemplateOut.getId(),
            scheduleRuntime.scheduledStartTime(),
            scheduleRuntime.scheduledEndTime(),
            scheduleRuntime.scheduledBreakMinutes(),
            scheduleRuntime.workDayType(),
            scheduleRuntime.remark()
        );
        return attendanceScheduleDao.selectByEmployeeAndDate(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            saveIn.getEmployeeId(),
            saveIn.getWorkDate()
        );
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        // 删除时先确认目标存在，避免前端误删时静默成功。
        validateId(id);
        requireSchedule(id);
        attendanceScheduleDao.deleteById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
    }

    @Override
    @Transactional
    public AttendanceScheduleOut.ScheduleBatchResultOut batchAssignSchedules(AttendanceScheduleIn.ScheduleBatchAssignIn saveIn) {
        // 批量排班必须先确认员工列表、日期区间和模板都齐全。
        validateBatchAssign(saveIn);
        // 模板存在后才能展开时间和工作日类型。
        AttendanceOut.ShiftTemplateOut shiftTemplateOut = requireShiftTemplate(saveIn.getShiftTemplateId());
        // 结果对象用于把创建、覆盖和跳过的数量清楚反馈给页面。
        AttendanceScheduleOut.ScheduleBatchResultOut resultOut = new AttendanceScheduleOut.ScheduleBatchResultOut();
        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        // 先去重员工列表，避免同一人被重复写入同一天。
        List<Long> employeeIds = saveIn.getEmployeeIds().stream().filter(Objects::nonNull).distinct().toList();
        // 逐员工逐日期展开批量排班，逻辑保持直白可控，方便后续扩展预览。
        for (Long employeeId : employeeIds) {
            requireEmployee(employeeId);
            for (LocalDate workDate : buildDates(saveIn.getStartDate(), saveIn.getEndDate())) {
                AttendanceScheduleOut.ScheduleItemOut existing = attendanceScheduleDao.selectByEmployeeAndDate(
                    AttendanceTenantContext.DEFAULT_TENANT_ID,
                    employeeId,
                    workDate
                );
                // 已有排班且明确要求跳过时，直接累计跳过数量。
                if (existing != null && Boolean.TRUE.equals(saveIn.getSkipExisting())) {
                    skippedCount += 1;
                    continue;
                }
                // 已有排班但未授权覆盖时，也按跳过处理，避免后台误改现有安排。
                if (existing != null && !Boolean.TRUE.equals(saveIn.getOverwriteExisting())) {
                    skippedCount += 1;
                    continue;
                }
                ScheduleRuntime scheduleRuntime = buildRuntime(workDate, shiftTemplateOut, saveIn.getRemark());
                if (existing == null) {
                    attendanceScheduleDao.insert(
                        AttendanceTenantContext.DEFAULT_TENANT_ID,
                        employeeId,
                        workDate,
                        shiftTemplateOut.getId(),
                        scheduleRuntime.scheduledStartTime(),
                        scheduleRuntime.scheduledEndTime(),
                        scheduleRuntime.scheduledBreakMinutes(),
                        scheduleRuntime.workDayType(),
                        scheduleRuntime.remark()
                    );
                    createdCount += 1;
                } else {
                    attendanceScheduleDao.updateById(
                        AttendanceTenantContext.DEFAULT_TENANT_ID,
                        existing.getId(),
                        shiftTemplateOut.getId(),
                        scheduleRuntime.scheduledStartTime(),
                        scheduleRuntime.scheduledEndTime(),
                        scheduleRuntime.scheduledBreakMinutes(),
                        scheduleRuntime.workDayType(),
                        scheduleRuntime.remark()
                    );
                    updatedCount += 1;
                }
            }
        }
        resultOut.setCreatedCount(createdCount);
        resultOut.setUpdatedCount(updatedCount);
        resultOut.setSkippedCount(skippedCount);
        resultOut.setAffectedEmployeeCount(employeeIds.size());
        resultOut.setAffectedDateCount(buildDates(saveIn.getStartDate(), saveIn.getEndDate()).size());
        resultOut.setMessage("批量排班已处理");
        return resultOut;
    }

    @Override
    @Transactional
    public AttendanceScheduleOut.ScheduleBatchResultOut copyLastWeek(AttendanceScheduleIn.ScheduleCopyIn saveIn) {
        // 复制上周本质上是把目标区间逐天映射到前 7 天的来源区间。
        return copySchedules(saveIn, 7, "已复制上周排班");
    }

    @Override
    @Transactional
    public AttendanceScheduleOut.ScheduleBatchResultOut copyLastMonth(AttendanceScheduleIn.ScheduleCopyIn saveIn) {
        // 复制上月按来源日期减一月取样，让整月复制更符合管理员直觉。
        return copySchedulesByMonth(saveIn);
    }

    @Override
    @Transactional
    public AttendanceScheduleOut.ScheduleBatchResultOut clearSchedules(AttendanceScheduleIn.ScheduleClearRangeIn saveIn) {
        // 清空操作必须明确限定员工范围和日期区间，避免误删整月。
        validateClearRange(saveIn);
        List<Long> employeeIds = saveIn.getEmployeeIds().stream().filter(Objects::nonNull).distinct().toList();
        attendanceScheduleDao.deleteRange(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            employeeIds,
            saveIn.getStartDate(),
            saveIn.getEndDate()
        );
        AttendanceScheduleOut.ScheduleBatchResultOut resultOut = new AttendanceScheduleOut.ScheduleBatchResultOut();
        resultOut.setCreatedCount(0);
        resultOut.setUpdatedCount(0);
        resultOut.setSkippedCount(0);
        resultOut.setAffectedEmployeeCount(employeeIds.size());
        resultOut.setAffectedDateCount(buildDates(saveIn.getStartDate(), saveIn.getEndDate()).size());
        resultOut.setMessage("排班区间已清空");
        return resultOut;
    }

    @Override
    public List<AttendanceScheduleOut.ScheduleUnassignedOut> checkUnassignedSchedules(AttendanceScheduleIn.ScheduleBoardQueryIn queryIn) {
        // 未排班检查与看板共享同一套员工范围和月份边界，避免两个结果打架。
        AttendanceScheduleOut.ScheduleBoardOut boardOut = getScheduleBoard(queryIn);
        // 先按员工和日期索引出已有排班，便于逐行找缺口。
        Map<Long, Set<LocalDate>> assignedDateMap = buildAssignedDateMap(boardOut.getScheduleItems());
        List<AttendanceScheduleOut.ScheduleUnassignedOut> resultList = new ArrayList<>();
        for (AttendanceScheduleOut.ScheduleEmployeeRowOut employeeRow : boardOut.getEmployeeRows()) {
            List<LocalDate> missingDates = new ArrayList<>();
            Set<LocalDate> assignedDates = assignedDateMap.getOrDefault(employeeRow.getEmployeeId(), Collections.emptySet());
            for (LocalDate workDate : boardOut.getDates()) {
                if (!assignedDates.contains(workDate)) {
                    missingDates.add(workDate);
                }
            }
            if (missingDates.isEmpty()) {
                continue;
            }
            AttendanceScheduleOut.ScheduleUnassignedOut item = new AttendanceScheduleOut.ScheduleUnassignedOut();
            item.setEmployeeId(employeeRow.getEmployeeId());
            item.setEmployeeNo(employeeRow.getEmployeeNo());
            item.setEmployeeName(employeeRow.getEmployeeName());
            item.setDepartmentName(employeeRow.getDepartmentName());
            item.setWorkplaceName(employeeRow.getWorkplaceName());
            item.setUnassignedCount(missingDates.size());
            item.setMissingDates(missingDates);
            resultList.add(item);
        }
        return resultList;
    }

    @Override
    public AttendanceOut.CsvExportOut exportSchedules(AttendanceScheduleIn.ScheduleBoardQueryIn queryIn) {
        // 导出直接复用当前看板筛选，保证下载结果和页面当前视图一致。
        AttendanceScheduleOut.ScheduleBoardOut boardOut = getScheduleBoard(queryIn);
        // 先把排班按员工和日期建索引，便于输出宽表。
        Map<String, AttendanceScheduleOut.ScheduleItemOut> scheduleIndex = boardOut.getScheduleItems().stream()
            .collect(Collectors.toMap(
                item -> item.getEmployeeId() + "_" + item.getWorkDate(),
                item -> item,
                (left, right) -> left,
                LinkedHashMap::new
            ));
        StringBuilder builder = new StringBuilder();
        builder.append("employeeNo,employeeName,departmentName,workplaceName");
        for (LocalDate workDate : boardOut.getDates()) {
            builder.append(",").append(workDate);
        }
        builder.append("\n");
        for (AttendanceScheduleOut.ScheduleEmployeeRowOut employeeRow : boardOut.getEmployeeRows()) {
            builder.append(csvCell(employeeRow.getEmployeeNo())).append(",")
                .append(csvCell(employeeRow.getEmployeeName())).append(",")
                .append(csvCell(employeeRow.getDepartmentName())).append(",")
                .append(csvCell(employeeRow.getWorkplaceName()));
            for (LocalDate workDate : boardOut.getDates()) {
                AttendanceScheduleOut.ScheduleItemOut scheduleItemOut = scheduleIndex.get(employeeRow.getEmployeeId() + "_" + workDate);
                builder.append(",").append(csvCell(scheduleItemOut == null ? "" : scheduleItemOut.getTemplateName()));
            }
            builder.append("\n");
        }
        AttendanceOut.CsvExportOut exportOut = new AttendanceOut.CsvExportOut();
        exportOut.setFileName("attendance_schedule_" + boardOut.getMonth() + ".csv");
        exportOut.setContent(builder.toString());
        return exportOut;
    }

    private AttendanceScheduleOut.ScheduleBatchResultOut copySchedules(AttendanceScheduleIn.ScheduleCopyIn saveIn, int minusDays, String message) {
        validateCopyIn(saveIn);
        List<Long> employeeIds = distinctEmployeeIds(saveIn.getEmployeeIds());
        List<LocalDate> targetDates = buildDates(saveIn.getStartDate(), saveIn.getEndDate());
        List<AttendanceScheduleOut.ScheduleItemOut> sourceItems = attendanceScheduleDao.selectScheduleItems(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            saveIn.getStartDate().minusDays(minusDays),
            saveIn.getEndDate().minusDays(minusDays),
            employeeIds
        );
        Map<String, AttendanceScheduleOut.ScheduleItemOut> sourceIndex = sourceItems.stream()
            .collect(Collectors.toMap(
                item -> item.getEmployeeId() + "_" + item.getWorkDate().plusDays(minusDays),
                item -> item,
                (left, right) -> left,
                LinkedHashMap::new
            ));
        return applyCopiedSchedules(employeeIds, targetDates, sourceIndex, Boolean.TRUE.equals(saveIn.getOverwriteExisting()), message);
    }

    private AttendanceScheduleOut.ScheduleBatchResultOut copySchedulesByMonth(AttendanceScheduleIn.ScheduleCopyIn saveIn) {
        validateCopyIn(saveIn);
        List<Long> employeeIds = distinctEmployeeIds(saveIn.getEmployeeIds());
        List<LocalDate> targetDates = buildDates(saveIn.getStartDate(), saveIn.getEndDate());
        // 先按目标区间所映射的上一月自然区间取源数据，让月复制保持日号对齐。
        List<AttendanceScheduleOut.ScheduleItemOut> sourceItems = attendanceScheduleDao.selectScheduleItems(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            saveIn.getStartDate().minusMonths(1),
            saveIn.getEndDate().minusMonths(1),
            employeeIds
        );
        Map<String, AttendanceScheduleOut.ScheduleItemOut> sourceIndex = sourceItems.stream()
            .collect(Collectors.toMap(
                item -> item.getEmployeeId() + "_" + item.getWorkDate().plusMonths(1),
                item -> item,
                (left, right) -> left,
                LinkedHashMap::new
            ));
        return applyCopiedSchedules(employeeIds, targetDates, sourceIndex, Boolean.TRUE.equals(saveIn.getOverwriteExisting()), "已复制上月排班");
    }

    private AttendanceScheduleOut.ScheduleBatchResultOut applyCopiedSchedules(
        List<Long> employeeIds,
        List<LocalDate> targetDates,
        Map<String, AttendanceScheduleOut.ScheduleItemOut> sourceIndex,
        boolean overwriteExisting,
        String message
    ) {
        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        for (Long employeeId : employeeIds) {
            requireEmployee(employeeId);
            for (LocalDate targetDate : targetDates) {
                AttendanceScheduleOut.ScheduleItemOut sourceItem = sourceIndex.get(employeeId + "_" + targetDate);
                if (sourceItem == null) {
                    skippedCount += 1;
                    continue;
                }
                AttendanceScheduleOut.ScheduleItemOut existing = attendanceScheduleDao.selectByEmployeeAndDate(
                    AttendanceTenantContext.DEFAULT_TENANT_ID,
                    employeeId,
                    targetDate
                );
                if (existing != null && !overwriteExisting) {
                    skippedCount += 1;
                    continue;
                }
                if (existing == null) {
                    attendanceScheduleDao.insert(
                        AttendanceTenantContext.DEFAULT_TENANT_ID,
                        employeeId,
                        targetDate,
                        sourceItem.getShiftTemplateId(),
                        sourceItem.getScheduledStartTime() == null ? null : LocalDateTime.of(targetDate, sourceItem.getScheduledStartTime().toLocalTime()),
                        sourceItem.getScheduledEndTime() == null ? null : rebuildCopiedEndTime(targetDate, sourceItem),
                        sourceItem.getScheduledBreakMinutes(),
                        sourceItem.getWorkDayType(),
                        sourceItem.getRemark()
                    );
                    createdCount += 1;
                } else {
                    attendanceScheduleDao.updateById(
                        AttendanceTenantContext.DEFAULT_TENANT_ID,
                        existing.getId(),
                        sourceItem.getShiftTemplateId(),
                        sourceItem.getScheduledStartTime() == null ? null : LocalDateTime.of(targetDate, sourceItem.getScheduledStartTime().toLocalTime()),
                        sourceItem.getScheduledEndTime() == null ? null : rebuildCopiedEndTime(targetDate, sourceItem),
                        sourceItem.getScheduledBreakMinutes(),
                        sourceItem.getWorkDayType(),
                        sourceItem.getRemark()
                    );
                    updatedCount += 1;
                }
            }
        }
        AttendanceScheduleOut.ScheduleBatchResultOut resultOut = new AttendanceScheduleOut.ScheduleBatchResultOut();
        resultOut.setCreatedCount(createdCount);
        resultOut.setUpdatedCount(updatedCount);
        resultOut.setSkippedCount(skippedCount);
        resultOut.setAffectedEmployeeCount(employeeIds.size());
        resultOut.setAffectedDateCount(targetDates.size());
        resultOut.setMessage(message);
        return resultOut;
    }

    private LocalDateTime rebuildCopiedEndTime(LocalDate targetDate, AttendanceScheduleOut.ScheduleItemOut sourceItem) {
        if (sourceItem.getScheduledEndTime() == null) {
            return null;
        }
        LocalDate copiedDate = targetDate;
        if (Boolean.TRUE.equals(sourceItem.getCrossDay())) {
            copiedDate = targetDate.plusDays(1);
        }
        return LocalDateTime.of(copiedDate, sourceItem.getScheduledEndTime().toLocalTime());
    }

    private List<AttendanceOut.EmployeeOut> listScheduleEmployees(AttendanceScheduleIn.ScheduleBoardQueryIn queryIn) {
        // 先把排班看板筛选转换成现有员工查询对象，尽量复用第一阶段员工查询能力。
        AttendanceIn.EmployeeQueryIn employeeQueryIn = new AttendanceIn.EmployeeQueryIn();
        employeeQueryIn.setKeyword(queryIn == null ? null : queryIn.getEmployeeKeyword());
        employeeQueryIn.setDepartmentId(queryIn == null ? null : queryIn.getDepartmentId());
        employeeQueryIn.setStatus("ACTIVE");
        List<AttendanceOut.EmployeeOut> employeeList = attendanceEmployeeDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeQueryIn);
        if (queryIn == null || queryIn.getWorkplaceId() == null) {
            return employeeList;
        }
        // 事业所过滤放在服务层完成，减少对既有员工 DAO 的侵入。
        return employeeList.stream()
            .filter(item -> Objects.equals(item.getWorkplaceId(), queryIn.getWorkplaceId()))
            .toList();
    }

    private List<AttendanceScheduleOut.ScheduleEmployeeRowOut> buildEmployeeRows(
        List<AttendanceOut.EmployeeOut> employeeList,
        Map<Long, Set<LocalDate>> assignedDateMap,
        LocalDate startDate,
        LocalDate endDate
    ) {
        List<AttendanceScheduleOut.ScheduleEmployeeRowOut> rowList = new ArrayList<>();
        int totalDays = buildDates(startDate, endDate).size();
        for (AttendanceOut.EmployeeOut employeeOut : employeeList) {
            AttendanceScheduleOut.ScheduleEmployeeRowOut rowOut = new AttendanceScheduleOut.ScheduleEmployeeRowOut();
            rowOut.setEmployeeId(employeeOut.getId());
            rowOut.setEmployeeNo(employeeOut.getEmployeeNo());
            rowOut.setEmployeeName(employeeOut.getEmployeeName());
            rowOut.setEmployeeNameKana(employeeOut.getEmployeeNameKana());
            rowOut.setDepartmentName(employeeOut.getDepartmentName());
            rowOut.setWorkplaceName(employeeOut.getWorkplaceName());
            rowOut.setUnassignedCount(totalDays - assignedDateMap.getOrDefault(employeeOut.getId(), Collections.emptySet()).size());
            rowList.add(rowOut);
        }
        return rowList;
    }

    private Map<Long, Set<LocalDate>> buildAssignedDateMap(List<AttendanceScheduleOut.ScheduleItemOut> scheduleItems) {
        Map<Long, Set<LocalDate>> assignedDateMap = new LinkedHashMap<>();
        for (AttendanceScheduleOut.ScheduleItemOut scheduleItemOut : scheduleItems) {
            assignedDateMap.computeIfAbsent(scheduleItemOut.getEmployeeId(), key -> new LinkedHashSet<>()).add(scheduleItemOut.getWorkDate());
        }
        return assignedDateMap;
    }

    private List<LocalDate> buildDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            dates.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return dates;
    }

    private ScheduleRuntime buildRuntime(LocalDate workDate, AttendanceOut.ShiftTemplateOut shiftTemplateOut, String remark) {
        // 休息、有休这类无时间模板允许只写工作日类型，不强制塞时间。
        LocalDateTime scheduledStartTime = null;
        LocalDateTime scheduledEndTime = null;
        if (StringUtils.hasText(shiftTemplateOut.getStartTime())) {
            scheduledStartTime = LocalDateTime.of(workDate, LocalTime.parse(shiftTemplateOut.getStartTime()));
        }
        if (StringUtils.hasText(shiftTemplateOut.getEndTime())) {
            LocalDate endDate = Boolean.TRUE.equals(shiftTemplateOut.getCrossDay()) ? workDate.plusDays(1) : workDate;
            scheduledEndTime = LocalDateTime.of(endDate, LocalTime.parse(shiftTemplateOut.getEndTime()));
        }
        return new ScheduleRuntime(
            scheduledStartTime,
            scheduledEndTime,
            shiftTemplateOut.getScheduledBreakMinutes() == null ? 0 : shiftTemplateOut.getScheduledBreakMinutes(),
            resolveWorkDayType(shiftTemplateOut.getShiftType()),
            trimToNull(remark)
        );
    }

    private String resolveWorkDayType(String shiftType) {
        if (!StringUtils.hasText(shiftType)) {
            return "WORKDAY";
        }
        return switch (shiftType.trim()) {
            case "REST" -> "REST";
            case "PAID_LEAVE" -> "PAID_LEAVE";
            case "SPECIAL_LEAVE" -> "SPECIAL_LEAVE";
            case "ABSENCE" -> "ABSENCE";
            default -> "WORKDAY";
        };
    }

    private AttendanceOut.EmployeeOut requireEmployee(Long employeeId) {
        validateId(employeeId);
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeId);
        if (employeeOut == null) {
            throw new IllegalArgumentException("员工不存在，id=" + employeeId);
        }
        return employeeOut;
    }

    private AttendanceOut.ShiftTemplateOut requireShiftTemplate(Long shiftTemplateId) {
        validateId(shiftTemplateId);
        AttendanceOut.ShiftTemplateOut shiftTemplateOut = attendanceShiftTemplateDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, shiftTemplateId);
        if (shiftTemplateOut == null) {
            throw new IllegalArgumentException("班次模板不存在，id=" + shiftTemplateId);
        }
        return shiftTemplateOut;
    }

    private AttendanceScheduleOut.ScheduleItemOut requireSchedule(Long id) {
        AttendanceScheduleOut.ScheduleItemOut scheduleItemOut = attendanceScheduleDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        if (scheduleItemOut == null) {
            throw new IllegalArgumentException("排班不存在，id=" + id);
        }
        return scheduleItemOut;
    }

    private YearMonth requireMonth(String monthText) {
        if (!StringUtils.hasText(monthText)) {
            throw new IllegalArgumentException("month 不能为空");
        }
        return YearMonth.parse(monthText.trim(), MONTH_FORMATTER);
    }

    private void validateScheduleSaveIn(AttendanceScheduleIn.ScheduleSaveIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("scheduleSaveIn 不能为空");
        }
        validateId(saveIn.getEmployeeId());
        validateId(saveIn.getShiftTemplateId());
        if (saveIn.getWorkDate() == null) {
            throw new IllegalArgumentException("workDate 不能为空");
        }
    }

    private void validateBatchAssign(AttendanceScheduleIn.ScheduleBatchAssignIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("scheduleBatchAssignIn 不能为空");
        }
        if (saveIn.getEmployeeIds() == null || saveIn.getEmployeeIds().isEmpty()) {
            throw new IllegalArgumentException("employeeIds 不能为空");
        }
        validateId(saveIn.getShiftTemplateId());
        validateDateRange(saveIn.getStartDate(), saveIn.getEndDate());
    }

    private void validateCopyIn(AttendanceScheduleIn.ScheduleCopyIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("scheduleCopyIn 不能为空");
        }
        if (saveIn.getEmployeeIds() == null || saveIn.getEmployeeIds().isEmpty()) {
            throw new IllegalArgumentException("employeeIds 不能为空");
        }
        validateDateRange(saveIn.getStartDate(), saveIn.getEndDate());
    }

    private void validateClearRange(AttendanceScheduleIn.ScheduleClearRangeIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("scheduleClearRangeIn 不能为空");
        }
        if (saveIn.getEmployeeIds() == null || saveIn.getEmployeeIds().isEmpty()) {
            throw new IllegalArgumentException("employeeIds 不能为空");
        }
        validateDateRange(saveIn.getStartDate(), saveIn.getEndDate());
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("日期区间不能为空");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id 必须大于 0");
        }
    }

    private List<Long> distinctEmployeeIds(List<Long> employeeIds) {
        return employeeIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String csvCell(String value) {
        String safeValue = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safeValue + "\"";
    }

    // 定义 排班运行态 记录结构，承接当前业务动作。
    private record ScheduleRuntime(
        LocalDateTime scheduledStartTime,
        LocalDateTime scheduledEndTime,
        Integer scheduledBreakMinutes,
        String workDayType,
        String remark
    ) {
    }
}
