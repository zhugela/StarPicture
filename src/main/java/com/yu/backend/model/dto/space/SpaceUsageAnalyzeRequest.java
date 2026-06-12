package com.yu.backend.model.dto.space;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间使用分析请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUsageAnalyzeRequest extends SpaceAnalyzeRequest {

    private static final long serialVersionUID = 1L;
}
