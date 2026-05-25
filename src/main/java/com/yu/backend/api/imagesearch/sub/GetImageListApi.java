package com.yu.backend.api.imagesearch.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yu.backend.api.imagesearch.model.ImageSearchResult;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 拉取百度以图搜图结果列表。
 * <p>注意：接口返回 JSON 使用 {@code thumbURL}/{@code fromURL} 等键名，与 Java 驼峰不一致，
 * 不能用 {@link JSONUtil#toList(JSONArray, Class)} 直接反序列化到 {@link ImageSearchResult}，否则预览地址全为空。</p>
 */
@Slf4j
public final class GetImageListApi {

    private GetImageListApi() {
    }

    /**
     * 获取图片列表
     *
     * @param url 百度 ajax 列表地址（由 {@link GetImageFirstUrlApi} 等步骤得到）
     */
    public static List<ImageSearchResult> getImageList(String url) {
        try {
            HttpResponse response = HttpUtil.createGet(url).execute();
            int statusCode = response.getStatus();
            String body = response.body();
            if (statusCode != 200) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            return processResponse(body);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取图片列表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
    }

    private static List<ImageSearchResult> processResponse(String responseBody) {
        JSONObject root = JSONUtil.parseObj(responseBody);
        if (!root.containsKey("data")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONObject data = root.getJSONObject("data");
        if (data == null || !data.containsKey("list")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONArray list = data.getJSONArray("list");
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<ImageSearchResult> out = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            JSONObject item = list.getJSONObject(i);
            if (item == null) {
                continue;
            }
            out.add(mapBaiduItem(item));
        }
        return out;
    }

    /**
     * 百度返回字段名多为 {@code thumbURL}、{@code fromURL}、{@code objURL} 等，与实体驼峰不一致，需显式取值。
     */
    private static ImageSearchResult mapBaiduItem(JSONObject item) {
        ImageSearchResult r = new ImageSearchResult();
        r.setThumbnailUrl(firstNonBlank(item,
                "thumbURL", "thumbUrl", "thumbnailUrl", "replaceUrl", "middleURL", "middleUrl", "objURL", "objUrl"));
        r.setFromUrl(firstNonBlank(item, "fromURL", "fromUrl", "from_url", "pageURL", "pageUrl"));
        return r;
    }

    private static String firstNonBlank(JSONObject o, String... keys) {
        for (String k : keys) {
            String v = o.getStr(k);
            if (StrUtil.isNotBlank(v)) {
                return v;
            }
        }
        return null;
    }
}
