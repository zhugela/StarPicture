package com.yu.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信公众号（服务号/订阅号）配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.mp")
public class WxMpProperties {

    /** 是否启用公众号回调 */
    private boolean enabled = true;

    /** 公众号 AppID */
    private String appId;

    /** 公众号 AppSecret */
    private String appSecret;

    /**
     * 服务器配置 Token（与公众平台「基本配置」一致）
     */
    private String token = "starpicture";

    /**
     * 消息加解密密钥（明文模式可留空）
     */
    private String encodingAesKey;

    /**
     * 关注后自动回复
     */
    private String subscribeReply = "欢迎关注内娱图库！\n\n回复「图库」浏览精选图片\n回复「帮助」查看使用说明。";

    /**
     * 未匹配关键词时的默认回复
     */
    private String defaultReply = "感谢留言！回复「图库」或「帮助」获取更多信息。";

    /**
     * 关键词自动回复（key 不区分大小写时可自行扩展）
     */
    private Map<String, String> keywordReply = defaultKeywords();

    private static Map<String, String> defaultKeywords() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("图库", "请访问内娱图库网站，浏览已通过审核的公开图片。");
        map.put("帮助", "【内娱图库】\n1. 网站注册登录后可上传、管理私人空间\n2. 菜单「使用帮助」→ 快捷说明");
        map.put("上传", "请登录网站，在「上传」页选择图片即可。");
        return map;
    }
}
