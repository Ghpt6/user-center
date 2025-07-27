package org.bri.usercenter.common;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.bri.usercenter.utils.ResponseUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    public BaseResponse businessExceptionHandler(BusinessException e) {
        log.error("businessException: ", e);
        return ResponseUtils.error(e.getCode(), e.getMessage(), e.getDetail());
    }

    @ExceptionHandler
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeExceptionHandler", e);
        return ResponseUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }
}
