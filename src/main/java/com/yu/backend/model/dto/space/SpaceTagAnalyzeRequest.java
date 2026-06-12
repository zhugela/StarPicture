package com.yu.backend.model.dto.space;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间标签分析请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceTagAnalyzeRequest extends SpaceAnalyzeRequest {

    private static final long serialVersionUID = 1L;
}
