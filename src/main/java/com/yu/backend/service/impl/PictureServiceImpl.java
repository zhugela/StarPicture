package com.yu.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.manager.FileManager;
import com.yu.backend.mapper.PictureMapper;
import com.yu.backend.model.dto.file.UploadPictureResult;
import com.yu.backend.model.dto.picture.PictureQueryRequest;
import com.yu.backend.model.dto.picture.PictureReviewRequest;
import com.yu.backend.model.dto.picture.PictureUploadRequest;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.PictureReviewStatusEnum;
import com.yu.backend.model.vo.PictureVO;
import com.yu.backend.model.vo.UserVO;
import com.yu.backend.service.PictureService;
import com.yu.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 26228
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2026-03-15 06:54:53
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {
    private static final long TWO_MB = 2 * 1024 * 1024L;
    @Resource
    private FileManager fileManager;

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private UserService userService;

    /**
     * 上传图片
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR);

        // 2. 上传图片到 COS
        Long pictureId = pictureUploadRequest.getId();
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture2(multipartFile, uploadPathPrefix);

        // 3. 判断是新增还是更新
        Picture picture;
        if (pictureId == null) {
            // 新增
            picture = buildPicture(uploadPictureResult, null, loginUser.getId());
        } else {
            // 更新
            Picture oldPicture = pictureMapper.selectById(pictureId);
            ThrowUtils.throwIf(ObjectUtils.isEmpty(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 校验权限：仅本人或管理员可更新
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            picture = buildPicture(uploadPictureResult, oldPicture, loginUser.getId());
        }

        // 4. 补充审核参数
        this.fillReviewParams(picture, loginUser);

        // 5. 保存到数据库
        ThrowUtils.throwIf(!this.saveOrUpdate(picture), ErrorCode.SYSTEM_ERROR, "保存失败");

        return PictureVO.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);

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
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();



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
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw
                    .like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }

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
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
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

        List<PictureVO> pictureVOList = new ArrayList<>();
        for (Picture picture : pictureList) {
            pictureVOList.add(PictureVO.objToVo(picture));
        }

        Set<Long> userIdSet = new HashSet<>();
        for (Picture picture : pictureList) {
            userIdSet.add(picture.getUserId());
        }
        List<User> userList = userService.listByIds(userIdSet);
        Map<Long, User> userMap = new HashMap<>();
        for (User user : userList) {
            userMap.put(user.getId(), user);
        }

        for (PictureVO pictureVO : pictureVOList) {
            User user = userMap.get(pictureVO.getUserId());
            pictureVO.setUser(userService.getUserVO(user));
        }

        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不能为空");

        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();

        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "图片id不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "图片url长度过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 1024, ErrorCode.PARAMS_ERROR, "图片简介长度过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "图片id不能为空");
        ThrowUtils.throwIf(reviewStatus == null, ErrorCode.PARAMS_ERROR, "审核状态不能为空");

        // 2. 查询图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 3. 判断是否重复审核
        ThrowUtils.throwIf(oldPicture.getReviewStatus().equals(reviewStatus),
                ErrorCode.PARAMS_ERROR, "请勿重复审核");

        // 4. 构建并保存审核信息
        Picture updatePicture = buildUpdateReviewPicture(loginUser, pictureReviewRequest);
        ThrowUtils.throwIf(!this.saveOrUpdate(updatePicture), ErrorCode.SYSTEM_ERROR, "保存失败");
    }

    /**
     * 填充审核参数（管理员自动审核通过，普通用户进入待审核状态）
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动审核通过");
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 构建审核图片对象
     */
    private Picture buildUpdateReviewPicture(User loginUser, PictureReviewRequest pictureReviewRequest) {
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewStatus(pictureReviewRequest.getReviewStatus());
        updatePicture.setReviewMessage(pictureReviewRequest.getReviewMessage());
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        return updatePicture;
    }

    /**
     * 构建图片对象（新增或修改）
     */
    private Picture buildPicture(UploadPictureResult uploadPictureResult, Picture oldPicture, Long userId) {
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(userId);
        if (oldPicture != null) {
            picture.setId(oldPicture.getId());
            picture.setEditTime(new Date());
        }
        return picture;
    }
    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
        validUrlPicture(fileUrl);
        String uuid = RandomUtil.randomString(16);
        String originFilename = FileUtil.mainName(fileUrl);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            HttpUtil.downloadFile(fileUrl, file);
            // return 要放在 try 里 ↓
            return fileManager.uploadPicture2(file, uploadPathPrefix);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
            // ❌ throw 后面不能有代码，return 不能放这里
        } finally {
            fileManager.deleteTempFile(file);
        }
    }

    private void validUrlPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        try {
            // 1. 验证 URL 格式  验证是否是合法的 URL
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        // 2. 校验 URL 协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");
        // 3. 发送 HEAD 请求以验证文件是否存在
        // 使用 try with resources 简化流程
        try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute()) {
            // 未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 4. 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        }
    }



}