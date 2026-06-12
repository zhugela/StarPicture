package com.yu.backend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间 / 图库分析结果
 */
@Data
public class SpaceAnalyzeVO implements Serializable {

    /**
     * 图片总数
     */
    private long totalCount;

    /**
     * 图片总大小（字节）
     */
    private long totalSize;

    private static final long serialVersionUID = 1L;
}
