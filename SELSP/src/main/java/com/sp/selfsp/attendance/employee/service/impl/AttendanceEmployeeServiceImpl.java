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

// 把当前类注册为服务实现，负责承接业务编排。
@Service
// 定义 考勤员工服务Impl，承接当前文件对应的业务职责。
public class AttendanceEmployeeServiceImpl implements AttendanceEmployeeService {

    // 声明 考勤员工数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceEmployeeDao attendanceEmployeeDao;
    // 声明 考勤事业所数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceWorkplaceDao attendanceWorkplaceDao;
    // 声明 考勤部门数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceDepartmentDao attendanceDepartmentDao;

    // 定义 考勤员工服务Impl 业务动作，负责承接当前模块的处理流程。
    public AttendanceEmployeeServiceImpl(
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceEmployeeDao attendanceEmployeeDao,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceWorkplaceDao attendanceWorkplaceDao,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceDepartmentDao attendanceDepartmentDao
    // 执行当前业务步骤，推进本行对应的 服务impl 处理。
    ) {
        // 把外部传入结果写入 考勤员工数据访问 字段，供后续流程继续使用。
        this.attendanceEmployeeDao = attendanceEmployeeDao;
        // 把外部传入结果写入 考勤事业所数据访问 字段，供后续流程继续使用。
        this.attendanceWorkplaceDao = attendanceWorkplaceDao;
        // 把外部传入结果写入 考勤部门数据访问 字段，供后续流程继续使用。
        this.attendanceDepartmentDao = attendanceDepartmentDao;
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 定义 listEmployees 业务动作，负责承接当前模块的处理流程。
    public List<AttendanceOut.EmployeeOut> listEmployees(AttendanceIn.EmployeeQueryIn queryIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return attendanceEmployeeDao.selectList(
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            AttendanceTenantContext.DEFAULT_TENANT_ID,
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            queryIn == null ? new AttendanceIn.EmployeeQueryIn() : queryIn
        );
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 新增员工 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.EmployeeOut createEmployee(AttendanceIn.EmployeeSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateEmployee(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingWorkplace(saveIn.getWorkplaceId());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingDepartment(saveIn.getDepartmentId());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        normalizeEmployee(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        ensureEmployeeNoUnique(saveIn.getEmployeeNo(), null);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceEmployeeDao.insert(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectByEmployeeNo(AttendanceTenantContext.DEFAULT_TENANT_ID, saveIn.getEmployeeNo());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        upsertEmployeeWorkRule(employeeOut.getId(), employeeOut.getEmploymentType(), employeeOut.getHireDate());
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return requireExistingEmployee(employeeOut.getId());
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 更新员工 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.EmployeeOut updateEmployee(Long id, AttendanceIn.EmployeeSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateId(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateEmployee(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingEmployee(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingWorkplace(saveIn.getWorkplaceId());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingDepartment(saveIn.getDepartmentId());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        normalizeEmployee(saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        ensureEmployeeNoUnique(saveIn.getEmployeeNo(), id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceEmployeeDao.updateById(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.EmployeeOut employeeOut = requireExistingEmployee(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        upsertEmployeeWorkRule(id, employeeOut.getEmploymentType(), employeeOut.getHireDate());
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return employeeOut;
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 删除员工 业务动作，负责承接当前模块的处理流程。
    public void deleteEmployee(Long id) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateId(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingEmployee(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceEmployeeDao.deleteWorkRuleByEmployeeId(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceEmployeeDao.deleteExternalMappingByEmployeeId(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        attendanceEmployeeDao.deleteById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 绑定外部系统映射 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.EmployeeOut bindExternalMapping(Long id, AttendanceIn.ExternalMappingSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        validateId(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireExistingEmployee(id);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn == null ? null : saveIn.getSourceSystem(), "sourceSystem 不能为空");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getExternalEmployeeId(), "externalEmployeeId 不能为空");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getExternalEmployeeNo(), "externalEmployeeNo 不能为空");
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (!StringUtils.hasText(saveIn.getStatus())) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            saveIn.setStatus("ACTIVE");
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if ((attendanceEmployeeDao.countExternalMappingByEmployeeId(AttendanceTenantContext.DEFAULT_TENANT_ID, id)) > 0) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            attendanceEmployeeDao.updateExternalMapping(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        } else {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            attendanceEmployeeDao.insertExternalMapping(AttendanceTenantContext.DEFAULT_TENANT_ID, id, saveIn);
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return requireExistingEmployee(id);
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 声明 Transactional 注解，让当前代码接入既定框架能力。
    @Transactional
    // 定义 导入Employees 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.EmployeeImportResultOut importEmployees(AttendanceIn.EmployeeImportIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn == null ? null : saveIn.getCsvText(), "csvText 不能为空");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        String[] lines = saveIn.getCsvText().trim().split("\\r?\\n");
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (lines.length < 2) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("CSV 至少需要表头和一行数据");
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        List<String> errors = new ArrayList<>();
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        int successCount = 0;
        // 遍历当前业务集合，逐条完成对应的数据处理动作。
        for (int index = 1; index < lines.length; index++) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            String line = lines[index].trim();
            // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
            if (line.isEmpty()) {
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                continue;
            }
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            String[] fields = line.split(",", -1);
            // 进入受控处理区块，统一兜底当前业务动作的异常路径。
            try {
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                createEmployee(buildEmployeeFromCsv(fields));
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                successCount++;
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            } catch (IllegalArgumentException error) {
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                errors.add("第 " + (index + 1) + " 行：" + error.getMessage());
            }
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.EmployeeImportResultOut resultOut = new AttendanceOut.EmployeeImportResultOut();
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        resultOut.setSuccessCount(successCount);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        resultOut.setFailedCount(errors.size());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        resultOut.setErrors(errors);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return resultOut;
    }

    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 定义 导出Employees 业务动作，负责承接当前模块的处理流程。
    public AttendanceOut.CsvExportOut exportEmployees() {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        List<AttendanceOut.EmployeeOut> employees = listEmployees(new AttendanceIn.EmployeeQueryIn());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        StringBuilder builder = new StringBuilder();
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        builder.append("employeeNo,employeeName,employeeNameKana,employmentType,workplaceName,departmentName,status,externalEmployeeId\n");
        // 遍历当前业务集合，逐条完成对应的数据处理动作。
        for (AttendanceOut.EmployeeOut employee : employees) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            builder.append(csvCell(employee.getEmployeeNo())).append(",")
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                .append(csvCell(employee.getEmployeeName())).append(",")
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                .append(csvCell(employee.getEmployeeNameKana())).append(",")
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                .append(csvCell(employee.getEmploymentType())).append(",")
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                .append(csvCell(employee.getWorkplaceName())).append(",")
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                .append(csvCell(employee.getDepartmentName())).append(",")
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                .append(csvCell(employee.getStatus())).append(",")
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                .append(csvCell(employee.getExternalEmployeeId())).append("\n");
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.CsvExportOut exportOut = new AttendanceOut.CsvExportOut();
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        exportOut.setFileName("attendance-employees-phase1.csv");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        exportOut.setContent(builder.toString());
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return exportOut;
    }

    // 定义 upsert员工工时规则 业务动作，负责承接当前模块的处理流程。
    private void upsertEmployeeWorkRule(Long employeeId, String employmentType, LocalDate hireDate) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        int dailyMinutes = "PART_TIME".equals(employmentType) || "ARBEIT".equals(employmentType) ? 300 : 480;
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        int weeklyMinutes = "PART_TIME".equals(employmentType) || "ARBEIT".equals(employmentType) ? 1500 : 2400;
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        LocalDate effectiveStartDate = hireDate == null ? LocalDate.now() : hireDate;
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if ((attendanceEmployeeDao.countWorkRuleByEmployeeId(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeId)) > 0) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            attendanceEmployeeDao.updateWorkRule(
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                employeeId,
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                employmentType == null ? "STANDARD" : employmentType,
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                dailyMinutes,
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                weeklyMinutes,
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                effectiveStartDate
            );
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        } else {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            attendanceEmployeeDao.insertWorkRule(
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                AttendanceTenantContext.DEFAULT_TENANT_ID,
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                employeeId,
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                employmentType == null ? "STANDARD" : employmentType,
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                dailyMinutes,
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                weeklyMinutes,
                // 执行当前业务步骤，推进本行对应的 服务impl 处理。
                effectiveStartDate
            );
        }
    }

    // 定义 validate员工 业务动作，负责承接当前模块的处理流程。
    private void validateEmployee(AttendanceIn.EmployeeSaveIn saveIn) {
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("employeeSaveIn 不能为空");
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getEmployeeNo(), "employeeNo 不能为空");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getEmployeeName(), "employeeName 不能为空");
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        requireText(saveIn.getEmploymentType(), "employmentType 不能为空");
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn.getWorkplaceId() == null || saveIn.getWorkplaceId() <= 0) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("workplaceId 不能为空");
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (saveIn.getDepartmentId() == null || saveIn.getDepartmentId() <= 0) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("departmentId 不能为空");
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (!StringUtils.hasText(saveIn.getStatus())) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            saveIn.setStatus("ACTIVE");
        }
    }

    // 定义 normalize员工 业务动作，负责承接当前模块的处理流程。
    private void normalizeEmployee(AttendanceIn.EmployeeSaveIn saveIn) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEmployeeNo(saveIn.getEmployeeNo().trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEmployeeName(saveIn.getEmployeeName().trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEmployeeNameKana(trimToNull(saveIn.getEmployeeNameKana()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setGender(trimToNull(saveIn.getGender()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEmploymentType(saveIn.getEmploymentType().trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEmail(trimToNull(saveIn.getEmail()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setPhone(trimToNull(saveIn.getPhone()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setStatus(saveIn.getStatus().trim());
    }

    // 定义 ensure员工NoUnique 业务动作，负责承接当前模块的处理流程。
    private void ensureEmployeeNoUnique(String employeeNo, Long currentId) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectByEmployeeNo(AttendanceTenantContext.DEFAULT_TENANT_ID, employeeNo.trim());
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (employeeOut == null) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            return;
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (currentId != null && currentId.equals(employeeOut.getId())) {
            // 执行当前业务步骤，推进本行对应的 服务impl 处理。
            return;
        }
        // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
        throw new IllegalArgumentException("employeeNo 已存在");
    }

    // 定义 requireExisting员工 业务动作，负责承接当前模块的处理流程。
    private AttendanceOut.EmployeeOut requireExistingEmployee(Long id) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, id);
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (employeeOut == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("员工不存在，id=" + id);
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return employeeOut;
    }

    // 定义 requireExisting事业所 业务动作，负责承接当前模块的处理流程。
    private void requireExistingWorkplace(Long workplaceId) {
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (attendanceWorkplaceDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, workplaceId) == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("事业所不存在，id=" + workplaceId);
        }
    }

    // 定义 requireExisting部门 业务动作，负责承接当前模块的处理流程。
    private void requireExistingDepartment(Long departmentId) {
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (attendanceDepartmentDao.selectById(AttendanceTenantContext.DEFAULT_TENANT_ID, departmentId) == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("部门不存在，id=" + departmentId);
        }
    }

    // 定义 build员工FromCsv 业务动作，负责承接当前模块的处理流程。
    private AttendanceIn.EmployeeSaveIn buildEmployeeFromCsv(String[] fields) {
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (fields.length < 9) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("CSV 列数不足");
        }
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceIn.EmployeeSaveIn saveIn = new AttendanceIn.EmployeeSaveIn();
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEmployeeNo(fields[0].trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEmployeeName(fields[1].trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEmployeeNameKana(fields[2].trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEmploymentType(fields[3].trim());
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setWorkplaceId(resolveWorkplaceIdByCode(fields[4].trim()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setDepartmentId(resolveDepartmentIdByCode(fields[5].trim()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setHireDate(StringUtils.hasText(fields[6]) ? LocalDate.parse(fields[6].trim()) : null);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setEmail(trimToNull(fields[7]));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setPhone(trimToNull(fields[8]));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        saveIn.setStatus("ACTIVE");
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return saveIn;
    }

    // 定义 resolve事业所IdBy编码 业务动作，负责承接当前模块的处理流程。
    private Long resolveWorkplaceIdByCode(String workplaceCode) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.WorkplaceOut workplaceOut = attendanceWorkplaceDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, workplaceCode);
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (workplaceOut == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("workplaceCode 不存在: " + workplaceCode);
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return workplaceOut.getId();
    }

    // 定义 resolve部门IdBy编码 业务动作，负责承接当前模块的处理流程。
    private Long resolveDepartmentIdByCode(String departmentCode) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.DepartmentOut departmentOut = attendanceDepartmentDao.selectByCode(AttendanceTenantContext.DEFAULT_TENANT_ID, departmentCode);
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (departmentOut == null) {
            // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
            throw new IllegalArgumentException("departmentCode 不存在: " + departmentCode);
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return departmentOut.getId();
    }

    // 定义 csvCell 业务动作，负责承接当前模块的处理流程。
    private String csvCell(String value) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return "\"" + safe + "\"";
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

