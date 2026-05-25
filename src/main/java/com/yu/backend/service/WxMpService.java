package com.yu.backend.service;

/**
 * 微信公众号：access_token、菜单、消息回复
 */
public interface WxMpService {

    /**
     * 校验 URL 并返回 echostr
     */
    String verifyPortal(String signature, String timestamp, String nonce, String echostr);

    /**
     * 处理公众平台 POST 消息/事件，返回 XML 或空串
     */
    String handleMessage(String requestBody, String signature, String timestamp, String nonce);

    /**
     * 创建默认自定义菜单（含跳转小程序）
     */
    void createDefaultMenu();
}
