package com.sp.selfsp.attendance.department.controller;

import com.sp.selfsp.attendance.department.service.AttendanceDepartmentService;
import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.common.util.CommonResponse;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 把当前类注册为 Spring REST 控制器，负责对外暴露考勤接口。
@RestController
// 给当前控制器绑定统一接口前缀，便于前端按模块访问。
@RequestMapping("/api/attendance/departments")
// 定义 考勤部门控制器，承接当前文件对应的业务职责。
public class AttendanceDepartmentController {

    // 声明 考勤部门服务 字段，用来保存当前业务状态或依赖。
    private final AttendanceDepartmentService attendanceDepartmentService;

    // 定义 考勤部门控制器 接口入口，负责接收前端请求并转发到业务服务。
    public AttendanceDepartmentController(AttendanceDepartmentService attendanceDepartmentService) {
        // 把外部传入结果写入 考勤部门服务 字段，供后续流程继续使用。
        this.attendanceDepartmentService = attendanceDepartmentService;
    }

    // 把当前方法暴露为查询接口，供前端读取业务数据。
    @GetMapping
    // 定义 listDepartments 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<List<AttendanceOut.DepartmentOut>> listDepartments() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceDepartmentService.listDepartments());
    }

    // 把当前方法暴露为新增接口，供前端提交新数据。
    @PostMapping
    // 定义 新增部门 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.DepartmentOut> createDepartment(@RequestBody AttendanceIn.DepartmentSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceDepartmentService.createDepartment(saveIn));
    }

    // 把当前方法暴露为更新接口，供前端保存修改结果。
    @PutMapping("/{id}")
    // 定义 更新部门 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.DepartmentOut> updateDepartment(@PathVariable Long id, @RequestBody AttendanceIn.DepartmentSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceDepartmentService.updateDepartment(id, saveIn));
    }

    // 把当前方法暴露为删除接口，供前端移除业务数据。
    @DeleteMapping("/{id}")
    // 定义 删除部门 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<Void> deleteDepartment(@PathVariable Long id) {
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
        attendanceDepartmentService.deleteDepartment(id);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(null);
    }
}
