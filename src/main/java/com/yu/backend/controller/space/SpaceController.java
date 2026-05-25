package com.yu.backend.controller.space;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.backend.annotation.AuthCheck;
import com.yu.backend.common.BaseResponse;
import com.yu.backend.common.ResultUtils;
import com.yu.backend.constant.UserConstant;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.space.SpaceAddRequest;
import com.yu.backend.model.dto.space.SpaceDeleteRequest;
import com.yu.backend.model.dto.space.SpaceEditRequest;
import com.yu.backend.model.dto.space.SpaceQueryRequest;
import com.yu.backend.model.dto.space.SpaceUpdateRequest;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.SpaceLevelEnum;
import com.yu.backend.model.vo.SpaceLevel;
import com.yu.backend.model.vo.SpaceVO;
import com.yu.backend.service.SpaceService;
import com.yu.backend.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 空间接口（持久化经 {@link SpaceService} → Mapper）
 */
@Slf4j
@RestController
@RequestMapping("/space")
@AllArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;
    private final UserService userService;

    /**
     * 空间级别枚举列表（创建空间等场景下拉用）
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(e -> new SpaceLevel(e.getValue(), e.getText(), e.getMaxCount(), e.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }

    /**
     * 创建空间（与前端 OpenAPI 路径 {@code /save} 一致）
     */
    @PostMapping("/save")
    public BaseResponse<Long> saveSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        return doAddSpace(spaceAddRequest, request);
    }

    /**
     * 创建空间（同义路径）
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        return doAddSpace(spaceAddRequest, request);
    }

    private BaseResponse<Long> doAddSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(spaceId);
    }

    /**
     * 根据 id 获取空间（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(space);
    }

    /**
     * 根据 id 获取空间（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceService.getSpaceVO(space, request));
    }

    /**
     * 分页获取空间列表（仅管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页获取空间列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                           HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        ThrowUtils.throwIf(size <= 0 || size > 20, ErrorCode.PARAMS_ERROR, "pageSize 不能超过 20");
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
    }

    /**
     * 编辑空间（本人或管理员）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceEditRequest == null || spaceEditRequest.getId() == null || spaceEditRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);

        Space space = spaceService.getById(spaceEditRequest.getId());
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        BeanUtil.copyProperties(spaceEditRequest, space, CopyOptions.create().ignoreNullValue());

        spaceService.fillSpaceBySpaceLevel(space);
        space.setEditTime(new Date());
        spaceService.validSpace(space, false);

        User loginUser = userService.getLoginUser(request);
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除空间（本人或管理员逻辑由 Service 校验）
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody SpaceDeleteRequest spaceDeleteRequest,
                                             HttpServletRequest request) {
        ThrowUtils.throwIf(spaceDeleteRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!spaceService.deleteSpace(spaceDeleteRequest, loginUser),
                ErrorCode.SYSTEM_ERROR, "删除失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新空间（管理员）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        ThrowUtils.throwIf(
                spaceUpdateRequest == null
                        || spaceUpdateRequest.getId() == null
                        || spaceUpdateRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR,
                "参数错误");

        Space origin = spaceService.getById(spaceUpdateRequest.getId());
        ThrowUtils.throwIf(origin == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

        BeanUtil.copyProperties(
                spaceUpdateRequest,
                origin,
                CopyOptions.create().ignoreNullValue());

        spaceService.validSpace(origin, false);
        spaceService.fillSpaceBySpaceLevel(origin);
        origin.setEditTime(new Date());

        ThrowUtils.throwIf(!spaceService.updateById(origin), ErrorCode.SYSTEM_ERROR, "更新失败");
        return ResultUtils.success(true);
    }
}
