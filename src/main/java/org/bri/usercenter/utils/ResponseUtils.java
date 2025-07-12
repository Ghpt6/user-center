package org.bri.usercenter.utils;

import org.bri.usercenter.common.BaseResponse;
import org.bri.usercenter.common.ErrorCode;

/**
 * 响应工具类
 */
public class ResponseUtils {
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok", "");
    }

    public static <T> BaseResponse<T> error(ErrorCode code) {
        return new BaseResponse<>(code);
    }

    public static <T> BaseResponse<T> error(ErrorCode code, String message, String detail) {
        return new BaseResponse<>(code, message, detail);
    }

    public static <T> BaseResponse<T> error(int code, String message, String detail) {
        return new BaseResponse<>(code,null, message, detail);
    }

}
