package com.yu.backend.controller;

import com.yu.backend.common.BaseResponse;
import com.yu.backend.common.ResultUtils;
import com.yu.backend.model.dto.UserRegisterRequest;
import com.yu.backend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
//不知道这个注解到底是干什么的只知道它是必须要用的
@RestController
@RequestMapping("/user")

public class UserController {
    //把service里面的的方法传到里面去
    @Resource
    private UserService userService;
    //就是写这个方法需要获取前端的请求然后再给后端返回参数
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        //首先就是需要传注册需要的数据
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        //然后把获取到的参数返回到userService里面去
        long userId = userService.userRegister(userAccount,userPassword,checkPassword);

        //返回给前端数据
        return ResultUtils.success(userId);
    }
}
