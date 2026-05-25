package com.yu.backend.wx.mp;

import cn.hutool.crypto.digest.DigestUtil;

import java.util.Arrays;

/**
 * 微信公众号服务器签名校验
 */
public final class WxMpSignUtils {

    private WxMpSignUtils() {
    }

    public static boolean checkSignature(String token, String signature, String timestamp, String nonce) {
        if (token == null || signature == null || timestamp == null || nonce == null) {
            return false;
        }
        String[] arr = new String[]{token, timestamp, nonce};
        Arrays.sort(arr);
        String raw = arr[0] + arr[1] + arr[2];
        String sha1 = DigestUtil.sha1Hex(raw);
        return sha1.equalsIgnoreCase(signature);
    }
}
