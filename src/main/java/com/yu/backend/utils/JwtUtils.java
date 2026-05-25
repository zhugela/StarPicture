package com.yu.backend.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 小程序 Bearer Token（Hutool JWT）
 */
public final class JwtUtils {

    private static final String CLAIM_USER_ID = "userId";

    private JwtUtils() {
    }

    public static String createToken(Long userId, String secret, int expireDays) {
        long expireAt = System.currentTimeMillis() + (long) expireDays * 24 * 60 * 60 * 1000;
        Map<String, Object> payload = new HashMap<>(4);
        payload.put(CLAIM_USER_ID, userId);
        payload.put("exp", expireAt);
        return JWTUtil.createToken(payload, secret.getBytes(StandardCharsets.UTF_8));
    }

    public static Long getUserId(String token, String secret) {
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        JWT jwt = JWTUtil.parseToken(token);
        if (!jwt.setKey(secret.getBytes(StandardCharsets.UTF_8)).verify()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录已失效");
        }
        Object expObj = jwt.getPayload("exp");
        if (expObj != null) {
            long exp = Long.parseLong(expObj.toString());
            if (System.currentTimeMillis() > exp) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录已过期");
            }
        }
        Object userIdObj = jwt.getPayload(CLAIM_USER_ID);
        if (userIdObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return Long.parseLong(userIdObj.toString());
    }
}
