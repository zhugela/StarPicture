package com.yu.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yu.backend.constant.UserConstant;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.vo.LoginUserVo;
import com.yu.backend.service.UserService;
import com.yu.backend.mapper.UserMapper;


import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
* @author 26228
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-03-09 19:39:59
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{


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

        return this.getLoginUserVO(user);
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
}




