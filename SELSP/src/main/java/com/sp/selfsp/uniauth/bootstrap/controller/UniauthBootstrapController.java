package com.sp.selfsp.uniauth.bootstrap.controller;

import com.sp.selfsp.common.util.CommonResponse;
import com.sp.selfsp.uniauth.bootstrap.service.UniauthBootstrapService;
import com.sp.selfsp.uniauth.common.UniauthCurrentUserContext;
import com.sp.selfsp.uniauth.bootstrap.domain.out.UniauthBootstrapOut;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 工作台控制器只暴露权限中心管理工作台读取接口，不再混入具体保存动作。
@RestController
@RequestMapping("/api/uniauth")
public class UniauthBootstrapController {

    // 工作台服务负责聚合租户、用户、角色、菜单和权限列表。
    private final UniauthBootstrapService uniauthBootstrapService;

    // 构造控制器时注入工作台服务。
    public UniauthBootstrapController(UniauthBootstrapService uniauthBootstrapService) {
        // 保存工作台服务，供 bootstrap 接口复用。
        this.uniauthBootstrapService = uniauthBootstrapService;
    }

    // bootstrap 接口负责一次性返回权限中心主工作台初始化数据。
    @GetMapping("/bootstrap")
    public CommonResponse<UniauthBootstrapOut> bootstrap() {
        // 当前接口要求必须先登录，再根据当前用户权限返回工作台聚合数据。
        return CommonResponse.success(uniauthBootstrapService.getWorkbench(UniauthCurrentUserContext.requireUser()));
    }
}
