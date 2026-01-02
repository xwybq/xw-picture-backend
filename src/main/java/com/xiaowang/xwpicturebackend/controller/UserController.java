package com.xiaowang.xwpicturebackend.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaowang.xwpicturebackend.annotation.AutoCheck;
import com.xiaowang.xwpicturebackend.common.BaseResponse;
import com.xiaowang.xwpicturebackend.common.PageRequest;
import com.xiaowang.xwpicturebackend.common.ResultUtils;
import com.xiaowang.xwpicturebackend.constant.UserConstant;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.exception.ThrowUtils;
import com.xiaowang.xwpicturebackend.model.dto.user.*;
import com.xiaowang.xwpicturebackend.model.entity.User;
import com.xiaowang.xwpicturebackend.model.vo.LoginUserVO;
import com.xiaowang.xwpicturebackend.model.vo.UserVO;
import com.xiaowang.xwpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return 新用户id
     */
//    @AutoCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return 登录用户信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取登录用户信息VO
     *
     * @param request
     * @return 登录用户信息VO
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     *
     * @param request
     * @return 注销成功
     */

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    @PostMapping("/add")
    @AutoCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        final String DEFAULT_PASSWORD = userService.getEncryptPassword("12345678");
        user.setUserPassword(DEFAULT_PASSWORD);
        final String DEFAULT_USER_NAME = "默认用户名";
        if (StrUtil.isBlank(userAddRequest.getUserName())) {
            user.setUserName(DEFAULT_USER_NAME);
        }
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    //            - 根据id删除用户（**管理员**）
    @AutoCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestParam Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    //            - 更新用户（**管理员**）
    @AutoCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    //            - 分页获取用户列表（**管理员**）
    @AutoCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);


//        Page<UserVO> userVOPage = (Page<UserVO>) userPage.convert(user -> {
//            UserVO userVO = new UserVO();
//            BeanUtils.copyProperties(user, userVO); // 自动拷贝所有同名属性
//            return userVO;
//        });
        return ResultUtils.success(userVOPage);
    }

    //            - 根据id获取用户（未脱敏）（**管理员**）
    @AutoCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    //            - 根据id获取用户（脱敏）
//    @AutoCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get/vo")
    public BaseResponse<LoginUserVO> getLoginUserVOById(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

}
