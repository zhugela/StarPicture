package com.yu.backend.aop;

import com.yu.backend.annotation.AuthCheck;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.UserRoleEnums;
import com.yu.backend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    //我已经定义了就是如果加了@AuthRequired注解的方法就会被这个切面拦截到，然后在这个切面里面我就可以做一些权限校验的逻辑，比如说我可以从请求头里面获取到用户的token，然后通过这个token去查询用户的信息，看看这个用户是否有权限访问这个方法，如果没有权限的话我就可以抛出一个异常，告诉用户没有权限访问这个方法。
//      现在就是要吧这个注解定义出来变成可以拦截的注解
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
    //下一步是不是就是要拿到用户登陆时传进来的角色
        String mustRole = authCheck.mustRole();
        //拿到用户的角色
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        //获取当前登录用户
        User loginuser = userService.getLoginUser(request);
        //获取当前用户的角色
        UserRoleEnums mustRoleEnum = UserRoleEnums.getEnumByValue(mustRole);
        //如果不需要权限放行
        if(mustRoleEnum == null){
            return joinPoint.proceed();
        }
        //以下的代码：必须有权限，拒绝
        UserRoleEnums userRoleEnum = UserRoleEnums.getEnumByValue(loginuser.getUserRole());
        if(userRoleEnum == null ){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //要求必须有管理员权限,但用户没有管理员权限，拒绝
        if(mustRoleEnum.equals(UserRoleEnums.ADMIN) && !userRoleEnum.equals(UserRoleEnums.ADMIN)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //通过权限校验，放行
        return joinPoint.proceed();

    }
}
