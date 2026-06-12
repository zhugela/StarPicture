package com.yu.backend.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.backend.model.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.yu.backend.model.dto.picture.PictureEditByBatchRequest;
import com.yu.backend.model.dto.picture.PictureEditRequest;
import com.yu.backend.model.dto.picture.PictureQueryRequest;
import com.yu.backend.model.dto.picture.PictureReviewRequest;
import com.yu.backend.model.dto.picture.PictureUploadByBatchRequest;
import com.yu.backend.model.dto.picture.PictureUploadWithUserDTO;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.vo.PictureVO;
import com.yu.backend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 26228
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-03-15 06:54:53
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param inputSource           文件 / url
     * @param pictureUploadRequest 请求（含登录用户）
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadWithUserDTO pictureUploadRequest);


    /**
     * 获取查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取单个图像的VO对象
     *
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片的VO对象列表
     *
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 检验参数
     */
    void validPicture(Picture picture);

    /**
     * 审核照片信息
     *
     * @param pictureReviewRequest
     *
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数，方便其他方法使用
     *
     * @param picture   picture
     * @param loginUser 登录的用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 删除图片（逻辑删除记录，并按引用数决定是否清理 COS）
     *
     * @param pictureId 图片 id
     * @param spaceId   空间 id（公共图库为 0；分表查询必填）
     * @param loginUser 当前登录用户
     */
    void deletePicture(long pictureId, Long spaceId, User loginUser);

    /**
     * 按 id + spaceId 查询图片（ShardingSphere 分表路由）
     *
     * @param id      图片 id
     * @param spaceId 空间 id，null 时仅按 id 查询（会广播分表，不推荐）
     */
    Picture getPicture(Long id, Long spaceId);

    /**
     * 校验当前用户是否有权操作该图片（编辑、删除、查看空间内资源等）。
     * 公共图库（spaceId 为空）：本人或系统管理员；私人空间：仅图片上传者本人，管理员也不可。
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 编辑图片（元数据）
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 批量更新空间内指定图片的分类、标签与按规则重命名
     */
    void batchEditPictureMetadata(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 清理图片在对象存储中的文件（主图、缩略图等）
     */
    void clearPictureFile(Picture picture);

    /**
     * 按照颜色相似度查询某空间下图片（仅返回有主色调的记录，最多 12 条）
     *
     * @param spaceId  空间 id
     * @param picColor 目标颜色（十六进制）
     * @param loginUser 当前登录用户
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 创建 AI 扩图任务
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(
            CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

}
