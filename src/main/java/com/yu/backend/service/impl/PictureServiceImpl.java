package com.yu.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.manager.FileManager;
import com.yu.backend.mapper.PictureMapper;
import com.yu.backend.model.dto.file.UploadPictureResult;
import com.yu.backend.model.dto.picture.PictureQueryRequest;
import com.yu.backend.model.dto.picture.PictureUploadRequest;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.UserRoleEnums;
import com.yu.backend.model.vo.PictureVO;
import com.yu.backend.model.vo.UserVO;
import com.yu.backend.service.PictureService;

import com.yu.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.yu.backend.constant.UserConstant.ADMIN_ROLE;

/**
* @author 26228
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2026-03-15 06:54:53
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{
    @Resource
    private FileManager fileManager;

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private UserService userService;

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //第一步：校验文件
        // --用户是否登录
        // --请求参数和文件为空
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR);

        //第二步：上传图片到COS
//        -拼接上传路径（比如public/用户id/xxx.jpg)
//        --调用FileManager的上传方法
        Long pictureId = pictureUploadRequest.getId();
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture2(multipartFile, uploadPathPrefix);
        //第三步：判断是新增还是修改
        Picture picture;
        if(pictureId != null){
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null,ErrorCode.NOT_FOUND_ERROR);
            picture = buildPicture(uploadPictureResult,oldPicture,loginUser.getId());
        }
        else {
            picture = buildPicture(uploadPictureResult,null,loginUser.getId());
        }
        //第四步：构建picture对象
        ThrowUtils.throwIf(!this.saveOrUpdate(picture),ErrorCode.SYSTEM_ERROR,"保存失败");
        //第五步：保存到数据库
        return PictureVO.objToVo(picture);

    }
    /**
     * 判断是否为管理员
     */


        @Override
        public boolean isAdmin(User user) {
            return user != null && UserRoleEnums.ADMIN.getValue().equals(user.getUserRole());
        }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
//            1.判断是否为空
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);

//        2.从pictureQueryRequest中获取参数
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
//        3. 创建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjectUtil.isNotNull(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.eq(ObjectUtil.isNotNull(picSize), "picSize", picSize);
        queryWrapper.eq(ObjectUtil.isNotNull(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjectUtil.isNotNull(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjectUtil.isNotNull(picScale), "picScale", picScale);
        queryWrapper.eq(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(category), "category", category);
        // 正确：同时搜 name 和 introduction
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw
                    .like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }

        //JSON数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        //对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        //关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null&& userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());

        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }

        // 1. Picture 转 PictureVO
        List<PictureVO> pictureVOList = new ArrayList<>();
        for (Picture picture : pictureList) {
            pictureVOList.add(PictureVO.objToVo(picture));
        }

        // 2. 收集所有 userId，批量查用户
        Set<Long> userIdSet = new HashSet<>();
        for (Picture picture : pictureList) {
            userIdSet.add(picture.getUserId());
        }
        List<User> userList = userService.listByIds(userIdSet);
        Map<Long, User> userMap = new HashMap<>();
        for (User user : userList) {
            userMap.put(user.getId(), user);
        }

        // 3. 给每个 PictureVO 填上用户信息
        for (PictureVO pictureVO : pictureVOList) {
            User user = userMap.get(pictureVO.getUserId());
            pictureVO.setUser(userService.getUserVO(user));
        }

        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }




    private Picture buildPicture(UploadPictureResult uploadPictureResult,Picture oldPicture,Long userId){
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(userId);
        if(oldPicture != null){
            picture.setId(oldPicture.getId());
            picture.setEditTime(new Date());
        }
        return picture;
    }
}




