package com.yu.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.mapper.SpaceUserMapper;
import com.yu.backend.mapper.UserMapper;
import com.yu.backend.model.dto.space.SpaceUserAddRequest;
import com.yu.backend.model.dto.space.SpaceUserQueryRequest;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.SpaceUser;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.SpaceRoleEnum;
import com.yu.backend.model.vo.SpaceUserVO;
import com.yu.backend.model.vo.SpaceVO;
import com.yu.backend.service.SpaceService;
import com.yu.backend.service.SpaceUserService;
import com.yu.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserService {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        if (StrUtil.isBlank(spaceUser.getSpaceRole())) {
            spaceUser.setSpaceRole(SpaceRoleEnum.VIEWER.getValue());
        }
        validSpaceUser(spaceUser, true);
        Date now = new Date();
        spaceUser.setCreateTime(now);
        spaceUser.setUpdateTime(now);
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }

    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (add) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            User user = userMapper.selectById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        String spaceRole = spaceUser.getSpaceRole();
        if (spaceRole != null && SpaceRoleEnum.getEnumByValue(spaceRole) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
        }
    }

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        if (spaceUserVO == null) {
            return null;
        }
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userMapper.selectById(userId);
            spaceUserVO.setUser(userService.getUserVO(user));
        }
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            spaceUserVO.setSpace(spaceService.getSpaceVO(space, request));
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream()
                .map(SpaceUserVO::objToVo)
                .collect(Collectors.toList());

        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());

        Map<Long, User> userMap = userMapper.selectBatchIds(userIdSet).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Space> spaceMap = spaceService.listByIds(spaceIdSet).stream()
                .collect(Collectors.toMap(Space::getId, s -> s, (a, b) -> a));

        spaceUserVOList.forEach(spaceUserVO -> {
            User user = userMap.get(spaceUserVO.getUserId());
            spaceUserVO.setUser(userService.getUserVO(user));
            Space space = spaceMap.get(spaceUserVO.getSpaceId());
            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        });
        return spaceUserVOList;
    }

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(StrUtil.isNotBlank(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }
}
