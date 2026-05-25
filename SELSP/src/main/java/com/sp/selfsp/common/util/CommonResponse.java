package com.sp.selfsp.common.util;

/**
 * 统一响应壳，保证所有接口都返回 code、message、data 三段结构。
 *
 * @param <T> 业务数据类型
 */
// 定义 通用响应，承接当前文件对应的业务职责。
public class CommonResponse<T> {

    // 业务码用于给前端判断成功或失败。
    // 声明 编码 字段，用来保存当前业务状态或依赖。
    private int code;

    // 提示语用于描述处理结果。
    // 声明 文案 字段，用来保存当前业务状态或依赖。
    private String message;

    // data 统一承载本次接口返回的业务数据。
    // 声明 data 字段，用来保存当前业务状态或依赖。
    private T data;

    /**
     * 组装成功响应。
     *
     * @param data 业务数据
     * @param <T> 数据类型
     * @return 标准成功壳
     */
    // 定义 success 处理入口，承接当前业务动作。
    public static <T> CommonResponse<T> success(T data) {
        // 新建响应对象，避免调用方自己散落拼装成功结构。
        // 执行当前业务步骤，推进本行对应的 general 处理。
        CommonResponse<T> response = new CommonResponse<>();
        // 0 统一表示成功。
        // 把当前后端返回结果回填到统一响应壳字段中。
        response.setCode(0);
        // success 统一作为成功消息。
        // 把当前后端返回结果回填到统一响应壳字段中。
        response.setMessage("success");
        // 透传业务数据给前端。
        // 把当前后端返回结果回填到统一响应壳字段中。
        response.setData(data);
        // 返回统一结构。
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
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
    // 定义 failure 处理入口，承接当前业务动作。
    public static <T> CommonResponse<T> failure(int code, String message) {
        // 失败场景同样由统一壳负责生成，避免控制器各自定义错误格式。
        // 执行当前业务步骤，推进本行对应的 general 处理。
        CommonResponse<T> response = new CommonResponse<>();
        // 回填业务失败码。
        // 把当前后端返回结果回填到统一响应壳字段中。
        response.setCode(code);
        // 回填失败消息。
        // 把当前后端返回结果回填到统一响应壳字段中。
        response.setMessage(message);
        // 返回失败结构。
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return response;
    }

    /**
     * 获取业务码。
     *
     * @return 业务码
     */
    // 对外返回 编码，供上下游继续读取当前业务字段。
    public int getCode() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return code;
    }

    /**
     * 设置业务码。
     *
     * @param code 业务码
     */
    // 回填 编码，让请求绑定或结果组装保存当前字段值。
    public void setCode(int code) {
        // 保存业务码，供前端统一处理。
        // 把外部传入结果写入 编码 字段，供后续流程继续使用。
        this.code = code;
    }

    /**
     * 获取提示语。
     *
     * @return 提示语
     */
    // 对外返回 文案，供上下游继续读取当前业务字段。
    public String getMessage() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return message;
    }

    /**
     * 设置提示语。
     *
     * @param message 提示语
     */
    // 回填 文案，让请求绑定或结果组装保存当前字段值。
    public void setMessage(String message) {
        // 保存接口提示语。
        // 把外部传入结果写入 文案 字段，供后续流程继续使用。
        this.message = message;
    }

    /**
     * 获取业务数据。
     *
     * @return 业务数据
     */
    // 对外返回 Data，供上下游继续读取当前业务字段。
    public T getData() {
        // 返回当前步骤产出的业务结果，继续交给上一层消费。
        return data;
    }

    /**
     * 设置业务数据。
     *
     * @param data 业务数据
     */
    // 回填 Data，让请求绑定或结果组装保存当前字段值。
    public void setData(T data) {
        // 保存接口业务数据。
        // 把外部传入结果写入 data 字段，供后续流程继续使用。
        this.data = data;
    }
}
