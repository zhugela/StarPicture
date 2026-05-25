package com.yu.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.backend.model.dto.space.SpaceAddRequest;
import com.yu.backend.model.dto.space.SpaceDeleteRequest;
import com.yu.backend.model.dto.space.SpaceQueryRequest;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 空间 Service
 */
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间
     *
     * @param space 空间
     * @param add   是否为创建时校验
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间包装类（单条）
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间包装类（分页）
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间级别填充限额（maxSize、maxCount 为空时按级别默认填充）
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 删除空间及其下图片（逻辑删），并尝试删除 COS 文件
     */
    boolean deleteSpace(SpaceDeleteRequest spaceDeleteRequest, User loginUser);

    /**
     * 空间内新增一条图片记录后的用量：totalCount + 1，totalSize += picSize（需在事务内调用）
     */
    boolean increaseUsageForNewPicture(Long spaceId, long picSize);

    /**
     * 空间内图片文件大小变化：totalSize += deltaPicSize，条数不变（需在事务内调用）
     */
    boolean adjustTotalSizeByDelta(Long spaceId, long deltaPicSize);

    /**
     * 删除空间内一条图片后的用量：totalCount - 1，totalSize -= picSize（需在事务内调用）
     */
    boolean delPictureUpdateSpaceUsage(Long spaceId, Long picSize);
}
