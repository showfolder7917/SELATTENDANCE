package com.sp.selfsp.attendance.employee.controller;

import com.sp.selfsp.attendance.common.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.common.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.employee.service.AttendanceEmployeeService;
import com.sp.selfsp.common.util.CommonResponse;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/employees")
public class AttendanceEmployeeController {

    private final AttendanceEmployeeService attendanceEmployeeService;

    public AttendanceEmployeeController(AttendanceEmployeeService attendanceEmployeeService) {
        this.attendanceEmployeeService = attendanceEmployeeService;
    }

    @GetMapping
    public CommonResponse<List<AttendanceOut.EmployeeOut>> listEmployees(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employmentType,
        @RequestParam(required = false) String status
    ) {
        // 查询条件先组装成统一入参对象，后续服务层才能复用同一套筛选逻辑。
        AttendanceIn.EmployeeQueryIn queryIn = new AttendanceIn.EmployeeQueryIn();
        queryIn.setKeyword(keyword);
        queryIn.setDepartmentId(departmentId);
        queryIn.setEmploymentType(employmentType);
        queryIn.setStatus(status);
        // 列表结果统一包进标准响应壳，供前端员工列表页直接消费。
        return CommonResponse.success(attendanceEmployeeService.listEmployees(queryIn));
    }

    @PostMapping
    public CommonResponse<AttendanceOut.EmployeeOut> createEmployee(@RequestBody AttendanceIn.EmployeeSaveIn saveIn) {
        // 新增员工时直接把表单入参交给服务层做校验、归一化和持久化。
        return CommonResponse.success(attendanceEmployeeService.createEmployee(saveIn));
    }

    @PutMapping("/{id}")
    public CommonResponse<AttendanceOut.EmployeeOut> updateEmployee(@PathVariable Long id, @RequestBody AttendanceIn.EmployeeSaveIn saveIn) {
        // 更新接口使用路径 id 锁定目标员工，避免只靠请求体导致误更新。
        return CommonResponse.success(attendanceEmployeeService.updateEmployee(id, saveIn));
    }

    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteEmployee(@PathVariable Long id) {
        // 删除前的引用校验和清理动作全部在服务层执行，控制器只负责触发。
        attendanceEmployeeService.deleteEmployee(id);
        return CommonResponse.success(null);
    }

    @PutMapping("/{id}/external-mapping")
    public CommonResponse<AttendanceOut.EmployeeOut> bindExternalMapping(@PathVariable Long id, @RequestBody AttendanceIn.ExternalMappingSaveIn saveIn) {
        // 外部打卡映射单独拆接口，便于页面在员工详情里单独维护第三方绑定信息。
        return CommonResponse.success(attendanceEmployeeService.bindExternalMapping(id, saveIn));
    }

    @PostMapping("/import")
    public CommonResponse<AttendanceOut.EmployeeImportResultOut> importEmployees(@RequestBody AttendanceIn.EmployeeImportIn saveIn) {
        // CSV 导入返回成功数、失败数和错误行，供前端直接展示导入结果弹窗。
        return CommonResponse.success(attendanceEmployeeService.importEmployees(saveIn));
    }

    @GetMapping("/export")
    public CommonResponse<AttendanceOut.CsvExportOut> exportEmployees() {
        // 导出接口返回文件名和 CSV 文本，前端可以直接组装下载文件。
        return CommonResponse.success(attendanceEmployeeService.exportEmployees());
    }
}

