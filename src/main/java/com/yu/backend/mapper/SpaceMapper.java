package com.yu.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.backend.model.entity.Space;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【space(空间)】的数据库操作 Mapper
 */
@Mapper
public interface SpaceMapper extends BaseMapper<Space> {

}
