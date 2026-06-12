package com.yu.backend.controller.space;

import com.yu.backend.annotation.AuthCheck;
import com.yu.backend.common.BaseResponse;
import com.yu.backend.common.ResultUtils;
import com.yu.backend.constant.UserConstant;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.space.SpaceAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceCategoryAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceRankAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceSizeAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceTagAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceUsageAnalyzeRequest;
import com.yu.backend.model.dto.space.SpaceUserAnalyzeRequest;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.vo.SpaceAnalyzeVO;
import com.yu.backend.model.vo.SpaceCategoryAnalyzeResponse;
import com.yu.backend.model.vo.SpaceSizeAnalyzeResponse;
import com.yu.backend.model.vo.SpaceTagAnalyzeResponse;
import com.yu.backend.model.vo.SpaceUserAnalyzeResponse;
import com.yu.backend.model.vo.SpaceUsageAnalyzeResponse;
import com.yu.backend.service.SpaceAnalyzeService;
import com.yu.backend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间 / 图库分析接口
 */
@Api(tags = "空间分析")
@RestController
@RequestMapping("/space/analyze")
@AllArgsConstructor
public class SpaceAnalyzeController {

    private final SpaceAnalyzeService spaceAnalyzeService;
    private final UserService userService;

    @ApiOperation("空间 / 图库统计")
    @PostMapping
    public BaseResponse<SpaceAnalyzeVO> getSpaceAnalyze(@RequestBody SpaceAnalyzeRequest spaceAnalyzeRequest,
                                                        HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceAnalyze(spaceAnalyzeRequest, loginUser));
    }

    @ApiOperation("空间使用状态分析")
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser));
    }

    @ApiOperation("空间分类分析")
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser));
    }

    @ApiOperation("空间标签分析")
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(
            @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser));
    }

    @ApiOperation("空间图片大小分析")
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(
            @RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser));
    }

    @ApiOperation("用户上传时间分析")
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(
            @RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser));
    }

    @ApiOperation("空间使用排行（仅管理员）")
    @PostMapping("/rank")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<Space>> getSpaceRankAnalyze(
            @RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser));
    }
}
