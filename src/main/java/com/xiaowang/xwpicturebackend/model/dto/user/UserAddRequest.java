package com.xiaowang.xwpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = -7303086446237302737L;

    /**
     * 账号
     */
    private String userAccount;


    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色:user/admin
     */
    private String userRole;


}
