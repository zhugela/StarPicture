package com.yu.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 图片各地址封装（存库 JSON，与 {@link Picture} 同级）
 *
 * @author <a href="https://github.com/lieeew">leikooo</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Urls implements Serializable {

    private static final long serialVersionUID = 1L;

    private String originalUrl;

    private String url;

    private String thumbnailUrl;

    private String transferUrl;
}
