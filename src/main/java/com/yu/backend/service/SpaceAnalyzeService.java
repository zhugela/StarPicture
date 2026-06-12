package com.yu.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yu.backend.model.dto.space.SpaceAnalyzeRequest;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.dto.space.SpaceRankAnalyzeRequest;

import java.util.List;
import com.yu.backend.model.dto.space.SpaceCategoryAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceSizeAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceTagAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceUserAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceUsageAnalyzeRequest;
import com.yu.backend.model.vo.SpaceAnalyzeVO;
import com.yu.backend.model.vo.SpaceCategoryAnalyzeResponse;
import com.yu.backend.model.vo.SpaceSizeAnalyzeResponse;
import com.yu.backend.model.vo.SpaceTagAnalyzeResponse;
import com.yu.backend.model.vo.SpaceUserAnalyzeResponse;
import com.yu.backend.model.vo.SpaceUsageAnalyzeResponse;

/**
 * 空间 / 图库分析
 */
public interface SpaceAnalyzeService {

    /**
     * 检验空间分析权限
     */
    void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);

    /**
     * 根据分析范围封装 Picture 查询条件
     */
    QueryWrapper<Picture> getAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest);

    /**
     * 空间 / 图库统计分析
     */
    SpaceAnalyzeVO getSpaceAnalyze(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);

    /**
     * 空间使用分析（容量、数量及占比）
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 空间分类分析
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
                                                               User loginUser);

    /**
     * 空间标签分析
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 空间图片大小分段分析
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 用户上传时间分析
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 空间使用排行（仅管理员）
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
