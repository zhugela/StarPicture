package com.yu.backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
@Data
public class PictureUploadByBatchRequest  {
    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;
}
