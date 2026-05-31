package com.sp.selfsp.uniauth.role.controller;

import com.sp.selfsp.common.util.CommonResponse;
import com.sp.selfsp.uniauth.common.UniauthCurrentUserContext;
import com.sp.selfsp.uniauth.role.domain.in.UniauthRoleSaveIn;
import com.sp.selfsp.uniauth.role.domain.out.UniauthRoleItemOut;
import com.sp.selfsp.uniauth.role.service.UniauthRoleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 角色控制器只暴露角色维护接口，不再混入用户、菜单和工作台动作。
@RestController
@RequestMapping("/api/uniauth/roles")
public class UniauthRoleController {

    // 角色服务负责角色保存、授权关系重建和审计日志。
    private final UniauthRoleService uniauthRoleService;

    // 构造角色控制器时注入角色服务。
    public UniauthRoleController(UniauthRoleService uniauthRoleService) {
        // 保存角色服务，供保存接口复用。
        this.uniauthRoleService = uniauthRoleService;
    }

    // 角色保存接口统一承接新增与修改角色。
    @PostMapping
    public CommonResponse<UniauthRoleItemOut> saveRole(@RequestBody UniauthRoleSaveIn saveIn, HttpServletRequest request) {
        // 保存成功后直接返回正式角色结果，供前端刷新角色列表。
        return CommonResponse.success(uniauthRoleService.saveRole(UniauthCurrentUserContext.requireUser(), saveIn, request.getRequestURI()));
    }
}
