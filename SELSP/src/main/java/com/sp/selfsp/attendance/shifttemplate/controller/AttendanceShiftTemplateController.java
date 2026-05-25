package com.sp.selfsp.attendance.shifttemplate.controller;

import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.shifttemplate.service.AttendanceShiftTemplateService;
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
@RequestMapping("/api/attendance/shift-templates")
// 定义 考勤班次模板控制器，承接当前文件对应的业务职责。
public class AttendanceShiftTemplateController {

    // 声明 考勤班次模板服务 字段，用来保存当前业务状态或依赖。
    private final AttendanceShiftTemplateService attendanceShiftTemplateService;

    // 定义 考勤班次模板控制器 接口入口，负责接收前端请求并转发到业务服务。
    public AttendanceShiftTemplateController(AttendanceShiftTemplateService attendanceShiftTemplateService) {
        // 把外部传入结果写入 考勤班次模板服务 字段，供后续流程继续使用。
        this.attendanceShiftTemplateService = attendanceShiftTemplateService;
    }

    // 把当前方法暴露为查询接口，供前端读取业务数据。
    @GetMapping
    // 定义 list班次Templates 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<List<AttendanceOut.ShiftTemplateOut>> listShiftTemplates() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceShiftTemplateService.listShiftTemplates());
    }

    // 把当前方法暴露为新增接口，供前端提交新数据。
    @PostMapping
    // 定义 新增班次模板 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.ShiftTemplateOut> createShiftTemplate(@RequestBody AttendanceIn.ShiftTemplateSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceShiftTemplateService.createShiftTemplate(saveIn));
    }

    // 把当前方法暴露为更新接口，供前端保存修改结果。
    @PutMapping("/{id}")
    // 定义 更新班次模板 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceOut.ShiftTemplateOut> updateShiftTemplate(@PathVariable Long id, @RequestBody AttendanceIn.ShiftTemplateSaveIn saveIn) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceShiftTemplateService.updateShiftTemplate(id, saveIn));
    }

    // 把当前方法暴露为删除接口，供前端移除业务数据。
    @DeleteMapping("/{id}")
    // 定义 删除班次模板 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<Void> deleteShiftTemplate(@PathVariable Long id) {
        // 执行当前业务步骤，推进本行对应的 控制器 处理。
        attendanceShiftTemplateService.deleteShiftTemplate(id);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(null);
    }

    // 把当前方法暴露为新增接口，供前端提交新数据。
    @PostMapping("/recommended")
    // 定义 生成推荐班次Templates 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<List<AttendanceOut.ShiftTemplateOut>> generateRecommendedShiftTemplates() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceShiftTemplateService.generateRecommendedShiftTemplates());
    }
}
