package com.sp.selfsp.attendance.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.employee.dao.AttendanceEmployeeDao;
import com.sp.selfsp.attendance.employee.service.AttendanceEmployeeService;
import java.time.LocalDate;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.AopTestUtils;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:reset-attendance-test-data.sql")
public class AttendanceEmployeeServiceBoundaryTest {

    @Autowired
    private AttendanceEmployeeService attendanceEmployeeService;

    @Autowired
    private AttendanceEmployeeDao attendanceEmployeeDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试目的：验证shouldCreateEmployeeWithWorkRuleAndRejectDuplicateEmployeeNo场景。
     */
    @Test
    void shouldCreateEmployeeWithWorkRuleAndRejectDuplicateEmployeeNo() {
        // 先新增一个正常员工，确认服务层会同时落员工主表和默认工时规则。
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeService.createEmployee(
            employeeSaveIn("E1001", "Service Employee", 1L, 1L)
        );

        assertNotNull(employeeOut);
        assertEquals("E1001", employeeOut.getEmployeeNo());
        assertEquals(1, attendanceEmployeeDao.countWorkRuleByEmployeeId(1L, employeeOut.getId()));

        // 再用相同员工编号提交第二次，验证租户内唯一编号约束会被触发。
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.createEmployee(employeeSaveIn("E1001", "Duplicate Employee", 1L, 1L))
        );
        assertTrue(exception.getMessage().contains("employeeNo"));
    }

    /**
     * 测试目的：验证shouldInsertThenUpdateExternalMappingAndExportEmployees场景。
     */
    @Test
    void shouldInsertThenUpdateExternalMappingAndExportEmployees() {
        // 先创建一个员工，再验证外部打卡映射从首次插入到后续更新的完整链路。
        AttendanceOut.EmployeeOut employeeOut = attendanceEmployeeService.createEmployee(
            employeeSaveIn("E1002", "Mapping Employee", 1L, 1L)
        );

        AttendanceOut.EmployeeOut inserted = attendanceEmployeeService.bindExternalMapping(
            employeeOut.getId(),
            externalMappingSaveIn("KINTAI", "EXT-001", "NO-001", "")
        );
        assertEquals("EXT-001", inserted.getExternalEmployeeId());

        AttendanceOut.EmployeeOut updated = attendanceEmployeeService.bindExternalMapping(
            employeeOut.getId(),
            externalMappingSaveIn("KINTAI", "EXT-002", "NO-002", "ACTIVE")
        );
        assertEquals("EXT-002", updated.getExternalEmployeeId());
        assertEquals("NO-002", updated.getExternalEmployeeNo());

        // 导出时应把刚更新后的外部员工标识带进 CSV，保证外部映射能被运营侧复核。
        AttendanceOut.CsvExportOut exportOut = attendanceEmployeeService.exportEmployees();
        assertEquals("attendance-employees-phase1.csv", exportOut.getFileName());
        assertTrue(exportOut.getContent().contains("EXT-002"));
    }

    /**
     * 测试目的：验证shouldImportEmployeesAndCollectErrorsForBadRows场景。
     */
    @Test
    void shouldImportEmployeesAndCollectErrorsForBadRows() {
        // 同一批 CSV 同时放一条合法记录和一条坏记录，验证导入会按行累计成功与失败结果。
        String csvText = String.join("\n",
            "employeeNo,employeeName,employeeNameKana,employmentType,workplaceCode,departmentCode,hireDate,email,phone",
            "E1003,Import Success,Import Kana,FULL_TIME,TKY-HQ,ADMIN,2026-05-01,success@example.jp,090-3000-0000",
            "E1004,Import Failure,Import Kana,FULL_TIME,UNKNOWN,ADMIN,2026-05-01,failure@example.jp,090-4000-0000"
        );

        AttendanceOut.EmployeeImportResultOut resultOut = attendanceEmployeeService.importEmployees(employeeImportIn(csvText));
        assertEquals(1, resultOut.getSuccessCount());
        assertEquals(1, resultOut.getFailedCount());
        assertTrue(resultOut.getErrors().get(0).contains("UNKNOWN"));

        // 最终列表只应新增成功那一条员工，坏行不能污染主表。
        java.util.List<AttendanceOut.EmployeeOut> employees = attendanceEmployeeService.listEmployees(new AttendanceIn.EmployeeQueryIn());
        assertEquals(2, employees.size());
    }

    /**
     * 测试目的：验证shouldListEmployeesWithNullQueryAndNormalizeBlankOptionalFields场景。
     */
    @Test
    void shouldListEmployeesWithNullQueryAndNormalizeBlankOptionalFields() {
        AttendanceIn.EmployeeSaveIn saveIn = employeeSaveIn("  E1005  ", "  Null Query Employee  ", 1L, 1L);
        saveIn.setEmployeeNameKana("   ");
        saveIn.setGender("   ");
        saveIn.setEmail("   ");
        saveIn.setPhone("   ");
        saveIn.setStatus("   ");

        AttendanceOut.EmployeeOut created = attendanceEmployeeService.createEmployee(saveIn);
        assertEquals("E1005", created.getEmployeeNo());
        assertEquals("Null Query Employee", created.getEmployeeName());
        assertEquals("ACTIVE", created.getStatus());
        org.junit.jupiter.api.Assertions.assertNull(created.getEmployeeNameKana());
        org.junit.jupiter.api.Assertions.assertNull(created.getEmail());
        org.junit.jupiter.api.Assertions.assertNull(created.getPhone());

        java.util.List<AttendanceOut.EmployeeOut> employees = attendanceEmployeeService.listEmployees(null);
        assertEquals(2, employees.size());
    }

    /**
     * 测试目的：验证shouldUpdateEmployeeInPlaceAndRewritePartTimeWorkRule场景。
     */
    @Test
    void shouldUpdateEmployeeInPlaceAndRewritePartTimeWorkRule() {
        // 先创建一个全职员工，作为后续切换成兼职规则的目标数据。
        AttendanceIn.EmployeeSaveIn createIn = employeeSaveIn("E1006", "Mutable Employee", 1L, 1L);
        createIn.setHireDate(LocalDate.of(2026, 5, 1));
        AttendanceOut.EmployeeOut created = attendanceEmployeeService.createEmployee(createIn);

        // 更新时把雇佣类型改成兼职并清空状态，验证服务层会重算规则并回填默认状态。
        AttendanceIn.EmployeeSaveIn updateIn = employeeSaveIn("E1006", "Part Time Employee", 1L, 1L);
        updateIn.setEmploymentType("PART_TIME");
        updateIn.setHireDate(null);
        updateIn.setStatus("   ");
        AttendanceOut.EmployeeOut updated = attendanceEmployeeService.updateEmployee(created.getId(), updateIn);

        assertEquals("E1006", updated.getEmployeeNo());
        assertEquals("PART_TIME", updated.getEmploymentType());
        assertEquals("ACTIVE", updated.getStatus());

        // 直接查规则表，确认兼职员工的日工时、周工时和规则类型都被改写。
        Map<String, Object> workRule = jdbcTemplate.queryForMap(
            "SELECT work_rule_type, standard_daily_minutes, standard_weekly_minutes, effective_start_date FROM employee_work_rule WHERE tenant_id = 1 AND employee_id = ?",
            created.getId()
        );
        assertEquals(300, ((Number) workRule.get("standard_daily_minutes")).intValue());
        assertEquals(1500, ((Number) workRule.get("standard_weekly_minutes")).intValue());
        assertEquals("PART_TIME", workRule.get("work_rule_type"));
        assertEquals(LocalDate.now(), ((java.sql.Date) workRule.get("effective_start_date")).toLocalDate());
    }

    /**
     * 测试目的：验证shouldRejectHeaderOnlyCsvAndAggregateShortRowAndBadDepartmentErrors场景。
     */
    @Test
    void shouldRejectHeaderOnlyCsvAndAggregateShortRowAndBadDepartmentErrors() {
        IllegalArgumentException headerOnly = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.importEmployees(
                employeeImportIn("employeeNo,employeeName,employeeNameKana,employmentType,workplaceCode,departmentCode,hireDate,email,phone")
            )
        );
        assertTrue(headerOnly.getMessage().contains("CSV"));

        String csvText = String.join("\n",
            "employeeNo,employeeName,employeeNameKana,employmentType,workplaceCode,departmentCode,hireDate,email,phone",
            "",
            "E1007,Too Short",
            "E1008,Bad Department,Import Kana,FULL_TIME,TKY-HQ,UNKNOWN,2026-05-01,bad-dept@example.jp,090-5000-0000"
        );
        AttendanceOut.EmployeeImportResultOut result = attendanceEmployeeService.importEmployees(employeeImportIn(csvText));

        assertEquals(0, result.getSuccessCount());
        assertEquals(2, result.getFailedCount());
        assertTrue(result.getErrors().stream().allMatch(item -> item != null && !item.isBlank()));
        assertTrue(result.getErrors().stream().anyMatch(item -> item.contains("UNKNOWN")));
    }

    /**
     * 测试目的：验证shouldRejectInvalidEmployeeInputsAndMissingReferences场景。
     */
    @Test
    void shouldRejectInvalidEmployeeInputsAndMissingReferences() {
        IllegalArgumentException nullSaveIn = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.createEmployee(null)
        );
        assertTrue(nullSaveIn.getMessage().contains("employeeSaveIn"));

        AttendanceIn.EmployeeSaveIn missingEmployeeNo = employeeSaveIn("E1009", "Missing Employee No", 1L, 1L);
        missingEmployeeNo.setEmployeeNo("   ");
        IllegalArgumentException employeeNoError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.createEmployee(missingEmployeeNo)
        );
        assertTrue(employeeNoError.getMessage().contains("employeeNo"));

        AttendanceIn.EmployeeSaveIn missingEmployeeName = employeeSaveIn("E1010", "Missing Employee Name", 1L, 1L);
        missingEmployeeName.setEmployeeName("   ");
        IllegalArgumentException employeeNameError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.createEmployee(missingEmployeeName)
        );
        assertTrue(employeeNameError.getMessage().contains("employeeName"));

        AttendanceIn.EmployeeSaveIn missingEmploymentType = employeeSaveIn("E1011", "Missing Employment Type", 1L, 1L);
        missingEmploymentType.setEmploymentType("   ");
        IllegalArgumentException employmentTypeError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.createEmployee(missingEmploymentType)
        );
        assertTrue(employmentTypeError.getMessage().contains("employmentType"));

        AttendanceIn.EmployeeSaveIn invalidWorkplaceId = employeeSaveIn("E1012", "Invalid Workplace Id", 1L, 1L);
        invalidWorkplaceId.setWorkplaceId(0L);
        IllegalArgumentException workplaceIdError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.createEmployee(invalidWorkplaceId)
        );
        assertTrue(workplaceIdError.getMessage().contains("workplaceId"));

        AttendanceIn.EmployeeSaveIn invalidDepartmentId = employeeSaveIn("E1013", "Invalid Department Id", 1L, 1L);
        invalidDepartmentId.setDepartmentId(0L);
        IllegalArgumentException departmentIdError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.createEmployee(invalidDepartmentId)
        );
        assertTrue(departmentIdError.getMessage().contains("departmentId"));

        AttendanceIn.EmployeeSaveIn missingWorkplace = employeeSaveIn("E1014", "Missing Workplace Ref", 999L, 1L);
        IllegalArgumentException workplaceRefError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.createEmployee(missingWorkplace)
        );
        assertTrue(workplaceRefError.getMessage().contains("999"));

        AttendanceIn.EmployeeSaveIn missingDepartment = employeeSaveIn("E1015", "Missing Department Ref", 1L, 999L);
        IllegalArgumentException departmentRefError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.createEmployee(missingDepartment)
        );
        assertTrue(departmentRefError.getMessage().contains("999"));

        IllegalArgumentException invalidUpdateId = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.updateEmployee(0L, employeeSaveIn("E1016", "Bad Update Id", 1L, 1L))
        );
        assertTrue(invalidUpdateId.getMessage().contains("id"));

        IllegalArgumentException missingEmployee = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.deleteEmployee(999L)
        );
        assertTrue(missingEmployee.getMessage().contains("999"));
    }

    /**
     * 测试目的：验证shouldRejectInvalidExternalMappingInputs场景。
     */
    @Test
    void shouldRejectInvalidExternalMappingInputs() {
        IllegalArgumentException invalidId = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.bindExternalMapping(0L, externalMappingSaveIn("KINTAI", "EXT-003", "NO-003", "ACTIVE"))
        );
        assertTrue(invalidId.getMessage().contains("id"));

        AttendanceIn.ExternalMappingSaveIn missingSource = externalMappingSaveIn("KINTAI", "EXT-003", "NO-003", "ACTIVE");
        missingSource.setSourceSystem("   ");
        IllegalArgumentException sourceError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.bindExternalMapping(1L, missingSource)
        );
        assertTrue(sourceError.getMessage().contains("sourceSystem"));

        AttendanceIn.ExternalMappingSaveIn missingExternalId = externalMappingSaveIn("KINTAI", "EXT-004", "NO-004", "ACTIVE");
        missingExternalId.setExternalEmployeeId("   ");
        IllegalArgumentException externalIdError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.bindExternalMapping(1L, missingExternalId)
        );
        assertTrue(externalIdError.getMessage().contains("externalEmployeeId"));

        AttendanceIn.ExternalMappingSaveIn missingExternalNo = externalMappingSaveIn("KINTAI", "EXT-005", "NO-005", "ACTIVE");
        missingExternalNo.setExternalEmployeeNo("   ");
        IllegalArgumentException externalNoError = assertThrows(
            IllegalArgumentException.class,
            () -> attendanceEmployeeService.bindExternalMapping(1L, missingExternalNo)
        );
        assertTrue(externalNoError.getMessage().contains("externalEmployeeNo"));
    }

    /**
     * 测试目的：验证shouldCoverPrivateEmployeeHelperBranchesViaReflection场景。
     */
    @Test
    void shouldCoverPrivateEmployeeHelperBranchesViaReflection() {
        AttendanceIn.EmployeeSaveIn nullWorkplace = employeeSaveIn("E1017", "Null Workplace", 1L, 1L);
        nullWorkplace.setWorkplaceId(null);
        IllegalArgumentException workplaceNullError = assertThrows(
            IllegalArgumentException.class,
            () -> invokeEmployeePrivate("validateEmployee", new Class<?>[]{AttendanceIn.EmployeeSaveIn.class}, nullWorkplace)
        );
        assertTrue(workplaceNullError.getMessage().contains("workplaceId"));

        AttendanceIn.EmployeeSaveIn nullDepartment = employeeSaveIn("E1018", "Null Department", 1L, 1L);
        nullDepartment.setDepartmentId(null);
        IllegalArgumentException departmentNullError = assertThrows(
            IllegalArgumentException.class,
            () -> invokeEmployeePrivate("validateEmployee", new Class<?>[]{AttendanceIn.EmployeeSaveIn.class}, nullDepartment)
        );
        assertTrue(departmentNullError.getMessage().contains("departmentId"));

        IllegalArgumentException nullIdError = assertThrows(
            IllegalArgumentException.class,
            () -> invokeEmployeePrivate("validateId", new Class<?>[]{Long.class}, new Object[]{null})
        );
        assertTrue(nullIdError.getMessage().contains("id"));

        AttendanceOut.EmployeeOut created = attendanceEmployeeService.createEmployee(
            employeeSaveIn("E1019", "Reflection Rule Employee", 1L, 1L)
        );
        invokeEmployeePrivate(
            "upsertEmployeeWorkRule",
            new Class<?>[]{Long.class, String.class, LocalDate.class},
            created.getId(),
            "ARBEIT",
            LocalDate.of(2026, 5, 3)
        );
        Map<String, Object> arbeitRule = jdbcTemplate.queryForMap(
            "SELECT work_rule_type, standard_daily_minutes, standard_weekly_minutes, effective_start_date FROM employee_work_rule WHERE tenant_id = 1 AND employee_id = ?",
            created.getId()
        );
        assertEquals(300, ((Number) arbeitRule.get("standard_daily_minutes")).intValue());
        assertEquals(1500, ((Number) arbeitRule.get("standard_weekly_minutes")).intValue());
        assertEquals(LocalDate.of(2026, 5, 3), ((java.sql.Date) arbeitRule.get("effective_start_date")).toLocalDate());

        attendanceEmployeeDao.deleteWorkRuleByEmployeeId(1L, created.getId());
        invokeEmployeePrivate(
            "upsertEmployeeWorkRule",
            new Class<?>[]{Long.class, String.class, LocalDate.class},
            created.getId(),
            null,
            null
        );
        Map<String, Object> standardRule = jdbcTemplate.queryForMap(
            "SELECT work_rule_type, standard_daily_minutes, standard_weekly_minutes, effective_start_date FROM employee_work_rule WHERE tenant_id = 1 AND employee_id = ?",
            created.getId()
        );
        assertEquals("STANDARD", standardRule.get("work_rule_type"));
        assertEquals(480, ((Number) standardRule.get("standard_daily_minutes")).intValue());
        assertEquals(2400, ((Number) standardRule.get("standard_weekly_minutes")).intValue());
    }

    /**
     * 测试辅助目的：构造员工保存入参，统一复用员工新增、更新和非法输入校验场景。
     */
    private AttendanceIn.EmployeeSaveIn employeeSaveIn(String employeeNo, String employeeName, Long workplaceId, Long departmentId) {
        AttendanceIn.EmployeeSaveIn saveIn = new AttendanceIn.EmployeeSaveIn();
        saveIn.setEmployeeNo(employeeNo);
        saveIn.setEmployeeName(employeeName);
        saveIn.setEmploymentType("FULL_TIME");
        saveIn.setWorkplaceId(workplaceId);
        saveIn.setDepartmentId(departmentId);
        return saveIn;
    }

    /**
     * 测试辅助目的：构造外部打卡映射入参，统一覆盖新增映射、更新映射和非法映射校验。
     */
    private AttendanceIn.ExternalMappingSaveIn externalMappingSaveIn(String sourceSystem, String externalEmployeeId, String externalEmployeeNo, String status) {
        AttendanceIn.ExternalMappingSaveIn saveIn = new AttendanceIn.ExternalMappingSaveIn();
        saveIn.setSourceSystem(sourceSystem);
        saveIn.setExternalEmployeeId(externalEmployeeId);
        saveIn.setExternalEmployeeNo(externalEmployeeNo);
        saveIn.setStatus(status);
        return saveIn;
    }

    /**
     * 测试辅助目的：构造员工导入请求体，便于在导入测试里直接塞入整段 CSV 文本。
     */
    private AttendanceIn.EmployeeImportIn employeeImportIn(String csvText) {
        AttendanceIn.EmployeeImportIn saveIn = new AttendanceIn.EmployeeImportIn();
        saveIn.setCsvText(csvText);
        return saveIn;
    }

    /**
     * 测试辅助目的：通过反射调用私有方法，补齐难以从公开接口覆盖到的分支验证。
     */
    private Object invokeEmployeePrivate(String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Object target = AopTestUtils.getTargetObject(attendanceEmployeeService);
            Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
