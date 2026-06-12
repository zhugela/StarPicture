package com.yu.backend.constant;

/**
 * 图片表分片相关常量
 */
public final class PictureConstant {

    /**
     * 公共图库分片键（原 spaceId IS NULL）
     */
    public static final Long PUBLIC_SPACE_ID = 0L;

    private PictureConstant() {
    }

    public static boolean isPublicSpace(Long spaceId) {
        return spaceId == null || PUBLIC_SPACE_ID.equals(spaceId);
    }

    public static Long normalizeSpaceId(Long spaceId) {
        return spaceId == null ? PUBLIC_SPACE_ID : spaceId;
    }
}
