package com.yu.backend.model.dto.picture;

import com.yu.backend.model.entity.User;
import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传请求（携带当前登录用户，供 Service 层使用）
 */
@Data
public class PictureUploadWithUserDTO implements Serializable {

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 登录的用户
     */
    private User user;

    private static final long serialVersionUID = 1L;
}
