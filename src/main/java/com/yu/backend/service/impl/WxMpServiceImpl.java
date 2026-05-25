package com.yu.backend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yu.backend.config.WxMiniAppProperties;
import com.yu.backend.config.WxMpProperties;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.service.WxMpService;
import com.yu.backend.wx.mp.WxMpSignUtils;
import com.yu.backend.wx.mp.WxMpXmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class WxMpServiceImpl implements WxMpService {

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String MENU_CREATE_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=";

    @Resource
    private WxMpProperties wxMpProperties;

    @Resource
    private WxMiniAppProperties wxMiniAppProperties;

  /** 简单内存缓存 access_token */
    private final AtomicReference<CachedToken> tokenCache = new AtomicReference<>();

    @Override
    public String verifyPortal(String signature, String timestamp, String nonce, String echostr) {
        ThrowUtils.throwIf(!wxMpProperties.isEnabled(), ErrorCode.OPERATION_ERROR, "公众号功能未启用");
        boolean ok = WxMpSignUtils.checkSignature(wxMpProperties.getToken(), signature, timestamp, nonce);
        ThrowUtils.throwIf(!ok, ErrorCode.PARAMS_ERROR, "签名校验失败");
        return echostr;
    }

    @Override
    public String handleMessage(String requestBody, String signature, String timestamp, String nonce) {
        if (!wxMpProperties.isEnabled() || StrUtil.isBlank(requestBody)) {
            return "";
        }
        if (!WxMpSignUtils.checkSignature(wxMpProperties.getToken(), signature, timestamp, nonce)) {
            log.warn("公众号消息签名校验失败");
            return "";
        }
        Map<String, String> msg = WxMpXmlUtils.parseIncomingXml(requestBody);
        String msgType = msg.get("MsgType");
        String fromUser = msg.get("FromUserName");
        String toUser = msg.get("ToUserName");
        if (StrUtil.hasBlank(msgType, fromUser, toUser)) {
            return "";
        }

        // 事件：关注、菜单点击等
        if ("event".equalsIgnoreCase(msgType)) {
            String event = msg.get("Event");
            if ("subscribe".equalsIgnoreCase(event)) {
                return WxMpXmlUtils.buildTextReply(fromUser, toUser, wxMpProperties.getSubscribeReply());
            }
            if ("CLICK".equalsIgnoreCase(event)) {
                return replyMenuClick(fromUser, toUser, msg.get("EventKey"));
            }
            return "";
        }

        // 文本消息：关键词自动回复
        if ("text".equalsIgnoreCase(msgType)) {
            String content = StrUtil.trim(msg.get("Content"));
            return WxMpXmlUtils.buildTextReply(fromUser, toUser, matchKeywordReply(content));
        }

        return "";
    }

    private String replyMenuClick(String fromUser, String toUser, String eventKey) {
        if ("HELP".equalsIgnoreCase(eventKey)) {
            return WxMpXmlUtils.buildTextReply(fromUser, toUser,
                    wxMpProperties.getKeywordReply().getOrDefault("帮助", wxMpProperties.getDefaultReply()));
        }
        if ("GALLERY".equalsIgnoreCase(eventKey)) {
            return WxMpXmlUtils.buildTextReply(fromUser, toUser,
                    wxMpProperties.getKeywordReply().getOrDefault("图库", wxMpProperties.getDefaultReply()));
        }
        return WxMpXmlUtils.buildTextReply(fromUser, toUser, wxMpProperties.getDefaultReply());
    }

    private String matchKeywordReply(String content) {
        if (StrUtil.isBlank(content)) {
            return wxMpProperties.getDefaultReply();
        }
        Map<String, String> rules = wxMpProperties.getKeywordReply();
        for (Map.Entry<String, String> entry : rules.entrySet()) {
            if (content.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return wxMpProperties.getDefaultReply();
    }

    @Override
    public void createDefaultMenu() {
        String accessToken = getAccessToken();
        String miniAppId = wxMiniAppProperties.getAppId();
        ThrowUtils.throwIf(StrUtil.isBlank(miniAppId), ErrorCode.OPERATION_ERROR, "未配置小程序 appId，无法创建含小程序入口的菜单");

        JSONObject menu = JSONUtil.createObj();
        menu.set("button", JSONUtil.createArray()
                .put(JSONUtil.createObj()
                        .set("type", "miniprogram")
                        .set("name", "打开图库")
                        .set("url", "http://mp.weixin.qq.com")
                        .set("appid", miniAppId)
                        .set("pagepath", "pages/index/index"))
                .put(JSONUtil.createObj()
                        .set("name", "功能")
                        .set("sub_button", JSONUtil.createArray()
                                .put(JSONUtil.createObj()
                                        .set("type", "click")
                                        .set("name", "使用帮助")
                                        .set("key", "HELP"))
                                .put(JSONUtil.createObj()
                                        .set("type", "click")
                                        .set("name", "图库说明")
                                        .set("key", "GALLERY"))
                                .put(JSONUtil.createObj()
                                        .set("type", "miniprogram")
                                        .set("name", "上传图片")
                                        .set("url", "http://mp.weixin.qq.com")
                                        .set("appid", miniAppId)
                                        .set("pagepath", "pages/upload/upload")))));

        String url = MENU_CREATE_URL + accessToken;
        String resp = HttpUtil.post(url, menu.toString());
        JSONObject result = JSONUtil.parseObj(resp);
        Integer errcode = result.getInt("errcode", 0);
        if (errcode != null && errcode != 0) {
            log.error("创建公众号菜单失败: {}", resp);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建菜单失败: " + result.getStr("errmsg"));
        }
        log.info("公众号自定义菜单创建成功");
    }

    private String getAccessToken() {
        CachedToken cached = tokenCache.get();
        long now = System.currentTimeMillis();
        if (cached != null && cached.expireAt > now + 60_000) {
            return cached.token;
        }
        ThrowUtils.throwIf(StrUtil.hasBlank(wxMpProperties.getAppId(), wxMpProperties.getAppSecret()),
                ErrorCode.OPERATION_ERROR, "请配置 wx.mp.app-id 与 app-secret");

        String url = TOKEN_URL + "?grant_type=client_credential"
                + "&appid=" + wxMpProperties.getAppId()
                + "&secret=" + wxMpProperties.getAppSecret();
        String resp = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(resp);
        if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 access_token 失败: " + json.getStr("errmsg"));
        }
        String token = json.getStr("access_token");
        int expiresIn = json.getInt("expires_in", 7200);
        tokenCache.set(new CachedToken(token, now + expiresIn * 1000L));
        return token;
    }

    private static final class CachedToken {
        private final String token;
        private final long expireAt;

        private CachedToken(String token, long expireAt) {
            this.token = token;
            this.expireAt = expireAt;
        }
    }
}
