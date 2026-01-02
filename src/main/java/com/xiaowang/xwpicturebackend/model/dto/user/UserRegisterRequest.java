package com.xiaowang.xwpicturebackend.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = -2755506298757474822L;
    //用户账号
    private String userAccount;
    //用户密码
    private String userPassword;
    //校验密码
    private String checkPassword;


}
