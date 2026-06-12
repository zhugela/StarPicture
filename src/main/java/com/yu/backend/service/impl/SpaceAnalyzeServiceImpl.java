package com.yu.backend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yu.backend.constant.PictureConstant;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.mapper.PictureMapper;
import com.yu.backend.mapper.SpaceMapper;
import com.yu.backend.model.dto.space.SpaceAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceCategoryAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceRankAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceSizeAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceTagAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceUsageAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceUserAnalyzeRequest;
import com.yu.backend.model.enums.TimeDimensionEnum;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.vo.SpaceAnalyzeVO;
import com.yu.backend.model.vo.SpaceCategoryAnalyzeResponse;
import com.yu.backend.model.vo.SpaceSizeAnalyzeResponse;
import com.yu.backend.model.vo.SpaceTagAnalyzeResponse;
import com.yu.backend.model.vo.SpaceUserAnalyzeResponse;
import com.yu.backend.model.vo.SpaceUsageAnalyzeResponse;
import com.yu.backend.service.SpaceAnalyzeService;
import com.yu.backend.service.SpaceService;
import com.yu.backend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private SpaceMapper spaceMapper;

    private static final int MAX_RANK_TOP_N = 50;

    @Override
    public void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权限查询空间信息");
        }
        if (Objects.nonNull(spaceAnalyzeRequest.getSpaceId()) && spaceAnalyzeRequest.getSpaceId() > 0L) {
            Space space = spaceService.getById(spaceAnalyzeRequest.getSpaceId());
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!userService.isAdmin(loginUser) && !space.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查询空间信息");
            }
        }
    }

    @Override
    public QueryWrapper<Picture> getAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceAnalyzeRequest, queryWrapper);
        return queryWrapper;
    }

    @Override
    public SpaceAnalyzeVO getSpaceAnalyze(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // 先权限校验
        checkSpaceAnalyzeAuth(spaceAnalyzeRequest, loginUser);
        // 封装查询条件
        QueryWrapper<Picture> queryWrapper = getAnalyzeQueryWrapper(spaceAnalyzeRequest);

        List<Picture> pictureList = pictureMapper.selectList(queryWrapper);
        long totalCount = pictureList.size();
        long totalSize = pictureList.stream()
                .map(Picture::getPicSize)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        SpaceAnalyzeVO vo = new SpaceAnalyzeVO();
        vo.setTotalCount(totalCount);
        vo.setTotalSize(totalSize);
        return vo;
    }

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
                                                          User loginUser) {
        checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);

        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        pictureQueryWrapper.select("picSize");
        fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, pictureQueryWrapper);

        List<Object> picSizeList = pictureMapper.selectObjs(pictureQueryWrapper);
        long usedCount = picSizeList.size();
        long usedSize = sumPicSize(picSizeList);

        if (spaceUsageAnalyzeRequest.isQueryPublic() || spaceUsageAnalyzeRequest.isQueryAll()) {
            return SpaceUsageAnalyzeResponse.builder()
                    .usedSize(usedSize)
                    .usedCount(usedCount)
                    .build();
        }

        Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
        if (spaceId != null && spaceId > 0L) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(loginUser, space);

            double sizeUsageRatio = calcUsageRatio(space.getTotalSize(), space.getMaxSize());
            double countUsageRatio = calcUsageRatio(space.getTotalCount(), space.getMaxCount());

            return SpaceUsageAnalyzeResponse.builder()
                    .maxCount(space.getMaxCount())
                    .maxSize(space.getMaxSize())
                    .sizeUsageRatio(sizeUsageRatio)
                    .countUsageRatio(countUsageRatio)
                    .usedSize(usedSize)
                    .usedCount(usedCount)
                    .build();
        }

        return new SpaceUsageAnalyzeResponse();
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(
            SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        // 校验查询范围（queryAll / queryPublic / spaceId 三选一）
        getAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest);

        List<Map<String, Object>> categoryStatistics =
                pictureMapper.getCategoryStatistics(spaceCategoryAnalyzeRequest);

        return categoryStatistics.stream()
                .filter(Objects::nonNull)
                .map(this::toCategoryAnalyzeResponse)
                .collect(Collectors.toList());
    }

    private SpaceCategoryAnalyzeResponse toCategoryAnalyzeResponse(Map<String, Object> map) {
        Object countObj = map.get("count");
        Object totalSizeObj = map.get("totalSize");
        Object categoryObj = map.get("category");

        long count = countObj instanceof Number ? ((Number) countObj).longValue() : 0L;
        long totalSize = totalSizeObj instanceof Number ? ((Number) totalSizeObj).longValue() : 0L;
        String category = categoryObj instanceof String && StrUtil.isNotBlank((String) categoryObj)
                ? (String) categoryObj
                : "未分类";

        return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,
                                                            User loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);

        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        queryWrapper.select("tags");

        List<String> tagsJsonList = pictureMapper.selectObjs(queryWrapper).stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        Map<String, Long> tagCountMap = tagsJsonList.stream()
                .flatMap(tagsJson -> parseTagsJson(tagsJson).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        return tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private static final long SIZE_100_KB = 100L * 1024;
    private static final long SIZE_500_KB = 500L * 1024;
    private static final long SIZE_1_MB = 1024L * 1024;

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
                                                              User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);

        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
        queryWrapper.select("picSize");

        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        pictureMapper.selectObjs(queryWrapper).stream()
                .filter(ObjUtil::isNotNull)
                .filter(obj -> obj instanceof Number)
                .mapToLong(obj -> ((Number) obj).longValue())
                .forEach(picSize -> putSizeRange(sizeRanges, picSize));

        return sizeRanges.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private static void putSizeRange(Map<String, Long> sizeRanges, long picSize) {
        if (picSize < SIZE_100_KB) {
            sizeRanges.put("<100KB", sizeRanges.getOrDefault("<100KB", 0L) + 1);
        } else if (picSize < SIZE_500_KB) {
            sizeRanges.put("100KB-500KB", sizeRanges.getOrDefault("100KB-500KB", 0L) + 1);
        } else if (picSize < SIZE_1_MB) {
            sizeRanges.put("500KB-1MB", sizeRanges.getOrDefault("500KB-1MB", 0L) + 1);
        } else {
            sizeRanges.put(">1MB", sizeRanges.getOrDefault(">1MB", 0L) + 1);
        }
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest,
                                                              User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        getAnalyzeQueryWrapper(spaceUserAnalyzeRequest);

        Long userId = spaceUserAnalyzeRequest.getUserId();
        if (userId != null && userId > 0
                && !userService.isAdmin(loginUser)
                && !loginUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查询该用户数据");
        }

        String timeDimension = TimeDimensionEnum.getEnumByValue(spaceUserAnalyzeRequest.getTimeDimension()).getValue();

        Map<String, Object> params = fillAnalyzeQueryMap(spaceUserAnalyzeRequest);
        params.put("timeDimension", timeDimension);
        if (userId != null && userId > 0) {
            params.put("userId", userId);
        }

        List<Map<String, Object>> queryResult = pictureMapper.analyzeByTimeDimension(params);
        return queryResult.stream()
                .filter(Objects::nonNull)
                .map(result -> {
                    String period = result.get("period") != null ? result.get("period").toString() : "";
                    Object countObj = result.get("count");
                    long count = countObj instanceof Number ? ((Number) countObj).longValue() : 0L;
                    return new SpaceUserAnalyzeResponse(period, count);
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> fillAnalyzeQueryMap(SpaceAnalyzeRequest request) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("queryAll", request.isQueryAll());
        params.put("queryPublic", request.isQueryPublic());
        params.put("spaceId", request.getSpaceId());
        return params;
    }

    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权查看空间排行");

        Integer topN = spaceRankAnalyzeRequest.getTopN();
        if (topN == null || topN <= 0) {
            topN = 10;
        }
        if (topN > MAX_RANK_TOP_N) {
            topN = MAX_RANK_TOP_N;
        }
        return spaceMapper.getTopNSpaceUsage(topN);
    }

    private List<String> parseTagsJson(String tagsJson) {
        if (StrUtil.isBlank(tagsJson)) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(tagsJson, String.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static long sumPicSize(List<Object> picSizeList) {
        return picSizeList.stream()
                .filter(Objects::nonNull)
                .mapToLong(obj -> obj instanceof Number ? ((Number) obj).longValue() : 0L)
                .sum();
    }

    private static double calcUsageRatio(Long used, Long max) {
        if (used == null || max == null || max <= 0L) {
            return 0D;
        }
        return NumberUtil.round(used * 100.0 / max, 2).doubleValue();
    }

    /**
     * 填充 QueryWrapper 对应属性
     */
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest,
                                                QueryWrapper<Picture> queryWrapper) {
        if (spaceAnalyzeRequest.isQueryAll()) {
            return;
        }
        if (spaceAnalyzeRequest.isQueryPublic()) {
            queryWrapper.eq("spaceId", PictureConstant.PUBLIC_SPACE_ID);
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }
}
