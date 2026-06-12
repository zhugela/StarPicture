package com.yu.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.backend.model.entity.Space;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 针对表【space(空间)】的数据库操作 Mapper
 */
@Mapper
public interface SpaceMapper extends BaseMapper<Space> {

    /**
     * 按 totalSize 降序取前 N 个空间
     */
    List<Space> getTopNSpaceUsage(@Param("topN") Integer topN);
}
