package com.yu.backend.model.dto.file;

import com.yu.backend.model.entity.Urls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadPictureResult {

    /**
     * 图片各访问地址
     */
    private Urls urls;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片主色调（COS 原图信息均色）
     */
    private String picColor;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;
}
