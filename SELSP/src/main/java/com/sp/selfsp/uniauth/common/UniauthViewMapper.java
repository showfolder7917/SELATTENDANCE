package com.sp.selfsp.uniauth.common;

import com.sp.selfsp.uniauth.common.domain.out.UniauthCurrentUserOut;
import org.springframework.stereotype.Component;

// 视图映射器统一把当前用户上下文转成明确输出对象，避免多个子域继续各自拼 Map 返回结构。
@Component
public class UniauthViewMapper {

    // 当前用户映射统一暴露标准字段，供权限中心前端和 attendance 宿主直接消费。
    public UniauthCurrentUserOut toCurrentUserOut(UniauthCurrentUser currentUser) {
        // 创建标准当前用户输出对象，承接登录态与宿主桥接所需字段。
        UniauthCurrentUserOut currentUserOut = new UniauthCurrentUserOut();
        // 用户主键用于宿主写操作记录操作人。
        currentUserOut.setUserId(currentUser.getUserId());
        // 账号名用于联调、日志和页面头部识别当前登录者。
        currentUserOut.setUsername(currentUser.getUsername());
        // 展示名用于中日双语界面的更友好显示。
        currentUserOut.setDisplayName(currentUser.getDisplayName());
        // 语言偏好决定前端默认用中文还是日文文案。
        currentUserOut.setLocale(currentUser.getLocale());
        // 租户主键用于业务系统数据隔离。
        currentUserOut.setTenantId(currentUser.getTenantId());
        // 租户编码用于跨系统展示与日志关联。
        currentUserOut.setTenantCode(currentUser.getTenantCode());
        // 租户状态让宿主可以在租户停用时快速阻断访问。
        currentUserOut.setTenantStatus(currentUser.getTenantStatus());
        // 权限码列表供按钮显隐和接口联调确认当前授权结果。
        currentUserOut.setPermissionCodes(currentUser.getPermissionCodes());
        // 菜单码列表供宿主动态决定哪些工程入口和导航可见。
        currentUserOut.setMenuCodes(currentUser.getMenuCodes());
        // 数据范围列表供宿主后续叠加本人、部门或租户级过滤。
        currentUserOut.setDataScopes(currentUser.getDataScopes());
        return currentUserOut;
    }
}
