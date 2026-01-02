package com.xiaowang.xwpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaowang.xwpicturebackend.constant.UserConstant;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.model.dto.user.UserQueryRequest;
import com.xiaowang.xwpicturebackend.model.entity.User;
import com.xiaowang.xwpicturebackend.model.enums.UserRoleEnum;
import com.xiaowang.xwpicturebackend.model.vo.LoginUserVO;
import com.xiaowang.xwpicturebackend.model.vo.UserVO;
import com.xiaowang.xwpicturebackend.service.UserService;
import com.xiaowang.xwpicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangjialeNB
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2026-01-02 13:08:01
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return 新用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1、校验参数
        if (StringUtils.isBlank(userAccount) || StringUtils.isBlank(userPassword) || StringUtils.isBlank(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!checkPassword.equals(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }
        //2、检查用户是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号已存在");
        }
        //3、密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //4、插入用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户注册失败");
        }
        return user.getId();
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        final String SALT = "xw-picture";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }


    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1、校验参数
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
        }
        //2、用户传来的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        //3、查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed , userAccount cannot math userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号不存在或密码错误");
        }
//        User user = this.lambdaQuery().eq(User::getUserAccount, userAccount).eq(User::getUserPassword, encryptPassword).one();
//        if (user == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号不存在或密码错误");
//        }
        //4、保存用户信息到session
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS_KEY, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取登录用户信息VO
     *
     * @param user
     * @return 登录用户信息VO
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS_KEY);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS_KEY);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS_KEY);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), StrUtil.equals(sortOrder, "ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollectionUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }

        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

}




