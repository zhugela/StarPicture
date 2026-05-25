package com.yu.backend.controller.picture;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.yu.backend.manager.CosManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yu.backend.api.aliyunai.AliYunAiApi;
import com.yu.backend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yu.backend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.yu.backend.api.imagesearch.ImageSearchApiFacade;
import com.yu.backend.api.imagesearch.model.ImageSearchResult;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.backend.annotation.AuthCheck;
import com.yu.backend.common.BaseResponse;
import com.yu.backend.common.DeleteRequest;
import com.yu.backend.common.ResultUtils;
import com.yu.backend.constant.UserConstant;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.mapper.SpaceMapper;
import com.yu.backend.model.dto.picture.*;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.Urls;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.PictureReviewStatusEnum;
import com.yu.backend.model.vo.PictureVO;
import com.yu.backend.service.PictureService;
import com.yu.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private CosManager cosManager;

    @Autowired
    private ObjectMapper objectMapper;

    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10000L)
            .expireAfterWrite(5L, TimeUnit.MINUTES)
            .build();
    /**
     *
     *删除照片
     */

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }
    /**
     * 根据id获取照片（管理员可用）
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse< Picture> getPictureById(@RequestParam("id") Long id, HttpServletRequest request){
        //1.参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        //2.通过id判断图片是否存在
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //3.获取照片
        return ResultUtils.success(picture);
    }
    /**
     * 根据id获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(@RequestParam("id") String idStr, HttpServletRequest request) {
        ThrowUtils.throwIf(StrUtil.isBlank(idStr), ErrorCode.PARAMS_ERROR);
        Long id;
        try {
            id = Long.parseLong(idStr.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id 格式错误");
        }
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            User loginUser = null;
            try {
                loginUser = userService.getLoginUser(request);
            } catch (BusinessException ignored) {
            }
            boolean canView = loginUser != null
                    && (loginUser.getId().equals(picture.getUserId()) || userService.isAdmin(loginUser));
            if (!canView) {
                ThrowUtils.throwIf(!Objects.equals(picture.getReviewStatus(), PictureReviewStatusEnum.PASS.getValue()),
                        ErrorCode.NOT_FOUND_ERROR);
            }
        } else {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }
    /**
     * 编辑图片
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureEditRequest.getId() == null, ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 空间内图片批量编辑元数据
     */
    @PostMapping("/edit/batch")
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.batchEditPictureMetadata(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片
     * @param pictureUpdateRequest
     * @param request
     * @return
     */

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 校验参数
        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);

        pictureService.fillReviewParams(picture, loginUser);

        // 判断图片是否存在（加上这段）
        Picture oldPicture = pictureService.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新
        boolean result = pictureService.updateById(picture);
        return ResultUtils.success(result);
    }
    /**
     * 获取图片列表（管理员可用）
     * <p>返回 {@link PictureVO}，包含与 {@code urls} 同步的顶层 {@code url}/{@code thumbnailUrl}，便于前端表格预览列绑定。</p>
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PictureVO>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long picsize = pictureQueryRequest.getPageSize();
        Page<Picture> page = pictureService.page(new Page<>(current, picsize), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVOPage(page, request));
    }
    /**
     * 获取图片列表VO
     */

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size <= 0 || size > 20, ErrorCode.PARAMS_ERROR, "pageSize 不能超过 20");
        preparePictureQueryForList(pictureQueryRequest, request);
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 获取图片列表 VO（Caffeine 本地缓存）
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size <= 0 || size > 20, ErrorCode.PARAMS_ERROR, "pageSize 不能超过 20");
        preparePictureQueryForList(pictureQueryRequest, request);
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = "listPictureVOByPage:" + hashKey;
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            try {
                Page<PictureVO> cachedPage = objectMapper.readValue(cachedValue,
                        objectMapper.getTypeFactory().constructParametricType(Page.class, PictureVO.class));
                if (cachedPage != null && cachedPage.getRecords() != null) {
                    return ResultUtils.success(cachedPage);
                }
            } catch (Exception e) {
                log.warn("listPictureVOByPageWithCache deserialize failed, invalidate key={}", cacheKey, e);
                LOCAL_CACHE.invalidate(cacheKey);
            }
        }
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        try {
            LOCAL_CACHE.put(cacheKey, objectMapper.writeValueAsString(pictureVOPage));
        } catch (Exception e) {
            log.warn("listPictureVOByPageWithCache serialize failed, key={}", cacheKey, e);
        }
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 创建图片的标签
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 审核照片（管理员）
     * @param pictureReviewRequest
     * @param request
     * @return
     */

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 通过url上传图片
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileurl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileurl,
                pictureUploadRequest.toPictureUploadWithUserDTO(loginUser));
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 公共图库：只查已过审且 spaceId 为 null；私有空间：校验当前用户为空间创建人。
     */
    private void preparePictureQueryForList(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            pictureQueryRequest.setNullSpaceId(true);
            Long queryUserId = pictureQueryRequest.getUserId();
            if (queryUserId != null) {
                try {
                    User loginUser = userService.getLoginUser(request);
                    if (loginUser.getId().equals(queryUserId)) {
                        pictureQueryRequest.setReviewStatus(null);
                    } else {
                        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
                    }
                } catch (BusinessException e) {
                    pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
                }
            } else {
                pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            }
        } else {
            pictureQueryRequest.setNullSpaceId(false);
            User loginUser = userService.getLoginUser(request);
            Space space = spaceMapper.selectById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }
    }

    /**
     * 以图搜图（根据已入库图片 id 解析可检索 URL，再走百度图搜）
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(
            @RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        pictureService.checkPictureAuth(loginUser, oldPicture);
        Urls urls = oldPicture.getUrls();
        ThrowUtils.throwIf(urls == null, ErrorCode.PARAMS_ERROR, "图片地址不存在");
        String searchUrl = resolveBaiduSearchUrl(urls);
        ThrowUtils.throwIf(StrUtil.isBlank(searchUrl), ErrorCode.PARAMS_ERROR, "无可用图片地址用于以图搜图");
        return ResultUtils.success(ImageSearchApiFacade.searchImage(searchUrl));
    }

    /**
     * 按颜色相似度检索空间内图片
     */
    @PostMapping("/search/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(
            @RequestBody SearchPictureByColorRequest searchPictureByColorRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> result = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 创建 AI 扩图任务
     */
    @PostMapping("/out_painting/create_task")
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(createPictureOutPaintingTaskRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(createPictureOutPaintingTaskRequest.getPictureId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(
                createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(
            @RequestParam(value = "taskId", required = false) String taskId,
            @RequestParam(value = "task_id", required = false) String taskIdSnake) {
        String resolvedTaskId = StrUtil.isNotBlank(taskId) ? taskId : taskIdSnake;
        ThrowUtils.throwIf(StrUtil.isBlank(resolvedTaskId), ErrorCode.PARAMS_ERROR, "任务 id 不能为空");
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(resolvedTaskId);
        return ResultUtils.success(task);
    }

    /**
     * 编辑器同源代理 COS 图片，解决 vue-cropper Canvas 跨域灰格
     */
    @GetMapping("/proxy/editor")
    public void proxyEditorImage(@RequestParam String url, HttpServletResponse response) throws IOException {
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(cosManager.keyFromPublicUrl(url) == null, ErrorCode.PARAMS_ERROR, "仅支持本系统 COS 图片");
        byte[] data = HttpUtil.downloadBytes(url);
        ThrowUtils.throwIf(data == null || data.length == 0, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        String lower = url.toLowerCase();
        if (lower.contains(".png")) {
            response.setContentType("image/png");
        } else if (lower.contains(".webp")) {
            response.setContentType("image/webp");
        } else {
            response.setContentType("image/jpeg");
        }
        response.setHeader("Cache-Control", "private, max-age=3600");
        response.getOutputStream().write(data);
    }

    /**
     * 优先转存地址；否则优先非 webp 缩略图，再退回缩略图或原图。
     */
    private static String resolveBaiduSearchUrl(Urls url) {
        if (StrUtil.isNotBlank(url.getTransferUrl())) {
            return url.getTransferUrl();
        }
        if (StrUtil.isNotBlank(url.getThumbnailUrl()) && !url.getThumbnailUrl().endsWith(".webp")) {
            return url.getThumbnailUrl();
        }
        if (StrUtil.isNotBlank(url.getThumbnailUrl())) {
            return url.getThumbnailUrl();
        }
        return url.getUrl();
    }

}

