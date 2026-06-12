package com.yu.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yu.backend.config.JwtProperties;
import com.yu.backend.constant.UserConstant;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.user.UserQueryRequest;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.UserRoleEnums;
import com.yu.backend.model.vo.LoginUserVo;
import com.yu.backend.model.vo.UserVO;
import com.yu.backend.service.UserService;
import com.yu.backend.mapper.UserMapper;
import com.yu.backend.utils.JwtUtils;


import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 26228
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-03-09 19:39:59
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private JwtProperties jwtProperties;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验格式
        // 账号密码、验证密码、确认密码 都不能为空
        if (StrUtil.hasBlank(userAccount,userPassword,checkPassword)) {
             //抛出参数错误的异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度不能小于4");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于8");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一样");
        }
//      检查用户数据库里面是否还有重复的账号
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        //统计相同的次数
        long count = this.baseMapper.selectCount(queryWrapper);
        //如果统计次数大于1的话就返回异常
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"重复创建账号");
        }

//        4.把用户注册密码进行加密
        //把最初的密码加盐生成加密密码
        String EncryptPassword = getEncryptPassword(userPassword);
//        5.把用户信息保存到数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(EncryptPassword);
        user.setUserName("帅哥");
        this.save(user);
//        6.返回用户id
        return user.getId();
    }
private static final String salt = "zhuzhu";
    @Override
    public String getEncryptPassword(String userPassword) {
        //1.定义盐值（固定字符串，防止彩虹表破解）
        //2.加md5盐获取加密后的密码，
        String userEncryptPassword = DigestUtils.md5DigestAsHex((salt+userPassword).getBytes());
        //3.返回加密后密码
        return userEncryptPassword;
    }
    /*
      *用户登录
     */
    @Override
    public LoginUserVo userlogin(String userAccount, String userPassword, HttpServletRequest httpRequest) {
        //1.校验
//         -账号密码不可以为空
//        -账号长度不能小于4
//        -密码长度不能小于8
        if(StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号密码不能为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度不能小于4");
        }
        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于8");
        }
        //2.对用户输入的密码进行加密
        String entryptpassword = getEncryptPassword(userPassword);
        //3.根据账号和加密后的密码去查数据库用户
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",entryptpassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        //-查不到还要返回账号密码异常
        if(user == null){
          throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号密码异常");
        }
        //4.将用户信息存到Session，记录登录态
        httpRequest.getSession().setAttribute(UserConstant.USER_LOGIN,user);

        LoginUserVo loginUserVo = this.getLoginUserVO(user);
        loginUserVo.setToken(JwtUtils.createToken(user.getId(),
                jwtProperties.getSecret(),
                jwtProperties.getExpireDays()));
        return loginUserVo;
    }
    /**
     * 获取脱敏类的用户信息
     *
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVo getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtil.copyProperties(user, loginUserVo);
        return loginUserVo;
    }
    /*
     * 获取当前登录用户信息
     * @param request 请求
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        User tokenUser = getLoginUserByToken(request);
        if (tokenUser != null) {
            return tokenUser;
        }
        // 1. 从 Session 中取出用户信息
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    private User getLoginUserByToken(HttpServletRequest request) {
        String authHeader = request.getHeader(UserConstant.AUTHORIZATION_HEADER);
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith(UserConstant.TOKEN_PREFIX)) {
            return null;
        }
        String token = authHeader.substring(UserConstant.TOKEN_PREFIX.length()).trim();
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            Long userId = JwtUtils.getUserId(token, jwtProperties.getSecret());
            User user = this.getById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            return user;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录已失效");
        }
    }

    /**
     * 获取脱敏后的信息
     * @param user
     * @return
     */

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO  userVO = new UserVO();
         BeanUtil.copyProperties(user, userVO);
        return userVO;

    }

    @Override
    public List<UserVO> getUserVOList(List<User> userVOList) {
        if (userVOList == null) {
            return null;
        }
        List<UserVO> userVOList1 = BeanUtil.copyToList(userVOList, UserVO.class);
        return userVOList1;
    }

    /**
     *  获取查询后的对象
     * @param userQueryRequest
     * @return
     */

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        //1.创建查询对象
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(userQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.根据查询对象啊生成查询条件
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        Long id = userQueryRequest.getId();
        String userRole = userQueryRequest.getUserRole();
        String userprofile = userQueryRequest.getUserProfile();

        if (id != null && id > 0) {
            queryWrapper.eq("id", id);
        }
        queryWrapper.like(StrUtil.isNotBlank(userName),"userName",userName);
        queryWrapper.like(StrUtil.isNotBlank(userAccount),"userAccount",userAccount);
        queryWrapper.eq(StrUtil.isNotBlank(userRole),"userRole",userRole);
        queryWrapper.like(StrUtil.isNotBlank(userprofile),"userProfile",userprofile);
        //4.返回查询对象
        return queryWrapper;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN);
        if (userObj != null) {
            request.getSession().removeAttribute(UserConstant.USER_LOGIN);
        }
        return true;
    }

    /**
     * 判断用户是不是管理员
     * @param user
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        // 数据库存 userRole 为英文码（如 admin），与枚举的 text 字段一致，勿用 getValue()（为中文展示名）
        return user != null && UserRoleEnums.ADMIN.getText().equals(user.getUserRole());
    }


}




