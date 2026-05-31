package com.sp.selfsp.uniauth.common;

import java.util.List;

// 统一值工具只负责权限中心内部常见的判空、转型和列表兜底，避免每个子域服务重复堆工具细节。
public final class UniauthValueSupport {

    // 工具类不承载状态，不允许被实例化。
    private UniauthValueSupport() {
    }

    // 文本必填校验统一放在这里，保证各子域抛出的参数错误口径一致。
    public static void requireText(String value, String message) {
        // 只有非空且非全空白文本才允许进入后续业务保存与查询动作。
        if (value == null || value.isBlank()) {
            // 参数不合格时直接阻断，避免后续写出无语义的脏数据。
            throw new IllegalArgumentException(message);
        }
    }

    // 空白文本统一回退默认值，减少各子域保存逻辑里重复写 trim 和判空。
    public static String blankToDefault(String value, String defaultValue) {
        // 有实际内容时保留原值，没有内容时回退到调用方提供的默认业务口径。
        return value == null || value.isBlank() ? defaultValue : value;
    }

    // 数据库结果统一转字符串，避免不同 DAO 返回类型导致服务层散落 toString 细节。
    public static String stringValue(Object value) {
        // 空值统一回空串，方便上层继续做默认值回退与展示兜底。
        return value == null ? "" : String.valueOf(value);
    }

    // 数据库结果统一转 long，兼容 H2 返回 Number 或字符串的两种情况。
    public static long longValue(Object value) {
        // 空值直接回 0，交给上层决定是否需要业务级默认值。
        if (value == null) {
            return 0L;
        }
        // Number 结果直接取 longValue，避免不必要的字符串中转。
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }
        // 其他结果按文本解析成长整型，兼容 MyBatis map 场景。
        return Long.parseLong(String.valueOf(value));
    }

    // 布尔值统一兼容 Boolean、Number 和字符串三种常见数据库返回形式。
    public static boolean booleanValue(Object value) {
        // 布尔类型直接返回真实结果，避免重复转换。
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        // 数字类型以非 0 视为真，兼容 tinyint 标志字段。
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        // 字符串类型兼容 true 和 1 这类常见表达。
        return "true".equalsIgnoreCase(String.valueOf(value)) || "1".equals(String.valueOf(value));
    }

    // 长整型列表统一回退空集合，避免角色和用户关系保存时空指针。
    public static List<Long> nullSafeLongList(List<Long> values) {
        // 传入为空时返回可遍历空集合，让调用方 foreach 保持稳定。
        return values == null ? List.of() : values;
    }

    // 字符串列表统一回退空集合，避免权限码和菜单码处理时多处判空。
    public static List<String> nullSafeStringList(List<String> values) {
        // 传入为空时返回空集合，表达“当前没有任何绑定关系”。
        return values == null ? List.of() : values;
    }
}
