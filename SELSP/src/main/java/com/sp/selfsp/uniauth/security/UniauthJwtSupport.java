package com.sp.selfsp.uniauth.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.selfsp.uniauth.common.UniauthCurrentUser;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 轻量 JWT 支撑类负责生成和校验第九阶段最小闭环所需的 token，不引入额外安全框架也能先跑通接入链。
@Component
public class UniauthJwtSupport {

    // 统一复用 Jackson 序列化器，保证 header 和 payload 的 JSON 口径稳定。
    private final ObjectMapper objectMapper;
    // HMAC 密钥由配置项注入，避免把签名秘钥写死在代码里。
    private final String jwtSecret;
    // 过期秒数用于控制 access token 生命周期。
    private final long expireSeconds;

    // 构造 JWT 支撑类，把签名和 JSON 依赖统一注入进来。
    public UniauthJwtSupport(
        ObjectMapper objectMapper,
        @Value("${uniauth.jwt.secret}") String jwtSecret,
        @Value("${uniauth.jwt.expire-seconds}") long expireSeconds
    ) {
        // 复用应用里统一的 ObjectMapper，避免 token 里的字段命名和接口 JSON 漂移。
        this.objectMapper = objectMapper;
        // 保存签名密钥，供生成和校验 token 时共同使用。
        this.jwtSecret = jwtSecret;
        // 保存过期秒数，便于按配置调整登录态有效期。
        this.expireSeconds = expireSeconds;
    }

    // 为当前登录用户签发 access token，直接把宿主需要的权限和租户上下文放进 payload。
    public String createAccessToken(UniauthCurrentUser currentUser) {
        try {
            // JWT header 固定声明当前使用 HS256，便于后续解析阶段校验算法口径。
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            // payload 把用户、权限、菜单和数据范围一次性打包给宿主消费。
            Map<String, Object> payload = new LinkedHashMap<>();
            // 用户主键是宿主审计和操作人写回的基础。
            payload.put("userId", currentUser.getUserId());
            // 登录名是宿主识别当前用户的稳定账号字段。
            payload.put("username", currentUser.getUsername());
            // 展示名用于页面右上角和日志友好展示。
            payload.put("displayName", currentUser.getDisplayName());
            // locale 让宿主在首次进入时沿用权限中心侧的语言偏好。
            payload.put("locale", currentUser.getLocale());
            // tenantId 是 attendance 默认的数据硬隔离条件。
            payload.put("tenantId", currentUser.getTenantId());
            // tenantCode 作为跨系统日志和接入方定位租户的稳定键。
            payload.put("tenantCode", currentUser.getTenantCode());
            // tenantStatus 允许宿主快速识别租户是否已经被平台停用。
            payload.put("tenantStatus", currentUser.getTenantStatus());
            // permissionCodes 直接供 controller 和按钮权限消费。
            payload.put("permissionCodes", currentUser.getPermissionCodes());
            // menuCodes 直接供前端动态导航消费。
            payload.put("menuCodes", currentUser.getMenuCodes());
            // dataScopes 直接供 attendance 查询层后续补过滤条件。
            payload.put("dataScopes", currentUser.getDataScopes());
            // iat 记录签发时间，便于问题排查与后续会话治理。
            payload.put("iat", Instant.now().getEpochSecond());
            // exp 控制 token 何时失效，避免长期有效凭证带来安全风险。
            payload.put("exp", Instant.now().plusSeconds(expireSeconds).getEpochSecond());
            // 先把 header 编成 base64url 文本，形成 JWT 第一段。
            String encodedHeader = encodeJson(header);
            // 再把 payload 编成 base64url 文本，形成 JWT 第二段。
            String encodedPayload = encodeJson(payload);
            // 使用 header.payload 计算签名，确保 token 内容被篡改时能被宿主发现。
            String signature = sign(encodedHeader + "." + encodedPayload);
            // 按 JWT 标准顺序返回三段式字符串。
            return encodedHeader + "." + encodedPayload + "." + signature;
        } catch (Exception error) {
            // 签发失败会导致登录态无法建立，这里直接抛业务异常让调用方明确失败。
            throw new IllegalArgumentException("生成登录令牌失败");
        }
    }

    // 解析并校验 access token，把 payload 还原成宿主可直接消费的当前用户上下文。
    public UniauthCurrentUser parseAccessToken(String token) {
        try {
            // 三段式缺失说明不是合法 JWT，直接阻断后续解析。
            String[] segments = token.split("\\.");
            // 只有标准三段式才允许进入签名校验和 payload 反序列化。
            if (segments.length != 3) {
                throw new IllegalArgumentException("登录令牌格式不正确");
            }
            // 重新计算签名并比较，防止 token 被篡改后继续访问业务系统。
            String expectedSignature = sign(segments[0] + "." + segments[1]);
            // 签名不一致说明令牌内容已被改写或秘钥不匹配。
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), segments[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("登录令牌签名无效");
            }
            // 解析 payload JSON，拿回用户身份与权限快照。
            Map<String, Object> payload = objectMapper.readValue(
                Base64.getUrlDecoder().decode(segments[1]),
                new TypeReference<Map<String, Object>>() {
                }
            );
            // 过期时间缺失或已经到期时立即阻断访问。
            long exp = longValue(payload.get("exp"));
            // exp 小于当前秒数时说明登录态已经失效。
            if (exp <= Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("登录令牌已过期");
            }
            // 把 payload 还原成标准上下文对象，供宿主接口和 service 统一读取。
            UniauthCurrentUser currentUser = new UniauthCurrentUser();
            // 用户主键是宿主操作日志和写库操作人的基础字段。
            currentUser.setUserId(longValue(payload.get("userId")));
            // 登录名用于宿主区分当前是谁在操作。
            currentUser.setUsername(stringValue(payload.get("username")));
            // 展示名用于页面和日志友好显示。
            currentUser.setDisplayName(stringValue(payload.get("displayName")));
            // 语言偏好用于宿主默认语言切换。
            currentUser.setLocale(stringValue(payload.get("locale")));
            // tenantId 用于业务数据硬隔离。
            currentUser.setTenantId(longValue(payload.get("tenantId")));
            // tenantCode 便于跨系统日志定位。
            currentUser.setTenantCode(stringValue(payload.get("tenantCode")));
            // tenantStatus 让宿主可识别停用租户。
            currentUser.setTenantStatus(stringValue(payload.get("tenantStatus")));
            // 权限码列表用于接口和按钮权限判断。
            currentUser.setPermissionCodes(stringList(payload.get("permissionCodes")));
            // 菜单码列表用于前端动态渲染导航。
            currentUser.setMenuCodes(stringList(payload.get("menuCodes")));
            // 数据范围列表供 attendance 侧后续叠加过滤条件。
            currentUser.setDataScopes(stringList(payload.get("dataScopes")));
            return currentUser;
        } catch (IllegalArgumentException error) {
            // 已知业务异常直接透传，保证前端能拿到明确登录态失败原因。
            throw error;
        } catch (Exception error) {
            // 其他解析异常统一收口成令牌无效，避免暴露底层实现细节。
            throw new IllegalArgumentException("登录令牌解析失败");
        }
    }

    // 把明文密码统一按 SHA-256 + Base64 摘要，供登录比对和数据种子复用。
    public String hashPassword(String plainPassword) {
        try {
            // SHA-256 足以支撑当前最小闭环的密码摘要需求，后续如有要求再替换为更强方案。
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 把 UTF-8 文本转成固定字节后计算摘要，避免不同平台编码差异。
            byte[] hashedBytes = digest.digest(stringValue(plainPassword).getBytes(StandardCharsets.UTF_8));
            // 用 Base64 存储摘要，便于 SQL 种子和数据库字段直接维护。
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception error) {
            // 摘要失败直接阻断，避免把明文或无效摘要写进数据库。
            throw new IllegalArgumentException("密码摘要计算失败");
        }
    }

    // 统一把对象编码成 base64url JSON，供 JWT header 和 payload 复用。
    private String encodeJson(Map<String, Object> jsonObject) throws Exception {
        // 先序列化成 UTF-8 JSON，再转成 URL 安全的 Base64 文本。
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
            objectMapper.writeValueAsBytes(jsonObject)
        );
    }

    // 使用 HMAC-SHA256 对 JWT 前两段签名，保证宿主能校验令牌完整性。
    private String sign(String signingText) throws Exception {
        // 创建 HS256 所需的 MAC 实例，和 header 中声明的算法保持一致。
        Mac mac = Mac.getInstance("HmacSHA256");
        // 把配置的密钥包装成 HMAC 密钥规格对象。
        SecretKeySpec secretKeySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        // 初始化 HMAC，确保后续 doFinal 使用的是当前应用配置的签名密钥。
        mac.init(secretKeySpec);
        // 计算签名并转成 base64url 文本，形成 JWT 第三段。
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
            mac.doFinal(signingText.getBytes(StandardCharsets.UTF_8))
        );
    }

    // 把 payload 中可能为 Number 或 String 的值统一转成长整型。
    private long longValue(Object rawValue) {
        // 空值直接视为 0，供调用方在必填字段场景继续做显式校验。
        if (rawValue == null) {
            return 0L;
        }
        // Number 直接转 long，避免 JSON 解析类型差异带来重复判断。
        if (rawValue instanceof Number numberValue) {
            return numberValue.longValue();
        }
        // 其他情况按字符串解析，兼容手工种子或跨语言 token 负载。
        return Long.parseLong(String.valueOf(rawValue));
    }

    // 把 payload 中的单值统一转成字符串，减少外层反复判空和 toString。
    private String stringValue(Object rawValue) {
        // 空值统一转空串，避免 controller 再拿到 null 做一轮兜底。
        return rawValue == null ? "" : String.valueOf(rawValue);
    }

    // 把 payload 里的数组字段还原成字符串列表，供权限、菜单和数据范围复用。
    private List<String> stringList(Object rawValue) {
        // 只有真正的列表字段才参与转换，其他情况直接给空列表。
        if (!(rawValue instanceof List<?> rawList)) {
            return List.of();
        }
        // 把任意元素统一转成字符串列表，兼容 JSON 解析后出现的 Object 元素类型。
        return rawList.stream().map(String::valueOf).toList();
    }
}
