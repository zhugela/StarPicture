package com.yu.backend.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * JWT Bearer Token（HS256，仅使用 JDK 加密实现，避免 BouncyCastle JCE 认证问题）
 */
public final class JwtUtils {

    private static final String CLAIM_USER_ID = "userId";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private JwtUtils() {
    }

    public static String createToken(Long userId, String secret, int expireDays) {
        long expireAt = System.currentTimeMillis() + (long) expireDays * 24 * 60 * 60 * 1000;
        JSONObject payload = new JSONObject();
        payload.set(CLAIM_USER_ID, userId);
        payload.set("exp", expireAt);

        String headerPart = base64UrlEncode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String payloadPart = base64UrlEncode(payload.toString().getBytes(StandardCharsets.UTF_8));
        String signature = sign(headerPart + "." + payloadPart, secret);
        return headerPart + "." + payloadPart + "." + signature;
    }

    public static Long getUserId(String token, String secret) {
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录已失效");
        }
        String signedContent = parts[0] + "." + parts[1];
        String expectedSignature = sign(signedContent, secret);
        if (!expectedSignature.equals(parts[2])) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录已失效");
        }

        String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
        JSONObject payload = JSONUtil.parseObj(payloadJson);
        Long exp = payload.getLong("exp");
        if (exp != null && System.currentTimeMillis() > exp) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录已过期");
        }
        Long userId = payload.getLong(CLAIM_USER_ID);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return userId;
    }

    private static String sign(String content, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return base64UrlEncode(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "JWT 签名失败");
        }
    }

    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static byte[] base64UrlDecode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }
}
