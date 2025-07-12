package org.bri.usercenter.common;

import lombok.Data;

/**
 * 通用响应类
 *
 * @param <T>
 */
@Data
public class BaseResponse<T> {

    private int code;

    private T data;

    private String message;

    private String detail;

    public BaseResponse(int code, T data, String message, String detail) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.detail = detail;
    }

    public BaseResponse(int code, T data) {
        this.code = code;
        this.data = data;
        this.message = "";
        this.detail = "";
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage(), errorCode.getDetail());
    }

    public BaseResponse(ErrorCode errorCode, String message, String detail) {
        this(errorCode.getCode(), null, message, detail);
    }
}
