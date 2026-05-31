package com.sp.selfsp.uniauth.auth.controller;

import com.sp.selfsp.common.util.CommonResponse;
import com.sp.selfsp.uniauth.auth.service.UniauthAuthService;
import com.sp.selfsp.uniauth.common.UniauthCurrentUserContext;
import com.sp.selfsp.uniauth.auth.domain.in.UniauthLoginIn;
import com.sp.selfsp.uniauth.auth.domain.out.UniauthLoginOut;
import com.sp.selfsp.uniauth.common.domain.out.UniauthCurrentUserOut;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 认证控制器只暴露登录和当前用户快照，不再承担管理工作台与保存动作。
@RestController
@RequestMapping("/api/uniauth/auth")
public class UniauthAuthController {

    // 认证服务负责登录校验、token 签发和快照返回。
    private final UniauthAuthService uniauthAuthService;

    // 构造认证控制器时注入认证服务。
    public UniauthAuthController(UniauthAuthService uniauthAuthService) {
        // 保存认证服务，供 login 和 me 两个接口复用。
        this.uniauthAuthService = uniauthAuthService;
    }

    // 登录接口负责把账号密码交换为 access token 与首屏工作台数据。
    @PostMapping("/login")
    public CommonResponse<UniauthLoginOut> login(@RequestBody UniauthLoginIn loginIn, HttpServletRequest request) {
        // 登录成功后直接返回 token、当前用户和首屏工作台，减少前端额外初始化请求。
        return CommonResponse.success(uniauthAuthService.login(loginIn, request.getRequestURI(), request.getRemoteAddr()));
    }

    // me 接口负责在刷新页面后回显当前 JWT 已解析出的用户快照。
    @GetMapping("/me")
    public CommonResponse<UniauthCurrentUserOut> me() {
        // 当前接口要求先有有效登录态，再返回当前用户快照。
        return CommonResponse.success(uniauthAuthService.getCurrentUserSnapshot(UniauthCurrentUserContext.requireUser()));
    }
}
