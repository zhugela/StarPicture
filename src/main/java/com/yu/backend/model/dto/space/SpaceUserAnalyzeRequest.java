package com.yu.backend.model.dto.space;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户上传时间分析请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {

    /**
     * 用户 ID（可选，筛选指定用户上传）
     */
    private Long userId;

    /**
     * 时间维度：day / week / month
     */
    private String timeDimension;

    private static final long serialVersionUID = 1L;
}
