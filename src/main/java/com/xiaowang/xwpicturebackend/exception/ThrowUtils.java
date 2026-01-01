package com.xiaowang.xwpicturebackend.exception;

public class ThrowUtils {

    /**
     * 业务异常
     *
     * @param condition        条件
     * @param runtimeException 运行时异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 业务异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message   信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
