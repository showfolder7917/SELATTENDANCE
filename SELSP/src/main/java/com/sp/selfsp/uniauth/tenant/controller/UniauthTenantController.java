package com.sp.selfsp.uniauth.tenant.controller;

import com.sp.selfsp.common.util.CommonResponse;
import com.sp.selfsp.uniauth.common.UniauthCurrentUserContext;
import com.sp.selfsp.uniauth.tenant.domain.in.UniauthTenantSaveIn;
import com.sp.selfsp.uniauth.tenant.domain.out.UniauthTenantItemOut;
import com.sp.selfsp.uniauth.tenant.service.UniauthTenantService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 租户控制器只暴露租户维护接口，不再承担工作台聚合和其他管理动作。
@RestController
@RequestMapping("/api/uniauth/tenants")
public class UniauthTenantController {

    // 租户服务负责校验权限、保存租户和写审计日志。
    private final UniauthTenantService uniauthTenantService;

    // 构造租户控制器时注入租户服务。
    public UniauthTenantController(UniauthTenantService uniauthTenantService) {
        // 保存租户服务，供保存接口复用。
        this.uniauthTenantService = uniauthTenantService;
    }

    // 租户保存接口统一承接新增与修改租户资料。
    @PostMapping
    public CommonResponse<UniauthTenantItemOut> saveTenant(@RequestBody UniauthTenantSaveIn saveIn, HttpServletRequest request) {
        // 保存成功后直接返回正式落库结果，供前端刷新租户列表。
        return CommonResponse.success(uniauthTenantService.saveTenant(UniauthCurrentUserContext.requireUser(), saveIn, request.getRequestURI()));
    }
}
