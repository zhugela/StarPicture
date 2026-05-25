package com.yu.backend.api.imagesearch.model;

import lombok.Data;

/**
 * 百度以图搜图列表单项（字段名与 {@link com.yu.backend.api.imagesearch.sub.GetImageListApi} 中 JSON 映射一致）
 */
@Data
public class ImageSearchResult {

    /**
     * 缩略图地址（展示预览用）
     */
    private String thumbnailUrl;

    /**
     * 来源页面地址
     */
    private String fromUrl;
}
