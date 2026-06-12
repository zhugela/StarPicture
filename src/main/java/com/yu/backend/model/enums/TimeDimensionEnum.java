package com.yu.backend.model.enums;

import cn.hutool.core.util.StrUtil;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import lombok.Getter;

/**
 * 分析时间维度
 */
@Getter
public enum TimeDimensionEnum {

    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String value;

    TimeDimensionEnum(String value) {
        this.value = value;
    }

    public static TimeDimensionEnum getEnumByValue(String value) {
        if (StrUtil.isBlank(value)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间维度不能为空");
        }
        for (TimeDimensionEnum timeDimensionEnum : TimeDimensionEnum.values()) {
            if (timeDimensionEnum.value.equals(value)) {
                return timeDimensionEnum;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间维度不存在");
    }
}
