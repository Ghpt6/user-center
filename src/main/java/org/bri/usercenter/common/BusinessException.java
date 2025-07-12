package org.bri.usercenter.common;

import lombok.Getter;

/**
 * 自定义全局异常
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String detail;

    public BusinessException(String message, int code, String detail) {
        super(message);
        this.code = code;
        this.detail = detail;
    }


    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.detail = detail;
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDetail());
    }
}
