package com.sp.selfsp.attendance.employee.controller;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
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

// 把当前类注册为 Spring REST 控制器，负责对外暴露考勤接口。
@RestController
// 给当前控制器绑定统一接口前缀，便于前端按模块访问。
@RequestMapping("/api/attendance/employees")
// 定义 考勤员工控制器，承接当前文件对应的业务职责。
public class AttendanceEmployeeController {

    // 声明 考勤员工服务 字段，用来保存当前业务状态或依赖。
    private final AttendanceEmployeeService attendanceEmployeeService;

    // 定义 考勤员工控制器 接口入口，负责接收前端请求并转发到业务服务。
    public AttendanceEmployeeController(AttendanceEmployeeService attendanceEmployeeService) {
        // 把外部传入结果写入 考勤员工服务 字段，供后续流程继续使用。
        this.attendanceEmployeeService = attendanceEmployeeService;
    }

    // 把当前方法暴露为查询接口，供前端读取业务数据。
    @GetMapping
    // 定义 listEmployees 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<List<AttendanceOut.EmployeeOut>> listEmployees(
        // 声明 RequestParam 注解，让当前代码接入既定框架能力。
        @RequestParam(required = false) String keyword,
        // 声明 RequestParam 注解，让当前代码接入既定框架能力。
        @RequestParam(required = false) Long departmentId,
        // 声明 RequestParam 注解，让当前代码接入既定框架能力。
        @RequestParam(required = false) String employmentType,
        // 声明 RequestParam 注解，让当前代码接入既定框架能力。
        @RequestParam(required = false) String status
    // 执行当前业务步骤，推进本行对应的 控制器 处理。
    ) {
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
        AttendanceIn.EmployeeQueryIn queryIn = new AttendanceIn.EmployeeQueryIn();
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
        queryIn.setKeyword(keyword);
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
        queryIn.setDepartmentId(departmentId);
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
        queryIn.setEmploymentType(employmentType);
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
        queryIn.setStatus(status);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceEmployeeService.listEmployees(queryIn));
    }

    // 把当前方法暴露为新增接口，供前端提交新数据。
    @PostMapping
    // 定义 新增员工 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.EmployeeOut> createEmployee(@RequestBody AttendanceIn.EmployeeSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceEmployeeService.createEmployee(saveIn));
    }

    // 把当前方法暴露为更新接口，供前端保存修改结果。
    @PutMapping("/{id}")
    // 定义 更新员工 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.EmployeeOut> updateEmployee(@PathVariable Long id, @RequestBody AttendanceIn.EmployeeSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceEmployeeService.updateEmployee(id, saveIn));
    }

    // 把当前方法暴露为删除接口，供前端移除业务数据。
    @DeleteMapping("/{id}")
    // 定义 删除员工 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<Void> deleteEmployee(@PathVariable Long id) {
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
        attendanceEmployeeService.deleteEmployee(id);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(null);
    }

    // 把当前方法暴露为更新接口，供前端保存修改结果。
    @PutMapping("/{id}/external-mapping")
    // 定义 绑定外部系统映射 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.EmployeeOut> bindExternalMapping(@PathVariable Long id, @RequestBody AttendanceIn.ExternalMappingSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceEmployeeService.bindExternalMapping(id, saveIn));
    }

    // 把当前方法暴露为新增接口，供前端提交新数据。
    @PostMapping("/import")
    // 定义 导入Employees 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.EmployeeImportResultOut> importEmployees(@RequestBody AttendanceIn.EmployeeImportIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceEmployeeService.importEmployees(saveIn));
    }

    // 把当前方法暴露为查询接口，供前端读取业务数据。
    @GetMapping("/export")
    // 定义 导出Employees 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.CsvExportOut> exportEmployees() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceEmployeeService.exportEmployees());
    }
}

