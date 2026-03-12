package com.yu.backend.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.backend.model.dto.user.UserQueryRequest;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.vo.LoginUserVo;
import com.yu.backend.model.vo.UserVO;
import org.springframework.http.HttpRequest;

import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 26228
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2026-03-09 19:39:59
*/
public interface UserService extends IService<User> {
    /*
    * 用户注册
    * @param userAccount 用户账户
    * @param userPassword 用户密码
    * @param checkPassword 校验密码
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

    /**
    * 获取加密后的密码
     */
    String getEncryptPassword(String userPassword);
    /*
      *用户登录
     */
    LoginUserVo userlogin(String userAccount, String userPassword, HttpServletRequest httpRequest);
    /**
     * 获取脱敏类的用户信息
     *
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    LoginUserVo getLoginUserVO(User user);
    /*
    *获取当前用户信息
     */
    User getLoginUser(HttpServletRequest request);
    /**
     * 获取脱敏后的信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     * @param userVOList
     * @return
     */

    List<UserVO> getUserVOList(List<UserVO> userVOList);
    /**
     * 获取查询对象条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
