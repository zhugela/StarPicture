package com.yu.backend.model.vo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.Urls;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

@Data
public class PictureVO {
    /**
     * id（JSON 输出为字符串，避免前端精度丢失）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图片访问地址集合
     */
    private Urls urls;

    /**
     * 主图地址（与 urls.url 同步，便于旧前端）
     */
    private String url;

    /**
     * 缩略图地址（与 urls.thumbnailUrl 同步）
     */
    private String thumbnailUrl;

    /**
     * 适合图片编辑器 / Canvas 加载的地址（优先 JPG 原图或 transfer，避免 webp 无法绘制）
     */
    private String editUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片简介
     */
    private String introduction;

    /**
     * 标签（JSON数组）
     */
    private List<String> tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片大小
     */
    private Long picSize;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 创建用户id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 空间 id（为空表示公共空间）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long spaceId;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝（管理端列表需要）
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 id
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    private static final long serialVersionUID = 1L;

    private static Picture voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        if (pictureVO.getUrls() != null) {
            picture.setUrls(pictureVO.getUrls());
        } else if (StrUtil.isNotBlank(pictureVO.getUrl()) || StrUtil.isNotBlank(pictureVO.getThumbnailUrl())) {
            picture.setUrls(Urls.builder()
                    .url(pictureVO.getUrl())
                    .thumbnailUrl(pictureVO.getThumbnailUrl())
                    .build());
        }
        return picture;
    }

    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);
        if (picture.getUrls() != null) {
            pictureVO.setUrl(picture.getUrls().getUrl());
            pictureVO.setThumbnailUrl(picture.getUrls().getThumbnailUrl());
            pictureVO.setEditUrl(resolveEditUrl(picture.getUrls()));
        }
        String tags = picture.getTags();
        if (StrUtil.isNotBlank(tags)) {
            pictureVO.setTags(JSONUtil.toList(tags, String.class));
        }
        return pictureVO;
    }

    /**
     * 编辑器优先用非 webp、可跨域绘制的地址
     */
    public static String resolveEditUrl(Urls urls) {
        if (urls == null) {
            return null;
        }
        if (StrUtil.isNotBlank(urls.getTransferUrl())) {
            return urls.getTransferUrl();
        }
        if (StrUtil.isNotBlank(urls.getOriginalUrl())) {
            return urls.getOriginalUrl();
        }
        if (StrUtil.isNotBlank(urls.getUrl()) && !urls.getUrl().toLowerCase().endsWith(".webp")) {
            return urls.getUrl();
        }
        if (StrUtil.isNotBlank(urls.getThumbnailUrl()) && !urls.getThumbnailUrl().toLowerCase().endsWith(".webp")) {
            return urls.getThumbnailUrl();
        }
        return urls.getUrl();
    }
}
