package com.yu.backend.controller.space;

import cn.hutool.core.util.ObjectUtil;
import com.yu.backend.common.BaseResponse;
import com.yu.backend.common.DeleteRequest;
import com.yu.backend.common.ResultUtils;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.space.SpaceUserAddRequest;
import com.yu.backend.model.dto.space.SpaceUserEditRequest;
import com.yu.backend.model.dto.space.SpaceUserQueryRequest;
import com.yu.backend.model.entity.SpaceUser;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.vo.SpaceUserVO;
import com.yu.backend.service.SpaceUserService;
import com.yu.backend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "空间成员")
@RestController
@RequestMapping("/spaceUser")
@Slf4j
@AllArgsConstructor
public class SpaceUserController {

    private final SpaceUserService spaceUserService;
    private final UserService userService;

    @ApiOperation("添加成员到空间")
    @PostMapping({"/manage/add", "/add"})
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceUserService.addSpaceUser(spaceUserAddRequest));
    }

    @ApiOperation("从空间移除成员")
    @PostMapping({"/manage/delete", "/delete"})
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        SpaceUser oldSpaceUser = spaceUserService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!spaceUserService.removeById(deleteRequest.getId()), ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @ApiOperation("查询成员在某空间的信息")
    @PostMapping({"/manage/get", "/get"})
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    @ApiOperation("查询成员列表")
    @PostMapping({"/manage/list", "/list"})
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    @ApiOperation("编辑成员角色")
    @PostMapping({"/manage/edit", "/edit"})
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest) {
        if (spaceUserEditRequest == null || spaceUserEditRequest.getId() == null || spaceUserEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserEditRequest, spaceUser);
        spaceUserService.validSpaceUser(spaceUser, false);
        SpaceUser oldSpaceUser = spaceUserService.getById(spaceUserEditRequest.getId());
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        spaceUser.setUpdateTime(new java.util.Date());
        ThrowUtils.throwIf(!spaceUserService.updateById(spaceUser), ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @ApiOperation("我加入的团队空间列表")
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }
}
