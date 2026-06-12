package com.yu.backend.model.dto.space;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间图片大小分析请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceSizeAnalyzeRequest extends SpaceAnalyzeRequest {

    private static final long serialVersionUID = 1L;
}
