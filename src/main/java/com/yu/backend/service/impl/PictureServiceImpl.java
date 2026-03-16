package com.yu.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yu.backend.mapper.PictureMapper;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.service.PictureService;

import org.springframework.stereotype.Service;

/**
* @author 26228
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2026-03-15 06:54:53
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

}




