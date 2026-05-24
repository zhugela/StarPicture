package com.yu.backend.model.dto.picture;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片编辑请求
 */
@Data
public class PictureEditRequest implements Serializable {

    /**
     * id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 空间 id（为空表示公共空间）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
