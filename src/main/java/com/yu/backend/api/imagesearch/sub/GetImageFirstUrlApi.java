package com.yu.backend.api.imagesearch.sub;

import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 第二步：从百度图搜中间页 HTML 中解析 {@code firstUrl}（ajax 列表入口）
 */
@Slf4j
public final class GetImageFirstUrlApi {

    private GetImageFirstUrlApi() {
    }

    /**
     * 获取图片列表页面地址（ajax URL）
     */
    public static String getImageFirstUrl(String url) {
        try {
            Document document = Jsoup.connect(url)
                    .timeout(5000)
                    .get();

            Elements scriptElements = document.getElementsByTag("script");
            for (Element script : scriptElements) {
                String scriptContent = script.html();
                if (scriptContent.contains("\"firstUrl\"")) {
                    Pattern pattern = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
                    Matcher matcher = pattern.matcher(scriptContent);
                    if (matcher.find()) {
                        String firstUrl = matcher.group(1);
                        return firstUrl.replace("\\/", "/");
                    }
                }
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到 url");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }
}
