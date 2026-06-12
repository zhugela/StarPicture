package com.yu.backend.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.backend.annotation.AuthCheck;
import com.yu.backend.common.BaseResponse;
import com.yu.backend.common.DeleteRequest;
import com.yu.backend.common.ResultUtils;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.user.UserAddRequest;
import com.yu.backend.model.dto.user.UserLoginRequest;
import com.yu.backend.model.dto.user.UserQueryRequest;
import com.yu.backend.model.dto.user.UserUpdateMyRequest;
import com.yu.backend.model.dto.user.UserRegisterRequest;
import cn.hutool.core.util.StrUtil;
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

//不知道这个注解到底是干什么的只知道它是必须要用的
@RestController
@RequestMapping("/user")

public class UserController {
    //把service里面的的方法传到里面去
    @Resource
    private UserService userService;
    //就是写这个方法需要获取前端的请求然后再给后端返回参数

    /*
    用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        //当注册请求为空的时候返回空
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        //首先就是需要传注册需要的数据
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        //然后把获取到的参数返回到userService里面去
        long userId = userService.userRegister(userAccount,userPassword,checkPassword);

        //返回给前端数据
        return ResultUtils.success(userId);
    }
    /*
    用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        //当登录请求为空的时候
        ThrowUtils.throwIf(userLoginRequest == null,ErrorCode.PARAMS_ERROR);
        //注入登录的信息
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        //给前端输出数据VO
        LoginUserVo loginUserVo = userService.userlogin(userAccount,userPassword,request);
        //返回成功数据
        return ResultUtils.success(loginUserVo);

    }

    /*
    *  获取用户信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request){
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 当前登录用户更新自己的资料（昵称、头像、简介）
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyProfile(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(userUpdateMyRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        User updateUser = new User();
        updateUser.setId(loginUser.getId());
        if (StrUtil.isNotBlank(userUpdateMyRequest.getUserName())) {
            updateUser.setUserName(userUpdateMyRequest.getUserName().trim());
        }
        if (userUpdateMyRequest.getUserAvatar() != null) {
            updateUser.setUserAvatar(userUpdateMyRequest.getUserAvatar().trim());
        }
        if (userUpdateMyRequest.getUserProfile() != null) {
            updateUser.setUserProfile(userUpdateMyRequest.getUserProfile().trim());
        }
        boolean result = userService.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 增加用户
     * @param userAddRequest
     * @return
     */

    @AuthCheck(mustRole = "admin")
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        //1.首先要检验参数
        ThrowUtils.throwIf(userAddRequest == null,ErrorCode.PARAMS_ERROR);
        //2.把参数复制到user对象里面去
        User user = new User();
        BeanUtils.copyProperties(userAddRequest,user);
        //3.设置一个默认密码常量，还要对密码进行加密
         String DEFAULT_PASSWORD = "12345678";
         user.setUserPassword(userService.getEncryptPassword(DEFAULT_PASSWORD));
         boolean saved = userService.save(user);
         ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "创建用户失败");
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据id获取用户信息
     * @param id
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/get")
    public BaseResponse<UserVO> getUserById(@RequestParam("id") Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 获取用户的id返回前端
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(@RequestParam("id") Long id){
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户（管理员）
     * @param deleteRequest
     * @return
     */


    @AuthCheck(mustRole = "admin")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest){
        //判断删除的用户是否被删除
        if(deleteRequest ==null || deleteRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);

    }

    /**
     * 更新用户列表
     * @param userUpdateRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        // 检查参数
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null
                || userUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

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
        // 告诉数据库：我要第几页，每页多少条，查询条件是什么

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
