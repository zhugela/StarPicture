package com.yu.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.backend.model.dto.space.SpaceAnalyzeRequest;
import com.yu.backend.model.entity.Picture;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 针对表【picture(图片)】的数据库操作 Mapper
 */
public interface PictureMapper extends BaseMapper<Picture> {

    /**
     * 按分类统计图片数量与总大小
     *
     * @param params 分析范围：queryAll / queryPublic / spaceId
     */
    List<Map<String, Object>> getCategoryStatistics(@Param("params") SpaceAnalyzeRequest params);

    /**
     * 按时间维度统计上传数量
     */
    List<Map<String, Object>> analyzeByTimeDimension(@Param("params") Map<String, Object> params);
}


