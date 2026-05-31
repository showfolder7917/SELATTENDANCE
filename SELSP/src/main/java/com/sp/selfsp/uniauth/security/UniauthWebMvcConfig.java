package com.sp.selfsp.uniauth.security;

import com.sp.selfsp.uniauth.common.UniauthCurrentUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// WebMvc 配置把轻量认证拦截器挂到宿主和权限中心接口上，保证 JWT 能在进入控制器前完成解析。
@Component
public class UniauthWebMvcConfig implements WebMvcConfigurer {

    // JWT 支撑类负责解析 Authorization 里的 bearer token。
    private final UniauthJwtSupport uniauthJwtSupport;

    // 构造配置器，把 JWT 解析器注入给内部拦截器使用。
    public UniauthWebMvcConfig(UniauthJwtSupport uniauthJwtSupport) {
        // 保存 JWT 依赖，供请求拦截阶段统一还原当前用户上下文。
        this.uniauthJwtSupport = uniauthJwtSupport;
    }

    // 注册权限中心拦截器，让 `/api/uniauth/**` 和宿主 `/api/attendance/host/**` 都能消费 bearer token。
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 这里不强制所有 attendance 旧接口必须登录，只先覆盖权限中心自身和宿主上下文入口。
        registry.addInterceptor(new HandlerInterceptor() {
            // 请求进入控制器前先尝试解析 token，把用户身份装进 ThreadLocal 上下文。
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                // 每次新请求都先清理旧线程上下文，避免线程复用时串用户。
                UniauthCurrentUserContext.clear();
                // 读取标准 Authorization 头，沿用 Bearer Token 口径。
                String authorization = request.getHeader("Authorization");
                // 没有 bearer token 时直接放行，兼容当前仍未强制鉴权的旧 attendance 接口。
                if (authorization == null || !authorization.startsWith("Bearer ")) {
                    return true;
                }
                // 解析掉 Bearer 前缀，只把真正令牌内容交给 JWT 支撑类。
                String accessToken = authorization.substring("Bearer ".length()).trim();
                // 空 token 视为无效登录态，直接让后续 requireUser 时失败。
                if (accessToken.isEmpty()) {
                    return true;
                }
                // 解析成功后把当前用户身份写进线程上下文，供 controller 和 service 直接读取。
                UniauthCurrentUserContext.set(uniauthJwtSupport.parseAccessToken(accessToken));
                return true;
            }

            // 请求结束后无论成功失败都清理上下文，防止线程池复用引发身份串线。
            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
                // 主动移除 ThreadLocal，确保下一次请求拿到干净上下文。
                UniauthCurrentUserContext.clear();
            }
        })
        // 权限中心接口天然需要能消费登录态。
        .addPathPatterns("/api/uniauth/**")
        // attendance 目前先只把宿主上下文和权限桥接入口接入 JWT 消费链。
        .addPathPatterns("/api/attendance/host/**");
    }
}
