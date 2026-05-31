package com.sp.selfsp.attendance.host.controller;

import com.sp.selfsp.common.util.CommonResponse;
import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import com.sp.selfsp.uniauth.common.UniauthCurrentUserContext;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 宿主桥接控制器用于证明 attendance 已能消费 uniauth 登录态，而不是继续把权限中心和业务系统割裂开。
@RestController
@RequestMapping("/api/attendance/host")
public class AttendanceHostController {

    // 上下文接口直接回显当前用户、租户、菜单和权限，供前端宿主和联调测试验证接入是否成功。
    @GetMapping("/context")
    public CommonResponse<Map<String, Object>> context() {
        // 当前接口要求必须带有效 bearer token，才能证明宿主已经成功接入权限中心。
        UniauthCurrentUser currentUser = UniauthCurrentUserContext.requireUser();
        // 用稳定 JSON 结构回显宿主真正能消费到的上下文字段。
        Map<String, Object> contextPayload = new LinkedHashMap<>();
        // 用户主键用于宿主后续写操作记录操作人。
        contextPayload.put("userId", currentUser.getUserId());
        // 展示名用于宿主界面展示当前登录者。
        contextPayload.put("displayName", currentUser.getDisplayName());
        // tenantId 用于宿主后续把业务查询限定在当前租户。
        contextPayload.put("tenantId", currentUser.getTenantId());
        // tenantCode 用于宿主展示当前所在租户。
        contextPayload.put("tenantCode", currentUser.getTenantCode());
        // permissionCodes 供宿主按钮权限消费。
        contextPayload.put("permissionCodes", currentUser.getPermissionCodes());
        // menuCodes 供宿主导航权限消费。
        contextPayload.put("menuCodes", currentUser.getMenuCodes());
        // dataScopes 供宿主后续叠加数据权限过滤。
        contextPayload.put("dataScopes", currentUser.getDataScopes());
        return CommonResponse.success(contextPayload);
    }
}
