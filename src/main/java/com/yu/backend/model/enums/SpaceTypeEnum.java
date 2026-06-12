package com.yu.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import lombok.Getter;

/**
 * 空间类型
 */
@Getter
public enum SpaceTypeEnum {

    PRIVATE("私有空间", 0),
    TEAM("团队空间", 1);

    private final String text;

    private final int value;

    SpaceTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static SpaceTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()) {
            if (spaceTypeEnum.value == value) {
                return spaceTypeEnum;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不存在");
    }
}
