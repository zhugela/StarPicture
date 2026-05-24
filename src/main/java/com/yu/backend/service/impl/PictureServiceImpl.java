package com.yu.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.api.aliyunai.AliYunAiApi;
import com.yu.backend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.yu.backend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yu.backend.utils.ColorSimilarUtils;
import com.yu.backend.manager.FileManager;
import com.yu.backend.manager.CosManager;
import com.yu.backend.manager.factory.UploadFactory;
import com.yu.backend.manager.upload.PictureUploadTemplate;
import com.yu.backend.mapper.PictureMapper;
import com.yu.backend.mapper.SpaceMapper;
import com.yu.backend.model.dto.file.UploadPictureResult;
import com.yu.backend.model.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.yu.backend.model.dto.picture.PictureEditByBatchRequest;
import com.yu.backend.model.dto.picture.PictureEditRequest;
import com.yu.backend.model.dto.picture.PictureQueryRequest;
import com.yu.backend.model.dto.picture.PictureReviewRequest;
import com.yu.backend.model.dto.picture.PictureUploadByBatchRequest;
import com.yu.backend.model.dto.picture.PictureUploadRequest;
import com.yu.backend.model.dto.picture.PictureUploadWithUserDTO;
import com.yu.backend.model.enums.FileUploadEnum;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.Urls;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.PictureReviewStatusEnum;
import com.yu.backend.model.vo.PictureVO;
import com.yu.backend.model.vo.UserVO;
import com.yu.backend.service.PictureService;
import com.yu.backend.service.SpaceService;
import com.yu.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 26228
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2026-03-15 06:54:53
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {
    private static final long TWO_MB = 2 * 1024 * 1024L;

    private final PictureMapper pictureMapper;
    private final UserService userService;
    private final UploadFactory uploadFactory;
    private final SpaceMapper spaceMapper;
    private final FileManager fileManager;
    private final CosManager cosManager;
    /** 用于触发 {@link Async} 代理（本类内部直接调 {@code this.clearPictureFile} 不会异步） */
    private final PictureService pictureService;
    private final SpaceService spaceService;
    private final TransactionTemplate transactionTemplate;
    private final Executor yuPictureExecutor;
    private final AliYunAiApi aliYunAiApi;

    public PictureServiceImpl(PictureMapper pictureMapper,
                              UserService userService,
                              UploadFactory uploadFactory,
                              SpaceMapper spaceMapper,
                              FileManager fileManager,
                              CosManager cosManager,
                              @Lazy PictureService pictureService,
                              SpaceService spaceService,
                              TransactionTemplate transactionTemplate,
                              @Qualifier("yuPictureExecutor") Executor yuPictureExecutor,
                              AliYunAiApi aliYunAiApi) {
        this.pictureMapper = pictureMapper;
        this.baseMapper = pictureMapper;
        this.userService = userService;
        this.uploadFactory = uploadFactory;
        this.spaceMapper = spaceMapper;
        this.fileManager = fileManager;
        this.cosManager = cosManager;
        this.pictureService = pictureService;
        this.spaceService = spaceService;
        this.transactionTemplate = transactionTemplate;
        this.yuPictureExecutor = yuPictureExecutor;
        this.aliYunAiApi = aliYunAiApi;
    }

    /**
     * 上传图片
     *
     * @param object               文件/url
     * @param pictureUploadRequest 请求类（含登录用户）
     */
    @Override
    public PictureVO uploadPicture(Object object, PictureUploadWithUserDTO pictureUploadRequest) {
        checkParam(pictureUploadRequest, object);
        User loginUser = pictureUploadRequest.getUser();
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未登录");

        Long spaceId = getSpaceIdAndCheckAuth(pictureUploadRequest);
        checkSpaceUsage(spaceId);

        Long pictureId = pictureUploadRequest.getId();

        PictureUploadTemplate uploadTemplate = uploadFactory.getUploadFactory(
                object instanceof MultipartFile
                        ? FileUploadEnum.FILE.getType()
                        : FileUploadEnum.URL.getType());

        String uploadPathPrefix = Objects.nonNull(spaceId)
                ? String.format("space/%s", spaceId)
                : String.format("public/%s", loginUser.getId());

        UploadPictureResult uploadPictureResult = uploadTemplate.uploadPicture(object, uploadPathPrefix);
        ThrowUtils.throwIf(uploadPictureResult.getUrls() == null
                        || StrUtil.isBlank(uploadPictureResult.getUrls().getUrl()),
                ErrorCode.SYSTEM_ERROR, "上传结果缺少地址");

        final long newPicBytes = uploadPictureResult.getPicSize() != null ? uploadPictureResult.getPicSize() : 0L;
        Picture existingForUpdate = null;
        final long oldPicBytes;
        if (pictureId != null) {
            existingForUpdate = buildUpdatePicture(loginUser, pictureId, spaceId);
            oldPicBytes = existingForUpdate.getPicSize() != null ? existingForUpdate.getPicSize() : 0L;
        } else {
            oldPicBytes = 0L;
        }
        assertSpaceFitsUploadedFile(spaceId, pictureId == null, newPicBytes, oldPicBytes);

        Picture picture;
        if (pictureId == null) {
            picture = buildPicture(uploadPictureResult, null, pictureUploadRequest, spaceId);
        } else {
            picture = buildPicture(uploadPictureResult, existingForUpdate, pictureUploadRequest, spaceId);
        }

        this.fillReviewParams(picture, loginUser);

        final Picture finalPicture = picture;
        final Long finalSpaceId = spaceId;
        final boolean insert = pictureId == null;
        transactionTemplate.execute(status -> {
            ThrowUtils.throwIf(!this.saveOrUpdate(finalPicture), ErrorCode.SYSTEM_ERROR, "保存失败");
            if (finalSpaceId != null) {
                if (insert) {
                    ThrowUtils.throwIf(!spaceService.increaseUsageForNewPicture(finalSpaceId, newPicBytes),
                            ErrorCode.SYSTEM_ERROR, "额度更新失败");
                } else {
                    long delta = newPicBytes - oldPicBytes;
                    if (delta != 0) {
                        ThrowUtils.throwIf(!spaceService.adjustTotalSizeByDelta(finalSpaceId, delta),
                                ErrorCode.SYSTEM_ERROR, "额度更新失败");
                    }
                }
            }
            return null;
        });

        return PictureVO.objToVo(picture);
    }

    /**
     * 是否上传到 space 空间 & 校验是否有权限
     *
     * @param pictureUploadRequest dto 封装类 包含 user
     * @return spaceId 如果是 null 就表示不上传到 space
     */
    private Long getSpaceIdAndCheckAuth(PictureUploadWithUserDTO pictureUploadRequest) {
        User loginUser = pictureUploadRequest.getUser();
        Long spaceId = pictureUploadRequest.getSpaceId();
        Long spaceIdOfPicture = null;
        if (spaceId != null) {
            Space space = spaceMapper.selectById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()),
                    ErrorCode.NO_AUTH_ERROR, "空间创建人才能上传图片");
            spaceIdOfPicture = spaceId;
        }
        if (Objects.isNull(spaceIdOfPicture) && pictureUploadRequest.getId() != null) {
            Picture picture = pictureMapper.selectById(pictureUploadRequest.getId());
            ThrowUtils.throwIf(Objects.isNull(picture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            if (Objects.isNull(picture.getSpaceId())) {
                return null;
            }
            spaceIdOfPicture = picture.getSpaceId();
        }
        return spaceIdOfPicture;
    }

    /**
     * 上传前粗校验：条数、总大小是否已达上限（与参考实现一致）
     */
    private void checkSpaceUsage(Long spaceId) {
        if (spaceId == null) {
            return;
        }
        Space space = spaceMapper.selectById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (space.getMaxCount() != null && space.getTotalCount() != null && space.getTotalCount() >= space.getMaxCount()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
        }
        if (space.getMaxSize() != null && space.getTotalSize() != null && space.getTotalSize() >= space.getMaxSize()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
        }
    }

    /**
     * 拿到实际上传文件大小后，校验本次写入是否超出限额（新增计条数；覆盖仅按体积差调整）
     */
    private void assertSpaceFitsUploadedFile(Long spaceId, boolean isNew, long newPicBytes, long oldPicBytes) {
        if (spaceId == null) {
            return;
        }
        Space space = spaceMapper.selectById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        long maxCount = space.getMaxCount() == null ? Long.MAX_VALUE : space.getMaxCount();
        long maxSize = space.getMaxSize() == null ? Long.MAX_VALUE : space.getMaxSize();
        long totalCount = space.getTotalCount() == null ? 0L : space.getTotalCount();
        long totalSize = space.getTotalSize() == null ? 0L : space.getTotalSize();
        if (isNew) {
            if (totalCount >= maxCount) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }
            if (totalSize + newPicBytes > maxSize) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
        } else {
            if (totalSize + (newPicBytes - oldPicBytes) > maxSize) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
        }
    }

    private Picture buildUpdatePicture(User loginUser, Long pictureId, Long spaceId) {
        Picture oldPicture = pictureMapper.selectById(pictureId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (Objects.isNull(spaceId) && Objects.nonNull(oldPicture.getSpaceId())) {
            return oldPicture;
        }
        if (Objects.nonNull(spaceId) && oldPicture.getSpaceId() != null
                && !Objects.equals(spaceId, oldPicture.getSpaceId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不匹配");
        }
        return oldPicture;
    }

    private Picture buildPicture(UploadPictureResult result,
                                 Picture oldPicture,
                                 PictureUploadWithUserDTO dto,
                                 Long resolvedSpaceId) {
        Picture.PictureBuilder builder = Picture.builder()
                .id(oldPicture != null ? oldPicture.getId() : null)
                .editTime(oldPicture != null ? new Date() : null)
                .spaceId(resolvedSpaceId)
                .urls(result.getUrls())
                .picColor(result.getPicColor())
                .name(StrUtil.isNotBlank(dto.getPicName()) ? dto.getPicName() : result.getPicName())
                .picScale(result.getPicScale())
                .picFormat(result.getPicFormat())
                .picHeight(result.getPicHeight())
                .picWidth(result.getPicWidth())
                .userId(dto.getUser().getId())
                .picSize(result.getPicSize());
        if (oldPicture != null) {
            builder.introduction(oldPicture.getIntroduction())
                    .category(oldPicture.getCategory())
                    .tags(oldPicture.getTags())
                    .createTime(oldPicture.getCreateTime());
        }
        return builder.build();
    }

    private void checkParam(PictureUploadWithUserDTO pictureUploadRequest, Object object) {
        if (pictureUploadRequest == null || object == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            if (!Objects.equals(picture.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            if (!Objects.equals(picture.getUserId(), loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未登录");
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        checkPictureAuth(loginUser, oldPicture);

        String mainUrl = oldPicture.getUrls() != null ? oldPicture.getUrls().getUrl() : null;
        long sameUrlCount = StrUtil.isNotBlank(mainUrl) ? countPicturesByMainUrl(mainUrl) : 0;

        final long pid = pictureId;
        final Picture snapshot = oldPicture;
        transactionTemplate.execute(status -> {
            ThrowUtils.throwIf(!this.removeById(pid), ErrorCode.OPERATION_ERROR);
            Long sid = snapshot.getSpaceId();
            if (sid != null) {
                ThrowUtils.throwIf(!spaceService.delPictureUpdateSpaceUsage(sid, snapshot.getPicSize()),
                        ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return null;
        });

        if (sameUrlCount <= 1) {
            pictureService.clearPictureFile(oldPicture);
        }
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR);
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        this.validPicture(picture);
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        checkPictureAuth(loginUser, oldPicture);
        this.fillReviewParams(picture, loginUser);
        ThrowUtils.throwIf(!this.updateById(picture), ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void batchEditPictureMetadata(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        ThrowUtils.throwIf(CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        Space space = spaceMapper.selectById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!space.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }

        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        if (CollUtil.isEmpty(pictureList)) {
            return;
        }

        Date now = new Date();
        for (Picture picture : pictureList) {
            picture.setEditTime(now);
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tags)) {
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        }
        fillPictureWithNameRule(pictureList, pictureEditByBatchRequest.getNameRule());

        int batchSize = 100;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < pictureList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, pictureList.size());
            List<Picture> subList = new ArrayList<>(pictureList.subList(i, end));
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                    transactionTemplate.execute(status -> {
                        ThrowUtils.throwIf(!this.updateBatchById(subList), ErrorCode.OPERATION_ERROR, "批量编辑失败");
                        return null;
                    }), yuPictureExecutor);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(
            CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        ThrowUtils.throwIf(createPictureOutPaintingTaskRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        Picture picture = this.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        checkPictureAuth(loginUser, picture);
        Urls urls = picture.getUrls();
        ThrowUtils.throwIf(urls == null || StrUtil.isBlank(urls.getUrl()), ErrorCode.PARAMS_ERROR, "图片地址不存在");

        CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(urls.getUrl());
        taskRequest.setInput(input);
        taskRequest.setParameters(createPictureOutPaintingTaskRequest.getParameters());
        return aliYunAiApi.createOutPaintingTask(taskRequest);
    }

    /**
     * nameRule 格式：图片{序号}
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replace("{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
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
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();

        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText));
        }

        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);

        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");

        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        boolean isAsc = sortOrder != null && "ascend".equals(sortOrder);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), isAsc, sortField);

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
        String url = Optional.ofNullable(picture.getUrls()).map(Urls::getUrl).orElse("");
        String introduction = picture.getIntroduction();

        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /**
     * 统计主图 URL 与给定地址相同的记录数（MySQL JSON_CONTAINS）
     */
    private long countPicturesByMainUrl(String pictureUrl) {
        return this.lambdaQuery()
                .apply("JSON_CONTAINS(urls, JSON_QUOTE({0}), '$.url')", pictureUrl)
                .count();
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

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        // --------------------
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        // --------------------
        // 格式化数量
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");
        // 要抓取的地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            // --------------------
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            if (StrUtil.isNotBlank(namePrefix)) {
                // 设置图片名称，序号连续递增
                pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            }
            // --------------------
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl,
                        pictureUploadRequest.toPictureUploadWithUserDTO(loginUser));
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
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

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Space space = spaceMapper.selectById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }
        LambdaQueryWrapper<Picture> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Picture::getSpaceId, spaceId);
        wrapper.isNotNull(Picture::getPicColor);
        wrapper.ne(Picture::getPicColor, "");
        List<Picture> pictureList = this.list(wrapper);
        if (CollUtil.isEmpty(pictureList)) {
            return Collections.emptyList();
        }
        final Color targetColor;
        try {
            targetColor = ColorSimilarUtils.parseAveColor(picColor);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "颜色参数不合法");
        }
        List<Picture> sortedPictures = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    String hexColor = picture.getPicColor();
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    try {
                        Color pictureColor = ColorSimilarUtils.parseAveColor(hexColor);
                        return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                    } catch (Exception ex) {
                        return Double.MAX_VALUE;
                    }
                }))
                .limit(12)
                .collect(Collectors.toList());
        return sortedPictures.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        if (oldPicture == null || oldPicture.getUrls() == null) {
            return;
        }
        Urls urls = oldPicture.getUrls();
        List<String> keys = Stream.of(urls.getOriginalUrl(), urls.getUrl(), urls.getThumbnailUrl(), urls.getTransferUrl())
                .filter(StrUtil::isNotBlank)
                .map(cosManager::keyFromPublicUrl)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(keys)) {
            cosManager.deleteObjects(keys);
        }
    }

}