package com.sp.selfsp.attendance.daily.controller;

import com.sp.selfsp.attendance.daily.domain.in.AttendanceDailyIn;
import com.sp.selfsp.attendance.daily.domain.out.AttendanceDailyOut;
import com.sp.selfsp.attendance.daily.service.AttendanceDailyService;
import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第四阶段日次勤怠控制器。
 */
// 把当前类注册为 Spring REST 控制器，负责对外暴露日次勤怠接口。
@RestController
// 给当前控制器绑定统一接口前缀，便于前端按模块访问。
@RequestMapping("/api/attendance/daily")
// 定义 日次勤怠控制器，承接当前文件对应的业务职责。
public class AttendanceDailyController {

    // 声明 日次勤怠服务 字段，用来保存当前业务状态或依赖。
    private final AttendanceDailyService attendanceDailyService;

    // 注入第四阶段服务实现，统一承接列表、详情和重算动作。
    public AttendanceDailyController(AttendanceDailyService attendanceDailyService) {
        // 把外部传入结果写入 日次勤怠服务 字段，供后续流程继续使用。
        this.attendanceDailyService = attendanceDailyService;
    }

    // 把当前方法暴露为查询接口，供前端读取日次结果列表和摘要。
    @GetMapping
    // 定义 查询日次列表 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceDailyOut.DailyListOut> listDailyResults(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(required = false) Long workplaceId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employeeKeyword,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Boolean exceptionOnly,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        AttendanceDailyIn.DailyQueryIn queryIn = new AttendanceDailyIn.DailyQueryIn();
        queryIn.setStartDate(startDate);
        queryIn.setEndDate(endDate);
        queryIn.setWorkplaceId(workplaceId);
        queryIn.setDepartmentId(departmentId);
        queryIn.setEmployeeKeyword(employeeKeyword);
        queryIn.setStatus(status);
        queryIn.setExceptionOnly(exceptionOnly);
        queryIn.setPage(page);
        queryIn.setPageSize(pageSize);
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceDailyService.listDailyResults(queryIn));
    }

    // 把当前方法暴露为查询接口，供前端读取单条日次详情。
    @GetMapping("/{id}")
    // 定义 查询日次详情 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceDailyOut.DailyDetailOut> getDailyDetail(@PathVariable Long id) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceDailyService.getDailyDetail(id));
    }

    // 把当前方法暴露为重算接口，供用户在单条异常上立即刷新当前结果。
    @PostMapping("/recalculate")
    // 定义 单日重算 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceDailyOut.RecalculateResultOut> recalculateDaily(
        @RequestBody AttendanceDailyIn.DailyRecalculateIn recalculateIn
    ) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceDailyService.recalculateDaily(recalculateIn));
    }

    // 把当前方法暴露为重算接口，供顶部筛选条件批量刷新整个范围。
    @PostMapping("/recalculate-range")
    // 定义 范围重算 接口入口，负责接收前端请求并转发到业务服务。
    public CommonResponse<AttendanceDailyOut.RecalculateResultOut> recalculateRange(
        @RequestBody AttendanceDailyIn.DailyRecalculateRangeIn recalculateRangeIn
    ) {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return CommonResponse.success(attendanceDailyService.recalculateRange(recalculateRangeIn));
    }
}
