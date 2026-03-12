package com.yu.backend.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.backend.annotation.AuthCheck;
import com.yu.backend.common.BaseResponse;
import com.yu.backend.common.DeleteRequest;
import com.yu.backend.common.ResultUtils;
import com.yu.backend.constant.UserConstant;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.user.UserAddRequest;
import com.yu.backend.model.dto.user.UserLoginRequest;
import com.yu.backend.model.dto.user.UserQueryRequest;
import com.yu.backend.model.dto.user.UserRegisterRequest;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.entity.UserUpdateRequest;
import com.yu.backend.model.vo.LoginUserVo;
import com.yu.backend.model.vo.UserVO;
import com.yu.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 检查参数
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        // 调用服务注册
        long userId = userService.userRegister(userAccount, userPassword, checkPassword);

        // 返回成功数据
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        // 登录并返回用户信息
        LoginUserVo loginUserVo = userService.userlogin(userAccount, userPassword, request);

        return ResultUtils.success(loginUserVo);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 添加用户（管理员权限）
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        // 检查参数
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);

        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);

        // 设置默认密码，并进行加密
        String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(userService.getEncryptPassword(DEFAULT_PASSWORD));

        // 保存用户
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(user.getId());
    }

    /**
     * 获取用户信息（管理员权限）
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/get/{id}")
    public BaseResponse<User> getUserById(@PathVariable long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(user);
    }

    /**
     * 获取用户信息 VO（管理员权限）
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/get/vo/")
    public BaseResponse<UserVO> getUserVOById(@PathVariable long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.NOT_FOUND_ERROR);

        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户（管理员权限）
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        // 检查参数
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户信息（管理员权限）
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        // 检查参数
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);

        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);

        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(true);
    }

    /**
     * 获取用户列表（分页，管理员权限）
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        // 检查参数
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();

        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);

        return ResultUtils.success(userVOPage);
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }
}