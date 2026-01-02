package com.xiaowang.xwpicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum UserRoleEnum {
    USER("用户", "user"),
    ADMIN("管理员", "admin");


    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.getValue().equals(value)) {
                return userRoleEnum;
            }
        }
        //数据量比较大时，使用map缓存，避免每次遍历
//        Map<String,String> map=new HashMap<>();
//        map.put(USER.getValue(),USER.getText());
//        map.put(ADMIN.getValue(),ADMIN.getText());
        return null;
    }
}
