package com.sp.selfsp.uniauth.module.controller;

import com.sp.selfsp.common.util.CommonResponse;
import com.sp.selfsp.uniauth.common.UniauthCurrentUserContext;
import com.sp.selfsp.uniauth.module.domain.in.UniauthModuleSaveIn;
import com.sp.selfsp.uniauth.module.domain.out.UniauthModuleItemOut;
import com.sp.selfsp.uniauth.module.service.UniauthModuleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 模块控制器只承接模块主数据保存协议，不直接拼任何权限或持久化细节。
@RestController
@RequestMapping("/api/uniauth/modules")
public class UniauthModuleController {

    // 模块服务负责模块主数据的真正业务编排。
    private final UniauthModuleService uniauthModuleService;

    // 构造控制器时注入模块服务，保持 controller 只做协议层收口。
    public UniauthModuleController(UniauthModuleService uniauthModuleService) {
        // 保存模块服务，供当前模块保存接口复用。
        this.uniauthModuleService = uniauthModuleService;
    }

    // 模块保存接口统一承接新增和更新动作，让前端模块区块只维护一个提交入口。
    @PostMapping
    public CommonResponse<UniauthModuleItemOut> saveModule(@RequestBody UniauthModuleSaveIn saveIn, HttpServletRequest request) {
        // 当前请求必须先从统一 JWT 上下文恢复操作者，再进入模块保存链路。
        return CommonResponse.success(uniauthModuleService.saveModule(UniauthCurrentUserContext.requireUser(), saveIn, request.getRequestURI()));
    }
}
