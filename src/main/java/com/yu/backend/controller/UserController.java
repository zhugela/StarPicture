package com.yu.backend.controller;

import com.yu.backend.common.BaseResponse;
import com.yu.backend.common.ResultUtils;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.UserLoginRequest;
import com.yu.backend.model.dto.UserRegisterRequest;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.vo.LoginUserVo;
import com.yu.backend.service.UserService;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
}
