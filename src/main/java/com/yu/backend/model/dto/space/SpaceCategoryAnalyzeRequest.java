package com.yu.backend.model.dto.space;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间分类分析请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceCategoryAnalyzeRequest extends SpaceAnalyzeRequest {

    private static final long serialVersionUID = 1L;
}
