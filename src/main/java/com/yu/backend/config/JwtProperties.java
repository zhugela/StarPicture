package com.yu.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 登录令牌配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    private String secret = "star-picture-jwt-secret-change-me";

    private int expireDays = 30;
}
