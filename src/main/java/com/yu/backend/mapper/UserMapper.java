package com.yu.backend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.backend.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 26228
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2026-03-09 19:39:59
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




