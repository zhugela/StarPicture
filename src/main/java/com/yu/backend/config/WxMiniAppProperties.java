package com.yu.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置（appId / secret 用于 code2session，tokenSecret 用于 JWT）
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxMiniAppProperties {

    private String appId;

    private String appSecret;

    private String tokenSecret = "star-picture-jwt-secret-change-me";

    private int tokenExpireDays = 30;
}
