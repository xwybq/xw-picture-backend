package com.xiaowang.xwpicturebackend.common;

import com.xiaowang.xwpicturebackend.exception.ErrorCode;

public class ResultUtils {

    // 成功方法（不变）
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    // 新增：带泛型的失败方法（核心优化）
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage());
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }

    // 保留原有的无泛型 error 方法（兼容旧代码）
    public static BaseResponse<?> errorWithoutGeneric(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }
}