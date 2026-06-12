package com.yu.backend.model.dto.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 以图搜图请求
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 空间 id（分表路由键）
     */
    private Long spaceId;
}
