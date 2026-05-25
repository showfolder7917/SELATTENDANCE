package com.sp.selfsp.attendance.workplace.controller;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.workplace.service.AttendanceWorkplaceService;
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
@RequestMapping("/api/attendance/workplaces")
// 定义 考勤事业所控制器，承接当前文件对应的业务职责。
public class AttendanceWorkplaceController {

    // 声明 考勤事业所服务 字段，用来保存当前业务状态或依赖。
    private final AttendanceWorkplaceService attendanceWorkplaceService;

    // 定义 考勤事业所控制器 接口入口，负责接收前端请求并转发到业务服务。
    public AttendanceWorkplaceController(AttendanceWorkplaceService attendanceWorkplaceService) {
        // 把外部传入结果写入 考勤事业所服务 字段，供后续流程继续使用。
        this.attendanceWorkplaceService = attendanceWorkplaceService;
    }

    // 把当前方法暴露为查询接口，供前端读取业务数据。
    @GetMapping
    // 定义 listWorkplaces 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<List<AttendanceOut.WorkplaceOut>> listWorkplaces() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceWorkplaceService.listWorkplaces());
    }

    // 把当前方法暴露为新增接口，供前端提交新数据。
    @PostMapping
    // 定义 新增事业所 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.WorkplaceOut> createWorkplace(@RequestBody AttendanceIn.WorkplaceSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceWorkplaceService.createWorkplace(saveIn));
    }

    // 把当前方法暴露为更新接口，供前端保存修改结果。
    @PutMapping("/{id}")
    // 定义 更新事业所 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.WorkplaceOut> updateWorkplace(@PathVariable Long id, @RequestBody AttendanceIn.WorkplaceSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceWorkplaceService.updateWorkplace(id, saveIn));
    }

    // 把当前方法暴露为删除接口，供前端移除业务数据。
    @DeleteMapping("/{id}")
    // 定义 删除事业所 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<Void> deleteWorkplace(@PathVariable Long id) {
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
        attendanceWorkplaceService.deleteWorkplace(id);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(null);
    }
}

