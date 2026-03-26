package com.yu.backend.model.enums;

import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import lombok.Getter;
import lombok.NonNull;
@Getter
public enum FileUploadEnum {
    FILE("file"),

    URL("url");

    private final String type;

    FileUploadEnum(String type) {
        this.type = type;
    }

    private static String getType(@NonNull String type) {
        for(FileUploadEnum fileRoleEnum : FileUploadEnum.values()) {
            if(fileRoleEnum.type.equals(type)) {
                return fileRoleEnum.type;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"不支持上传的类型");
    }
}
