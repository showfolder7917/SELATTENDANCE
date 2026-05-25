package com.sp.selfsp.common.web;

// 统一响应壳用于把异常收口成固定 JSON 结构。
import com.sp.selfsp.common.util.CommonResponse;
// HttpStatus 负责标记失败接口的 HTTP 状态。
import org.springframework.http.HttpStatus;
// ResponseEntity 负责同时返回状态码和响应体。
import org.springframework.http.ResponseEntity;
// ExceptionHandler 用于声明异常收口入口。
import org.springframework.web.bind.annotation.ExceptionHandler;
// RestControllerAdvice 负责对所有控制器统一生效。
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常收口器。
 */
// 声明 RestControllerAdvice 注解，让当前代码接入既定框架能力。
@RestControllerAdvice
// 定义 全局异常处理，承接当前文件对应的业务职责。
public class GlobalExceptionHandler {

    /**
     * 处理参数和业务校验异常。
     *
     * @param error 异常对象
     * @return 统一失败响应
     */
    // 声明 ExceptionHandler 注解，让当前代码接入既定框架能力。
    @ExceptionHandler(IllegalArgumentException.class)
    // 定义 handleIllegalArgument 处理入口，承接当前业务动作。
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgument(IllegalArgumentException error) {
        // 缺省消息兜底成固定文案，避免前端拿到空字符串。
        // 执行当前业务步骤，推进本行对应的 general 处理。
        String message = error.getMessage() == null || error.getMessage().isBlank()
            // 执行当前业务步骤，推进本行对应的 general 处理。
            ? "请求参数不合法"
            // 执行当前业务步骤，推进本行对应的 general 处理。
            : error.getMessage();
        // 参数或业务校验失败统一返回 400。
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return ResponseEntity
            // 执行当前业务步骤，推进本行对应的 general 处理。
            .status(HttpStatus.BAD_REQUEST)
            // 执行当前业务步骤，推进本行对应的 general 处理。
            .body(CommonResponse.failure(400, message));
    }
}
