package com.yu.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.manager.CosManager;
import com.yu.backend.mapper.PictureMapper;
import com.yu.backend.mapper.SpaceMapper;
import com.yu.backend.mapper.UserMapper;
import com.yu.backend.model.dto.space.SpaceAddRequest;
import com.yu.backend.model.dto.space.SpaceDeleteRequest;
import com.yu.backend.model.dto.space.SpaceQueryRequest;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.Urls;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.SpaceLevelEnum;
import com.yu.backend.model.vo.SpaceVO;
import com.yu.backend.model.vo.UserVO;
import com.yu.backend.service.SpaceService;
import com.yu.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 空间 Service 实现（持久化仅依赖 SpaceMapper、PictureMapper）
 */
@Slf4j
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private UserService userService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private PictureMapper pictureMapper;
    @Resource
    private CosManager cosManager;

    private final Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    private boolean isExistSpaceByUserId(Long userId) {
        if (userId == null) {
            return false;
        }
        return this.lambdaQuery().eq(Space::getUserId, userId).count() > 0;
    }

    private List<Picture> listPicturesBySpaceIdAndUserId(Long spaceId, Long ownerUserId) {
        if (spaceId == null || ownerUserId == null) {
            return new ArrayList<>();
        }
        return pictureMapper.selectList(
                new LambdaQueryWrapper<Picture>()
                        .eq(Picture::getSpaceId, spaceId)
                        .eq(Picture::getUserId, ownerUserId));
    }

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        Space space = buildInsertSpace(spaceAddRequest, loginUser);
        validSpace(space, true);

        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (!userService.isAdmin(loginUser) && spaceLevelEnum != SpaceLevelEnum.COMMON) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建此级别空间");
        }

        Object lock = lockMap.computeIfAbsent(loginUser.getId(), k -> new Object());
        synchronized (lock) {
            try {
                Long newSpaceId = transactionTemplate.execute(status -> {
                    boolean isExist = isExistSpaceByUserId(loginUser.getId());
                    ThrowUtils.throwIf(isExist, ErrorCode.SYSTEM_ERROR, "用户已存在私有空间");
                    ThrowUtils.throwIf(!this.save(space), ErrorCode.SYSTEM_ERROR, "创建失败");
                    return space.getId();
                });
                return Optional.ofNullable(newSpaceId).orElse(-1L);
            } finally {
                lockMap.remove(loginUser.getId());
            }
        }
    }

    private Space buildInsertSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        space.setUserId(loginUser.getId());
        space.setTotalSize(0L);
        space.setTotalCount(0L);
        Date now = new Date();
        space.setCreateTime(now);
        space.setEditTime(now);
        space.setUpdateTime(now);
        fillSpaceBySpaceLevel(space);
        return space;
    }

    @Override
    public boolean deleteSpace(SpaceDeleteRequest spaceDeleteRequest, User loginUser) {
        ThrowUtils.throwIf(spaceDeleteRequest == null, ErrorCode.PARAMS_ERROR);
        final Long spaceId = spaceDeleteRequest.getSpaceId();
        Space space = validDeleteSpace(spaceId, loginUser);

        transactionTemplate.execute(status -> {
            ThrowUtils.throwIf(!this.removeById(spaceId), ErrorCode.OPERATION_ERROR, "删除失败");
            List<Picture> pictures = listPicturesBySpaceIdAndUserId(spaceId, space.getUserId());
            if (!CollectionUtils.isEmpty(pictures)) {
                List<Long> ids = pictures.stream().map(Picture::getId).collect(Collectors.toList());
                pictureMapper.deleteBatchIds(ids);
                pictures.forEach(this::clearCosFilesForPicture);
            }
            return null;
        });
        return true;
    }

    private Space validDeleteSpace(Long spaceId, User loginUser) {
        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "参数错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Space space = this.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return space;
    }

    /**
     * 删除 COS 上的主图与缩略图（与 {@link PictureServiceImpl#clearPictureFile} 行为一致）
     */
    private void clearCosFilesForPicture(Picture picture) {
        if (picture == null || picture.getUrls() == null) {
            return;
        }
        Urls urls = picture.getUrls();
        List<String> keys = new ArrayList<>();
        String k1 = cosManager.keyFromPublicUrl(urls.getUrl());
        String k2 = cosManager.keyFromPublicUrl(urls.getThumbnailUrl());
        if (StrUtil.isNotBlank(k1)) {
            keys.add(k1);
        }
        if (StrUtil.isNotBlank(k2) && !Objects.equals(k1, k2)) {
            keys.add(k2);
        }
        if (!keys.isEmpty()) {
            cosManager.deleteObjects(keys);
        }
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum.getEnumByValue(spaceLevel);

        if (add) {
            if (StringUtils.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
        }
        if (StringUtils.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }

    @Override
    public boolean increaseUsageForNewPicture(Long spaceId, long picSize) {
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
        return this.lambdaUpdate()
                .eq(Space::getId, spaceId)
                .setSql("totalSize = totalSize + " + picSize)
                .setSql("totalCount = totalCount + 1")
                .update();
    }

    @Override
    public boolean adjustTotalSizeByDelta(Long spaceId, long deltaPicSize) {
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
        if (deltaPicSize == 0) {
            return true;
        }
        return this.lambdaUpdate()
                .eq(Space::getId, spaceId)
                .setSql("totalSize = totalSize + (" + deltaPicSize + ")")
                .update();
    }

    @Override
    public boolean delPictureUpdateSpaceUsage(Long spaceId, Long picSize) {
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
        long bytes = picSize != null ? picSize : 0L;
        return this.lambdaUpdate()
                .eq(Space::getId, spaceId)
                .setSql("totalSize = totalSize - " + bytes)
                .setSql("totalCount = totalCount - 1")
                .update();
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        if (spaceVO == null) {
            return null;
        }
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userMapper.selectById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());

        Set<Long> userIdSet = spaceList.stream()
                .map(Space::getUserId)
                .filter(uid -> uid != null && uid > 0)
                .collect(Collectors.toSet());
        Map<Long, User> userIdUserMap = new HashMap<>();
        if (!userIdSet.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIdSet);
            for (User u : users) {
                userIdUserMap.put(u.getId(), u);
            }
        }
        spaceVOList.forEach(spaceVO -> {
            Long uid = spaceVO.getUserId();
            User user = uid == null ? null : userIdUserMap.get(uid);
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        ThrowUtils.throwIf(space.getSpaceLevel() == null, ErrorCode.PARAMS_ERROR, "空间级别不能为空");
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        long maxSize = spaceLevelEnum.getMaxSize();
        if (space.getMaxSize() == null) {
            space.setMaxSize(maxSize);
        }
        long maxCount = spaceLevelEnum.getMaxCount();
        if (space.getMaxCount() == null) {
            space.setMaxCount(maxCount);
        }
    }
}
