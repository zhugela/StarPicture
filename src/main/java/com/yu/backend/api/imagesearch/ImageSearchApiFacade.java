package com.yu.backend.api.imagesearch;

import com.yu.backend.api.imagesearch.model.ImageSearchResult;
import com.yu.backend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.yu.backend.api.imagesearch.sub.GetImageListApi;
import com.yu.backend.api.imagesearch.sub.GetImagePageUrlApi;

import java.util.List;

/**
 * 门面：将百度以图搜图的多步 HTTP 封装为单一入口。
 */
public final class ImageSearchApiFacade {

    private ImageSearchApiFacade() {
    }

    /**
     * 搜索图片
     *
     * @param imageUrl 公网可访问的图片地址
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        return GetImageListApi.getImageList(imageFirstUrl);
    }
}
