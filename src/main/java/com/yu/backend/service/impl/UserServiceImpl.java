package com.yu.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.User;
import com.yu.backend.service.UserService;
import com.yu.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 26228
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-03-09 19:39:59
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




