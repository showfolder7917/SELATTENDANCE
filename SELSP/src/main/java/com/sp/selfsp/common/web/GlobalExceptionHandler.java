package com.sp.selfsp.common.web;

import com.sp.selfsp.common.util.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常收口器。
 *
 * <p>把业务校验失败统一转换成固定响应结构，避免各控制器各自返回不同格式。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数和业务校验异常。
     *
     * @param error 异常对象
     * @return 统一失败响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgument(IllegalArgumentException error) {
        // 某些校验异常可能没有显式消息，这里补默认文案，避免前端收到空错误提示。
        String message = error.getMessage() == null || error.getMessage().isBlank()
            ? "请求参数不合法"
            : error.getMessage();
        // 业务校验失败统一收口成 400 和标准响应壳，便于页面统一处理错误提示。
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(CommonResponse.failure(400, message));
    }
}
