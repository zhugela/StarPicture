package com.yu.backend.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;

import java.util.Arrays;

/**
 * 微信公众号服务器签名校验
 */
public final class WxMpSignUtils {

    private WxMpSignUtils() {
    }

    public static boolean checkSignature(String token, String signature, String timestamp, String nonce) {
        if (StrUtil.hasBlank(token, signature, timestamp, nonce)) {
            return false;
        }
        String[] arr = {token, timestamp, nonce};
        Arrays.sort(arr);
        String content = arr[0] + arr[1] + arr[2];
        String sha1 = DigestUtil.sha1Hex(content);
        return sha1.equalsIgnoreCase(signature);
    }
}
