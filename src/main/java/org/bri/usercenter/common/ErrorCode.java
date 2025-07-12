package org.bri.usercenter.common;

import lombok.Getter;

/**
 * 错误代码
 */
@Getter
public enum ErrorCode {
    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据未空", ""),
    NO_AUTH_ERROR(40100, "无权限", ""),
    NO_LOGIN_ERROR(40101, "未登录", ""),
    SYSTEM_ERROR(50000, "系统异常", ""),
    ;


    private final int code;
    /**
     * 错误信息
     */
    private final String message;
    /**
     * 错误信息（详情）
     */
    private final String detail;

    ErrorCode(int code, String message, String detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

}