package com.yu.backend.context;

import com.yu.backend.model.dto.space.SpaceUserAuthContext;

/**
 * 当前请求的空间权限上下文（ThreadLocal）
 */
public final class SpaceUserAuthContextHolder {

    private static final ThreadLocal<SpaceUserAuthContext> CONTEXT = new ThreadLocal<>();

    private SpaceUserAuthContextHolder() {
    }

    public static void set(String loginId, SpaceUserAuthContext context) {
        CONTEXT.set(context);
    }

    public static SpaceUserAuthContext get(String loginId) {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
