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
import com.yu.backend.model.dto.user.UserRegisterRequest;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.entity.UserUpdateRequest;
import com.yu.backend.model.vo.LoginUserVo;
import com.yu.backend.model.vo.UserVO;
import com.yu.backend.service.UserService;
import org.apache.coyote.Request;
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
     * 增加用户
     * @param userAddRequest
     * @return
     */

    @AuthCheck(mustRole = "admin")
    @GetMapping("/get")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        //1.首先要检验参数
        ThrowUtils.throwIf(userAddRequest == null,ErrorCode.PARAMS_ERROR);
        //2.把参数复制到user对象里面去
        User user = new User();
        BeanUtils.copyProperties(userAddRequest,user);
        //3.设置一个默认密码常量，还要对密码进行加密
         String DEFAULT_PASSWORD = "12345678";
         user.setUserPassword(userService.getEncryptPassword(DEFAULT_PASSWORD));
        //4.返回成功之后的数据
        return ResultUtils.success(user.getId());
    }
    /**
     * 根据id获取用户信息
     * @param id
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/get")
    public BaseResponse<User> getUserBuId(Long id){
        //1.检查参数
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);

        //2.从Userservice里面获取用户信息
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_FOUND_ERROR);
        //3.返回成功信息
        return ResultUtils.success(user);
    }

    /**
     * 获取用户的id返回前端
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id){
        //1.检查参数
        ThrowUtils.throwIf(id<=0,ErrorCode.NOT_FOUND_ERROR);

        //2.获取user里面的信息
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);

        //3.把user里面的数据复制到uservo里面
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
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        if(userUpdateRequest == null||userUpdateRequest.getId()<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest,user);
        //调用userservice里面的update方法修改用户信息
        boolean result  = userService.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(true);
    }

    @AuthCheck(mustRole = "admin")
    @GetMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
        //1.检验参数
        ThrowUtils.throwIf(userQueryRequest == null,ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        // 告诉数据库：我要第几页，每页多少条，查询条件是什么

        Page<User> userPage = userService.page(
                new Page<>(current, pageSize),           // 分页参数
                userService.getQueryWrapper(userQueryRequest)  // 查询条件
        );
        Page<UserVO> userVOPage = new Page<>(current,pageSize,userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }



}
