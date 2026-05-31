package com.sp.selfsp.uniauth.user.controller;

import com.sp.selfsp.common.util.CommonResponse;
import com.sp.selfsp.uniauth.common.UniauthCurrentUserContext;
import com.sp.selfsp.uniauth.user.domain.in.UniauthUserSaveIn;
import com.sp.selfsp.uniauth.user.domain.out.UniauthUserItemOut;
import com.sp.selfsp.uniauth.user.service.UniauthUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 用户控制器只暴露账号维护接口，不再混入角色、菜单和租户动作。
@RestController
@RequestMapping("/api/uniauth/users")
public class UniauthUserController {

    // 用户服务负责账号保存、角色关系重建和审计日志。
    private final UniauthUserService uniauthUserService;

    // 构造用户控制器时注入用户服务。
    public UniauthUserController(UniauthUserService uniauthUserService) {
        // 保存用户服务，供保存接口复用。
        this.uniauthUserService = uniauthUserService;
    }

    // 用户保存接口统一承接新增与修改账号资料。
    @PostMapping
    public CommonResponse<UniauthUserItemOut> saveUser(@RequestBody UniauthUserSaveIn saveIn, HttpServletRequest request) {
        // 保存成功后直接返回正式账号结果，供前端刷新用户表格。
        return CommonResponse.success(uniauthUserService.saveUser(UniauthCurrentUserContext.requireUser(), saveIn, request.getRequestURI()));
    }
}
