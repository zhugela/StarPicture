package com.yu.backend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间使用分析结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUsageAnalyzeResponse implements Serializable {

    /**
     * 已使用大小（字节）
     */
    private Long usedSize;

    /**
     * 总大小上限（字节）
     */
    private Long maxSize;

    /**
     * 空间使用比例（%）
     */
    private Double sizeUsageRatio;

    /**
     * 当前图片数量
     */
    private Long usedCount;

    /**
     * 最大图片数量
     */
    private Long maxCount;

    /**
     * 图片数量占比（%）
     */
    private Double countUsageRatio;

    private static final long serialVersionUID = 1L;
}
