/*
 * 文件名：AttendanceBootstrapServiceImpl.java
 * 描述：考勤首页聚合服务实现。
 * 创建时间：2026-05-25
 * 修改时间：2026-05-25
 */
package com.sp.selfsp.attendance.bootstrap.service.impl;

import com.sp.selfsp.attendance.bootstrap.dao.AttendanceBootstrapDao;
import com.sp.selfsp.attendance.bootstrap.service.AttendanceBootstrapService;
import com.sp.selfsp.attendance.common.AttendanceTenantContext;
import com.sp.selfsp.attendance.department.dao.AttendanceDepartmentDao;
import com.sp.selfsp.attendance.domain.in.AttendanceIn;
import com.sp.selfsp.attendance.domain.out.AttendanceOut;
import com.sp.selfsp.attendance.employee.dao.AttendanceEmployeeDao;
import com.sp.selfsp.attendance.shifttemplate.dao.AttendanceShiftTemplateDao;
import com.sp.selfsp.attendance.tenant.dao.AttendanceTenantDao;
import com.sp.selfsp.attendance.workplace.dao.AttendanceWorkplaceDao;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * 考勤首页聚合服务实现。
 */
// 把当前类注册为服务实现，负责承接业务编排。
@Service
// 定义 考勤初始化聚合服务Impl，承接当前文件对应的业务职责。
public class AttendanceBootstrapServiceImpl implements AttendanceBootstrapService {

    // 声明 考勤初始化聚合数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceBootstrapDao attendanceBootstrapDao;
    // 声明 考勤租户数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceTenantDao attendanceTenantDao;
    // 声明 考勤事业所数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceWorkplaceDao attendanceWorkplaceDao;
    // 声明 考勤部门数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceDepartmentDao attendanceDepartmentDao;
    // 声明 考勤员工数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceEmployeeDao attendanceEmployeeDao;
    // 声明 考勤班次模板数据访问 字段，用来保存当前业务状态或依赖。
    private final AttendanceShiftTemplateDao attendanceShiftTemplateDao;

    /**
     * 构造考勤首页聚合服务实现。
     *
     * @param attendanceBootstrapDao 首页统计数据访问接口
     * @param attendanceTenantDao 租户数据访问接口
     * @param attendanceWorkplaceDao 事业所数据访问接口
     * @param attendanceDepartmentDao 部门数据访问接口
     * @param attendanceEmployeeDao 员工数据访问接口
     * @param attendanceShiftTemplateDao 班次模板数据访问接口
     */
    // 定义 考勤初始化聚合服务Impl 业务动作，负责承接当前模块的处理流程。
    public AttendanceBootstrapServiceImpl(
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceBootstrapDao attendanceBootstrapDao,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceTenantDao attendanceTenantDao,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceWorkplaceDao attendanceWorkplaceDao,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceDepartmentDao attendanceDepartmentDao,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceEmployeeDao attendanceEmployeeDao,
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceShiftTemplateDao attendanceShiftTemplateDao
    // 执行当前业务步骤，推进本行对应的 服务impl 处理。
    ) {
        // 把外部传入结果写入 考勤初始化聚合数据访问 字段，供后续流程继续使用。
        this.attendanceBootstrapDao = attendanceBootstrapDao;
        // 把外部传入结果写入 考勤租户数据访问 字段，供后续流程继续使用。
        this.attendanceTenantDao = attendanceTenantDao;
        // 把外部传入结果写入 考勤事业所数据访问 字段，供后续流程继续使用。
        this.attendanceWorkplaceDao = attendanceWorkplaceDao;
        // 把外部传入结果写入 考勤部门数据访问 字段，供后续流程继续使用。
        this.attendanceDepartmentDao = attendanceDepartmentDao;
        // 把外部传入结果写入 考勤员工数据访问 字段，供后续流程继续使用。
        this.attendanceEmployeeDao = attendanceEmployeeDao;
        // 把外部传入结果写入 考勤班次模板数据访问 字段，供后续流程继续使用。
        this.attendanceShiftTemplateDao = attendanceShiftTemplateDao;
    }

    /**
     * 读取首页聚合概览。
     *
     * @return 首页聚合结果
     */
    // 显式声明当前方法在覆写上层约定，实现当前业务契约。
    @Override
    // 对外返回 初始化聚合汇总，供上下游继续读取当前业务字段。
    public AttendanceOut.BootstrapSummaryOut getBootstrapSummary() {
        // 首页聚合只负责组装多个子模块的只读结果，不承接写逻辑。
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        Map<String, Object> counts = attendanceBootstrapDao.selectCounts(AttendanceTenantContext.DEFAULT_TENANT_ID);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.BootstrapSummaryOut summaryOut = new AttendanceOut.BootstrapSummaryOut();
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        summaryOut.setTenant(attendanceTenantDao.selectCurrentTenant(AttendanceTenantContext.DEFAULT_TENANT_ID));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        summaryOut.setWorkplaces(attendanceWorkplaceDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        summaryOut.setDepartments(attendanceDepartmentDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        summaryOut.setEmployees(attendanceEmployeeDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID, new AttendanceIn.EmployeeQueryIn()));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        summaryOut.setShiftTemplates(attendanceShiftTemplateDao.selectList(AttendanceTenantContext.DEFAULT_TENANT_ID));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        List<AttendanceOut.BootstrapStepOut> steps = new ArrayList<>();
        // 把当前初始化步骤加入向导列表，供前端按顺序展示完成度。
        steps.add(buildStep("tenant", "wizard.tenant", countAsInt(counts, "tenantCount"), "guide.tenant"));
        // 把当前初始化步骤加入向导列表，供前端按顺序展示完成度。
        steps.add(buildStep("workplace", "wizard.workplace", countAsInt(counts, "workplaceCount"), "guide.workplace"));
        // 把当前初始化步骤加入向导列表，供前端按顺序展示完成度。
        steps.add(buildStep("employee", "wizard.employee", countAsInt(counts, "employeeCount"), "guide.employee"));
        // 把当前初始化步骤加入向导列表，供前端按顺序展示完成度。
        steps.add(buildStep("shiftTemplate", "wizard.shiftTemplate", countAsInt(counts, "shiftTemplateCount"), "guide.shiftTemplate"));
        // 把当前初始化步骤加入向导列表，供前端按顺序展示完成度。
        steps.add(buildStep("workRule", "wizard.workRule", countAsInt(counts, "workRuleCount"), "guide.workRule"));
        // 只有当前五个基础准备项都完成后，第二阶段排班入口才真正开放。
        boolean scheduleReady = isScheduleReady(counts);
        // 把第二阶段排班步骤加入向导列表，已具备前置条件时直接开放进入。
        steps.add(buildPhaseGateStep("schedule", "wizard.schedule", countAsInt(counts, "scheduleCount"), "guide.schedule", scheduleReady));
        // 打卡接收仍依赖第二阶段先准备出排班，因此只有存在排班后才开放。
        steps.add(buildPhaseGateStep("punch", "wizard.punch", 0, "guide.punch", countAsInt(counts, "scheduleCount") > 0));
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        summaryOut.setSteps(steps);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        summaryOut.setRecommendedNextAction(resolveRecommendedNextAction(steps));
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return summaryOut;
    }

    // 定义 build步骤 业务动作，负责承接当前模块的处理流程。
    private AttendanceOut.BootstrapStepOut buildStep(String stepCode, String titleKey, int count, String description) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.BootstrapStepOut stepOut = new AttendanceOut.BootstrapStepOut();
        // 首页向导通过步骤编码和字典键让前端知道当前应该优先处理什么。
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        stepOut.setStepCode(stepCode);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        stepOut.setTitleKey(titleKey);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        stepOut.setCount(count);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        stepOut.setDescription(description);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        stepOut.setStatus(count > 0 ? "COMPLETED" : "NEEDS_ACTION");
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return stepOut;
    }

    // 定义 build阶段闸门步骤 业务动作，负责承接当前模块的处理流程。
    private AttendanceOut.BootstrapStepOut buildPhaseGateStep(String stepCode, String titleKey, int count, String description, boolean ready) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        AttendanceOut.BootstrapStepOut stepOut = new AttendanceOut.BootstrapStepOut();
        // 后续阶段步骤只有在上游准备完成后才会从锁定态切换到可操作态。
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        stepOut.setStepCode(stepCode);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        stepOut.setTitleKey(titleKey);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        stepOut.setCount(count);
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        stepOut.setDescription(description);
        if (!ready) {
            // 前置条件未满足时仍以锁定态展示，避免用户误以为现在就能操作。
            stepOut.setStatus("LOCKED_NEXT_PHASE");
        } else {
            // 当前阶段入口已开放后，根据排班数量决定是已完成还是需要处理。
            stepOut.setStatus(count > 0 ? "COMPLETED" : "NEEDS_ACTION");
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return stepOut;
    }

    // 定义 resolve推荐NextAction 业务动作，负责承接当前模块的处理流程。
    private String resolveRecommendedNextAction(List<AttendanceOut.BootstrapStepOut> steps) {
        // 遍历当前业务集合，逐条完成对应的数据处理动作。
        for (AttendanceOut.BootstrapStepOut step : steps) {
            // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
            if (Objects.equals("NEEDS_ACTION", step.getStatus())) {
                // 返回当前步骤产出的业务结果，继续交给上一层消费。
                return step.getTitleKey();
            }
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return "wizard.schedule";
    }

    // 定义 数量AsInt 业务动作，负责承接当前模块的处理流程。
    private int countAsInt(Map<String, Object> counts, String key) {
        // 执行当前业务步骤，推进本行对应的 服务impl 处理。
        Object value = counts.get(key);
        // H2 聚合字段别名在 HashMap 中可能被写成全大写，因此这里补一个大小写兜底读取。
        if (value == null) {
            value = counts.get(key.toUpperCase());
        }
        // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
        if (value instanceof Number number) {
            // 返回当前步骤产出的业务结果，继续交给上一层消费。
            return number.intValue();
        }
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return 0;
    }

    // 定义 is排班Ready 业务动作，负责承接当前模块的处理流程。
    private boolean isScheduleReady(Map<String, Object> counts) {
        // 第二阶段排班至少需要公司、事业所、员工、班次模板和默认规则都已到位。
        return countAsInt(counts, "tenantCount") > 0
            && countAsInt(counts, "workplaceCount") > 0
            && countAsInt(counts, "employeeCount") > 0
            && countAsInt(counts, "shiftTemplateCount") > 0
            && countAsInt(counts, "workRuleCount") > 0;
    }
}
