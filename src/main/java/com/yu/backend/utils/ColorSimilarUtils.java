package com.yu.backend.utils;

import cn.hutool.core.util.StrUtil;

import java.awt.Color;

/**
 * 颜色相似度（欧氏距离归一化到 0~1，越大越相似）
 */
public final class ColorSimilarUtils {

    private ColorSimilarUtils() {
    }

    /**
     * 解析 COS 均色或前端传入的十六进制串；无前缀时按 RGB 十六进制补 {@code #}。
     */
    public static Color parseAveColor(String raw) {
        if (StrUtil.isBlank(raw)) {
            throw new IllegalArgumentException("blank color");
        }
        String t = raw.trim();
        if (!t.startsWith("#") && !t.startsWith("0x") && !t.startsWith("0X")) {
            t = "#" + t;
        }
        return Color.decode(t);
    }

    public static double calculateSimilarity(Color color1, Color color2) {
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();

        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();

        double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
        return 1 - distance / Math.sqrt(3 * Math.pow(255, 2));
    }

    public static double calculateSimilarity(String hexColor1, String hexColor2) {
        return calculateSimilarity(parseAveColor(hexColor1), parseAveColor(hexColor2));
    }
}
