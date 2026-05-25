package com.yu.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 腾讯云 COS 配置（与 {@link CosClientConfiguration} 分离，避免与 {@code @Bean} 同类时绑定顺序异常）
 */
@Data
@Component
@ConfigurationProperties(prefix = "cos.client")
public class CosClientProperties {

    private String host;

    private String secretId;

    private String secretKey;

    private String region;

    private String bucket;

    /**
     * 为 true 时仅普通 PUT，不附加数据万象 PicOperations
     */
    private Boolean simpleUploadOnly = Boolean.FALSE;
}
