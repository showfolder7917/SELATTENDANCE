package com.sp.selfsp.uniauth.menu.controller;

import com.sp.selfsp.common.util.CommonResponse;
import com.sp.selfsp.uniauth.common.UniauthCurrentUserContext;
import com.sp.selfsp.uniauth.menu.domain.in.UniauthMenuSaveIn;
import com.sp.selfsp.uniauth.menu.domain.out.UniauthMenuItemOut;
import com.sp.selfsp.uniauth.menu.service.UniauthMenuService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 菜单控制器只暴露菜单树维护接口，不再混入用户、角色和工作台动作。
@RestController
@RequestMapping("/api/uniauth/menus")
public class UniauthMenuController {

    // 菜单服务负责菜单节点保存和审计日志。
    private final UniauthMenuService uniauthMenuService;

    // 构造菜单控制器时注入菜单服务。
    public UniauthMenuController(UniauthMenuService uniauthMenuService) {
        // 保存菜单服务，供保存接口复用。
        this.uniauthMenuService = uniauthMenuService;
    }

    // 菜单保存接口统一承接新增与修改菜单节点。
    @PostMapping
    public CommonResponse<UniauthMenuItemOut> saveMenu(@RequestBody UniauthMenuSaveIn saveIn, HttpServletRequest request) {
        // 保存成功后直接返回正式节点结果，供前端重绘菜单树。
        return CommonResponse.success(uniauthMenuService.saveMenu(UniauthCurrentUserContext.requireUser(), saveIn, request.getRequestURI()));
    }
}
