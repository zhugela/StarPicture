package com.yu.backend.model.dto.picture;

import com.yu.backend.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * 图片上传请求（接口入参）
 *
 * @author leikooo
 */
@Data
public class PictureUploadRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1989537749944365432L;

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片 url
     */
    private String fileUrl;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 转为携带登录用户的 DTO，供 {@link com.yu.backend.service.PictureService#uploadPicture} 使用
     */
    public PictureUploadWithUserDTO toPictureUploadWithUserDTO(User loginUser) {
        PictureUploadWithUserDTO pictureUploadWithUserDTO = new PictureUploadWithUserDTO();
        BeanUtils.copyProperties(this, pictureUploadWithUserDTO);
        pictureUploadWithUserDTO.setUser(loginUser);
        return pictureUploadWithUserDTO;
    }
}
