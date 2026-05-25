package com.yu.backend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 {@link COSClient}，依赖已绑定完成的 {@link CosClientProperties}
 */
@Slf4j
@Configuration
public class CosClientConfiguration {

    @Bean
    public COSClient cosClient(CosClientProperties props) {
        String sid = StringUtils.trimToNull(props.getSecretId());
        String sk = StringUtils.trimToNull(props.getSecretKey());
        String reg = StringUtils.trimToNull(props.getRegion());
        if (sid == null || sk == null || reg == null) {
            throw new IllegalStateException(
                    "COS 配置不完整：请检查 application-local.yml（或当前激活 profile）中的 cos.client.secretId、secretKey、region 是否已填写且未被其它配置覆盖");
        }
        COSCredentials cred = new BasicCOSCredentials(sid, sk);
        ClientConfig clientConfig = new ClientConfig(new Region(reg));
        log.info("COSClient 已初始化，region={} bucket={} simpleUploadOnly={}",
                reg,
                StringUtils.defaultString(props.getBucket(), "(未配置)"),
                Boolean.TRUE.equals(props.getSimpleUploadOnly()));
        return new COSClient(cred, clientConfig);
    }
}
