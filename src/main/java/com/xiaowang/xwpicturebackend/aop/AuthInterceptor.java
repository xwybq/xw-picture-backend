package com.xiaowang.xwpicturebackend.aop;

import com.xiaowang.xwpicturebackend.annotation.AutoCheck;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.model.entity.User;
import com.xiaowang.xwpicturebackend.model.enums.UserRoleEnum;
import com.xiaowang.xwpicturebackend.model.vo.LoginUserVO;
import com.xiaowang.xwpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    /**
     * 拦截方法，检查用户权限
     *
     * @param joinPoint
     * @param autoCheck
     * @return
     * @throws Throwable
     */
    @Around("@annotation(autoCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AutoCheck autoCheck) throws Throwable {
        //1、从注解中获取必须的角色
        String mustRole = autoCheck.mustRole();
        //2、从请求中获取登录用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        // 检查必须的角色是否为空
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 检查登录用户角色是否为空
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //3、如果必须是管理员角色，但是登录用户并没有管理员权限，抛出异常
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
