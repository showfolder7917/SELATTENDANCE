package com.sp.selfsp.common.util;

/**
 * 统一响应壳，保证所有接口都返回 code、message、data 三段结构。
 *
 * @param <T> 业务数据类型
 */
public class CommonResponse<T> {

    // 业务码用于让前端统一区分成功、参数错误和其他失败场景。
    private int code;

    // 提示语用于直接显示本次接口处理结果或失败原因。
    private String message;

    // data 统一承接控制器真正要返回的业务对象或列表。
    private T data;

    /**
     * 组装成功响应。
     *
     * @param data 业务数据
     * @param <T> 数据类型
     * @return 标准成功壳
     */
    public static <T> CommonResponse<T> success(T data) {
        // 成功场景统一在这里封装，避免每个控制器重复拼 code 和 message。
        CommonResponse<T> response = new CommonResponse<>();
        // 约定 0 表示接口调用成功。
        response.setCode(0);
        // 成功消息固定成 success，便于前端和测试统一断言。
        response.setMessage("success");
        // 业务数据原样透传给前端页面或上游调用方。
        response.setData(data);
        return response;
    }

    /**
     * 组装失败响应。
     *
     * @param code 失败码
     * @param message 失败消息
     * @param <T> 数据类型
     * @return 标准失败壳
     */
    public static <T> CommonResponse<T> failure(int code, String message) {
        // 失败场景也统一封装，确保所有错误响应格式保持一致。
        CommonResponse<T> response = new CommonResponse<>();
        // 调用方传入的失败码决定前端后续如何提示或分流。
        response.setCode(code);
        // 失败消息直接作为用户可读或日志可读的错误描述。
        response.setMessage(message);
        return response;
    }

    /**
     * 获取业务码。
     *
     * @return 业务码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置业务码。
     *
     * @param code 业务码
     */
    public void setCode(int code) {
        // 回填业务码，供统一响应壳在序列化时输出给前端。
        this.code = code;
    }

    /**
     * 获取提示语。
     *
     * @return 提示语
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置提示语。
     *
     * @param message 提示语
     */
    public void setMessage(String message) {
        // 回填接口提示语，供页面直接展示结果或失败原因。
        this.message = message;
    }

    /**
     * 获取业务数据。
     *
     * @return 业务数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置业务数据。
     *
     * @param data 业务数据
     */
    public void setData(T data) {
        // 回填本次接口真正的业务数据载荷。
        this.data = data;
    }
}
