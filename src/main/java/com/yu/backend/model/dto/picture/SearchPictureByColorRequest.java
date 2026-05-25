package com.yu.backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 按颜色检索空间内图片
 */
@Data
public class SearchPictureByColorRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片主色调（十六进制，可与 COS Ave 一致）
     */
    private String picColor;

    /**
     * 空间 id
     */
    private Long spaceId;
}
