package com.sp.selfsp.attendance.employee.service.impl;

import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.department.dao.AttendanceDepartmentDao;
import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.employee.dao.AttendanceEmployeeDao;
import com.sp.selfsp.attendance.employee.service.AttendanceEmployeeService;
import com.sp.selfsp.attendance.workplace.dao.AttendanceWorkplaceDao;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 员工模块服务实现。
 *
 * <p>负责串联员工主数据、所属组织、工时规则和外部打卡映射这几条业务链。</p>
 */
@Service
public class AttendanceEmployeeServiceImpl implements AttendanceEmployeeService {

    // 员工主表与附属表的读写都从这里进入。
    private final AttendanceEmployeeDao attendanceEmployeeDao;
    // 员工归属事业所校验依赖这个 DAO。
    private final AttendanceWorkplaceDao attendanceWorkplaceDao;
    // 员工归属部门校验依赖这个 DAO。
    private final AttendanceDepartmentDao attendanceDepartmentDao;

    public AttendanceEmployeeServiceImpl(
        AttendanceEmployeeDao attendanceEmployeeDao,
        AttendanceWorkplaceDao attendanceWorkplaceDao,
        AttendanceDepartmentDao attendanceDepartmentDao
    ) {
        this.attendanceEmployeeDao = attendanceEmployeeDao;
        this.attendanceWorkplaceDao = attendanceWorkplaceDao;
        this.attendanceDepartmentDao = attendanceDepartmentDao;
    }

    @Override
    public List<AttendanceOut.EmployeeOut> listEmployees(AttendanceIn.EmployeeQueryIn queryIn) {
        // 查询入参允许为空，空时等价于“不过滤任何条件”的员工全量列表。
        return attendanceEmployeeDao.selectList(
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            queryIn == null ? new AttendanceIn.EmployeeQueryIn() : queryIn
        );
    }

    @Override
    @Transactional
    public AttendanceOut.EmployeeOut createEmployee(AttendanceIn.EmployeeSaveIn saveIn) {
        // 先做必填校验，避免脏数据进入主表和附属规则表。
        validateEmployee(saveIn);
        // 员工必须挂在真实存在的事业所下，否则后续排班地点无法确定。
        requireExistingWorkplace(saveIn.getWorkplaceId());
        // 员工必须挂在真实存在的部门下，否则组织筛选和报表统计会失真。
        requireExistingDepartment(saveIn.getDepartmentId());
        // 入库前统一去空格和补默认值，保证后续唯一键校验和列表展示稳定。
        normalizeEmployee(saveIn);
        // 员工编号在租户内必须唯一，避免导入或手工录入造成重复员工。
        ensureEmployeeNoUnique(saveIn.getEmployeeNo(), null);
        // 先写入员工主表，后续工时规则和外部信息都依赖这个员工主键。
        attendanceEmployeeDao.insert(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        // 通过员工编号回读刚新增的完整员工信息，拿到数据库生成的主键和标准化后的字段。
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectByEmployeeNo(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn.getEmployeeNo());
        // 新增员工后立即补默认工时规则，避免排班和日次计算时缺少规则基线。
        upsertEmployeeWorkRule(employeeOut.getId(), employeeOut.getEmploymentType(), employeeOut.getHireDate());
        // 返回数据库中的最新员工快照，确保前端拿到完整联表字段。
        return requireExistingEmployee(employeeOut.getId());
    }

    @Override
    @Transactional
    public AttendanceOut.EmployeeOut updateEmployee(Long id, AttendanceIn.EmployeeSaveIn saveIn) {
        // 路径 id 非法时立即拦截，避免误更新租户内其他数据。
        validateId(id);
        // 更新和新增共用同一套员工表单校验规则。
        validateEmployee(saveIn);
        // 目标员工不存在时不允许继续更新，避免前端拿旧页面覆盖已删除数据。
        requireExistingEmployee(id);
        // 更新后归属事业所仍需存在，保证后续排班地点有效。
        requireExistingWorkplace(saveIn.getWorkplaceId());
        // 更新后归属部门仍需存在，保证组织统计和筛选有效。
        requireExistingDepartment(saveIn.getDepartmentId());
        // 统一整理输入值，避免因为空格导致唯一性和展示异常。
        normalizeEmployee(saveIn);
        // 更新时允许保留自己的编号，但不能撞上别人的编号。
        ensureEmployeeNoUnique(saveIn.getEmployeeNo(), id);
        // 先更新主表，再根据最新雇佣类型重算默认工时规则。
        attendanceEmployeeDao.updateById(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        AttendanceOut.EmployeeOut employeeOut = requireExistingEmployee(id);
        // 雇佣类型或入社日变化后，要同步覆盖该员工的默认工时规则。
        upsertEmployeeWorkRule(id, employeeOut.getEmploymentType(), employeeOut.getHireDate());
        return employeeOut;
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        // 删除动作只接受合法主键。
        validateId(id);
        // 删除前先确认员工存在，保证前端重复删除时能得到明确错误。
        requireExistingEmployee(id);
        // 先删工时规则，避免留下无法关联员工主表的孤儿数据。
        attendanceEmployeeDao.deleteWorkRuleByEmployeeId(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        // 再删外部打卡映射，确保第三方标识不会指向已删除员工。
        attendanceEmployeeDao.deleteExternalMappingByEmployeeId(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        // 最后删除员工主表记录。
        attendanceEmployeeDao.deleteById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
    }

    @Override
    @Transactional
    public AttendanceOut.EmployeeOut bindExternalMapping(Long id, AttendanceIn.ExternalMappingSaveIn saveIn) {
        // 绑定外部打卡系统前必须先锁定有效员工。
        validateId(id);
        requireExistingEmployee(id);
        // 外部来源系统名决定后续从哪个第三方接收打卡。
        requireText(saveIn == null ? null : saveIn.getSourceSystem(), "sourceSystem 不能为空");
        // 第三方员工 ID 是后续回写和重处理原始打卡的主定位键。
        requireText(saveIn.getExternalEmployeeId(), "externalEmployeeId 不能为空");
        // 第三方员工编号用于人工核对和导入导出展示。
        requireText(saveIn.getExternalEmployeeNo(), "externalEmployeeNo 不能为空");
        // 未显式指定状态时默认按启用处理，保证新绑定能立即参与打卡归集。
        if (!StringUtils.hasText(saveIn.getStatus())) {
            saveIn.setStatus("ACTIVE");
        }
        // 同一个员工只保留一条当前映射，已有记录时走更新，没有记录时走新增。
        if ((attendanceEmployeeDao.countExternalMappingByEmployeeId(AttendanceTenantContext.DEFAULT_TENANT_ID, id)) > 0) {
            attendanceEmployeeDao.updateExternalMapping(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        } else {
            attendanceEmployeeDao.insertExternalMapping(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        }
        // 返回联表后的员工详情，让前端立即看到绑定结果。
        return requireExistingEmployee(id);
    }

    @Override
    @Transactional
    public AttendanceOut.EmployeeImportResultOut importEmployees(AttendanceIn.EmployeeImportIn saveIn) {
        // 导入接口至少要拿到整段 CSV 文本，空文本直接视为无效请求。
        requireText(saveIn == null ? null : saveIn.getCsvText(), "csvText 不能为空");
        // 先按行拆分，后续逐行转成员工保存入参。
        String[] lines = saveIn.getCsvText().trim().split("\\r?\\n");
        // 只有表头没有数据时不进入导入流程，避免误报成功。
        if (lines.length < 2) {
            throw new IllegalArgumentException("CSV 至少需要表头和一行数据");
        }
        // 收集每一条失败行的错误消息，供前端导入结果页逐条展示。
        List<String> errors = new ArrayList<>();
        // 统计真正成功写入员工主表的记录数。
        int successCount = 0;
        // 跳过表头，从第二行开始逐条导入员工数据。
        for (int index = 1; index < lines.length; index++) {
            String line = lines[index].trim();
            // 空行直接忽略，避免用户在 CSV 尾部多敲回车造成无意义错误。
            if (line.isEmpty()) {
                continue;
            }
            // 保留空列拆分，避免尾部空字段被 split 默认丢掉。
            String[] fields = line.split(",", -1);
            // 每一行单独保护，保证坏数据不会中断整批导入。
            try {
                createEmployee(buildEmployeeFromCsv(fields));
                successCount++;
            } catch (IllegalArgumentException error) {
                // 错误行号按用户看到的 CSV 行号返回，便于直接定位原文件。
                errors.add("第 " + (index + 1) + " 行：" + error.getMessage());
            }
        }
        // 把整批导入结果组装成前端导入总结弹窗需要的返回结构。
        AttendanceOut.EmployeeImportResultOut resultOut = new AttendanceOut.EmployeeImportResultOut();
        resultOut.setSuccessCount(successCount);
        resultOut.setFailedCount(errors.size());
        resultOut.setErrors(errors);
        return resultOut;
    }

    @Override
    public AttendanceOut.CsvExportOut exportEmployees() {
        List<AttendanceOut.EmployeeOut> employees = listEmployees(new AttendanceIn.EmployeeQueryIn());
        StringBuilder builder = new StringBuilder();
        builder.append("employeeNo,employeeName,employeeNameKana,employmentType,workplaceName,departmentName,status,externalEmployeeId\n");
        for (AttendanceOut.EmployeeOut employee : employees) {
            builder.append(csvCell(employee.getEmployeeNo())).append(",")
                .append(csvCell(employee.getEmployeeName())).append(",")
                .append(csvCell(employee.getEmployeeNameKana())).append(",")
                .append(csvCell(employee.getEmploymentType())).append(",")
                .append(csvCell(employee.getWorkplaceName())).append(",")
                .append(csvCell(employee.getDepartmentName())).append(",")
                .append(csvCell(employee.getStatus())).append(",")
                .append(csvCell(employee.getExternalEmployeeId())).append("\n");
        }
        AttendanceOut.CsvExportOut exportOut = new AttendanceOut.CsvExportOut();
        exportOut.setFileName("attendance-employees-phase1.csv");
        exportOut.setContent(builder.toString());
        return exportOut;
    }

    private void upsertEmployeeWorkRule(Long employeeId, String employmentType, LocalDate hireDate) {
        int dailyMinutes = "PART_TIME".equals(employmentType) || "ARBEIT".equals(employmentType) ? 300 : 480;
        int weeklyMinutes = "PART_TIME".equals(employmentType) || "ARBEIT".equals(employmentType) ? 1500 : 2400;
        LocalDate effectiveStartDate = hireDate == null ? LocalDate.now() : hireDate;
        if ((attendanceEmployeeDao.countWorkRuleByEmployeeId(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeId)) > 0) {
            attendanceEmployeeDao.updateWorkRule(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                employeeId,
                employmentType == null ? "STANDARD" : employmentType,
                dailyMinutes,
                weeklyMinutes,
                effectiveStartDate
            );
        } else {
            attendanceEmployeeDao.insertWorkRule(
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                employeeId,
                employmentType == null ? "STANDARD" : employmentType,
                dailyMinutes,
                weeklyMinutes,
                effectiveStartDate
            );
        }
    }

    private void validateEmployee(AttendanceIn.EmployeeSaveIn saveIn) {
        if (saveIn == null) {
            throw new IllegalArgumentException("employeeSaveIn 不能为空");
        }
        requireText(saveIn.getEmployeeNo(), "employeeNo 不能为空");
        requireText(saveIn.getEmployeeName(), "employeeName 不能为空");
        requireText(saveIn.getEmploymentType(), "employmentType 不能为空");
        if (saveIn.getWorkplaceId() == null || saveIn.getWorkplaceId() <= 0) {
            throw new IllegalArgumentException("workplaceId 不能为空");
        }
        if (saveIn.getDepartmentId() == null || saveIn.getDepartmentId() <= 0) {
            throw new IllegalArgumentException("departmentId 不能为空");
        }
        if (!StringUtils.hasText(saveIn.getStatus())) {
            saveIn.setStatus("ACTIVE");
        }
    }

    private void normalizeEmployee(AttendanceIn.EmployeeSaveIn saveIn) {
        saveIn.setEmployeeNo(saveIn.getEmployeeNo().trim());
        saveIn.setEmployeeName(saveIn.getEmployeeName().trim());
        saveIn.setEmployeeNameKana(trimToNull(saveIn.getEmployeeNameKana()));
        saveIn.setGender(trimToNull(saveIn.getGender()));
        saveIn.setEmploymentType(saveIn.getEmploymentType().trim());
        saveIn.setEmail(trimToNull(saveIn.getEmail()));
        saveIn.setPhone(trimToNull(saveIn.getPhone()));
        saveIn.setStatus(saveIn.getStatus().trim());
    }

    private void ensureEmployeeNoUnique(String employeeNo, Long currentId) {
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectByEmployeeNo(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeNo.trim());
        if (employeeOut == null) {
            return;
        }
        if (currentId != null && currentId.equals(employeeOut.getId())) {
            return;
        }
        throw new IllegalArgumentException("employeeNo 已存在");
    }

    private AttendanceOut.EmployeeOut requireExistingEmployee(Long id) {
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        if (employeeOut == null) {
            throw new IllegalArgumentException("员工不存在，id=" + id);
        }
        return employeeOut;
    }

    private void requireExistingWorkplace(Long workplaceId) {
        if (attendanceWorkplaceDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, workplaceId) == null) {
            throw new IllegalArgumentException("事业所不存在，id=" + workplaceId);
        }
    }

    private void requireExistingDepartment(Long departmentId) {
        if (attendanceDepartmentDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, departmentId) == null) {
            throw new IllegalArgumentException("部门不存在，id=" + departmentId);
        }
    }

    private AttendanceIn.EmployeeSaveIn buildEmployeeFromCsv(String[] fields) {
        if (fields.length < 9) {
            throw new IllegalArgumentException("CSV 列数不足");
        }
        AttendanceIn.EmployeeSaveIn saveIn = new AttendanceIn.EmployeeSaveIn();
        saveIn.setEmployeeNo(fields[0].trim());
        saveIn.setEmployeeName(fields[1].trim());
        saveIn.setEmployeeNameKana(fields[2].trim());
        saveIn.setEmploymentType(fields[3].trim());
        saveIn.setWorkplaceId(resolveWorkplaceIdByCode(fields[4].trim()));
        saveIn.setDepartmentId(resolveDepartmentIdByCode(fields[5].trim()));
        saveIn.setHireDate(StringUtils.hasText(fields[6]) ? LocalDate.parse(fields[6].trim()) : null);
        saveIn.setEmail(trimToNull(fields[7]));
        saveIn.setPhone(trimToNull(fields[8]));
        saveIn.setStatus("ACTIVE");
        return saveIn;
    }

    private Long resolveWorkplaceIdByCode(String workplaceCode) {
        AttendanceOut.WorkplaceOut workplaceOut = attendanceWorkplaceDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, workplaceCode);
        if (workplaceOut == null) {
            throw new IllegalArgumentException("workplaceCode 不存在: " + workplaceCode);
        }
        return workplaceOut.getId();
    }

    private Long resolveDepartmentIdByCode(String departmentCode) {
        AttendanceOut.DepartmentOut departmentOut = attendanceDepartmentDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, departmentCode);
        if (departmentOut == null) {
            throw new IllegalArgumentException("departmentCode 不存在: " + departmentCode);
        }
        return departmentOut.getId();
    }

    private String csvCell(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
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

