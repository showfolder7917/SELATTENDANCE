package com.sp.selfsp.uniauth.common;

// ThreadLocal 上下文负责把当前请求解析出的用户身份带到 controller 和 service，避免层层透传参数。
public final class UniauthCurrentUserContext {

    // 每个请求线程独立保存当前用户，避免并发请求之间相互串租户和权限数据。
    private static final ThreadLocal<UniauthCurrentUser> CURRENT_USER = new ThreadLocal<>();

    // 工具类不允许被实例化，避免误当成普通状态对象持有。
    private UniauthCurrentUserContext() {
    }

    // 写入当前请求用户，让后续业务层可以直接读取宿主身份。
    public static void set(UniauthCurrentUser currentUser) {
        CURRENT_USER.set(currentUser);
    }

    // 读取当前请求用户，供可选身份场景自行判断是否已登录。
    public static UniauthCurrentUser get() {
        return CURRENT_USER.get();
    }

    // 强制读取当前登录用户，供必须登录的接口快速失败。
    public static UniauthCurrentUser requireUser() {
        // 先拿当前线程上下文，避免每个接口重复手动判空。
        UniauthCurrentUser currentUser = CURRENT_USER.get();
        // 没有上下文就说明当前请求未经过有效登录态校验。
        if (currentUser == null) {
            throw new IllegalArgumentException("当前请求未登录或登录态已失效");
        }
        return currentUser;
    }

    // 在请求结束时主动清理 ThreadLocal，避免线程复用时把上一位用户串到下一次请求。
    public static void clear() {
        CURRENT_USER.remove();
    }
}
