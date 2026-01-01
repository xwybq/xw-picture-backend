package com.xiaowang.xwpicturebackend.common;

import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import lombok.Data;

/**
 * 通用返回类
 *
 * @param <T> 数据类型
 */
@Data
public class BaseResponse<T> {
    private int code;
    private T data;
    private String message;


    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

}
