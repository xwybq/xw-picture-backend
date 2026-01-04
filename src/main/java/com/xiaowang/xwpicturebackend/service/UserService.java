package com.xiaowang.xwpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaowang.xwpicturebackend.model.dto.user.UserQueryRequest;
import com.xiaowang.xwpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaowang.xwpicturebackend.model.vo.LoginUserVO;
import com.xiaowang.xwpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author wangjialeNB
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2026-01-02 13:08:01
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密后的密码
     *
     * @param userPassword
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return 登录用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取登录用户信息VO
     *
     * @param user
     * @return 登录用户信息VO
     */
    LoginUserVO getLoginUserVO(User user);


    /**
     * 获取登录用户
     *
     * @param request
     * @return 登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return 注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户VO列表
     *
     * @param userList
     * @return 用户VO列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取用户VO
     *
     * @param user
     * @return 用户VO
     */
    UserVO getUserVO(User user);


    /**
     * 获取查询Wrapper
     *
     * @param userQueryRequest
     * @return 查询Wrapper
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user
     * @return 是否为管理员
     */
    boolean isAdmin(User user);
}






