package com.yu.backend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.yu.backend.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

@Data
public class PictureVO {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图片url
     */
    private String url;

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
    private Long userId;

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
    /**
     * 封装类转对象
     */
    private static Picture voToObj(PictureVO pictureVO) {
        if(pictureVO == null){
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

    /**
     * 对象转封装类
     */
    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(picture, pictureVO);
        // 加个判空，防止 tags 为 null 时报错
        String tags = picture.getTags();
        if (StrUtil.isNotBlank(tags)) {
            pictureVO.setTags(JSONUtil.toList(tags, String.class));
        }
        return pictureVO;
    }
}
