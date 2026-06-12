package com.yu.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.backend.model.entity.SpaceUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【space_user(空间用户关联)】的数据库操作 Mapper
 */
@Mapper
public interface SpaceUserMapper extends BaseMapper<SpaceUser> {

}
