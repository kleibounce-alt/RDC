package com.klei.goodfish.util;

import com.google.gson.Gson;

/**
 * 统一响应工具类
 */
public class ResultUtil<T> {

    private int code;
    private boolean success;
    private String message;
    private T data;

    private static final Gson gson = new Gson();

    private ResultUtil() {}

    /**
     * 成功响应（带数据）
     */
    public static <T> ResultUtil<T> success(T data) {
        ResultUtil<T> result = new ResultUtil<>();
        result.code = 200;
        result.success = true;
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    /**
     * 成功响应（带数据和自定义消息）
     */
    public static <T> ResultUtil<T> success(String message, T data) {
        ResultUtil<T> result = new ResultUtil<>();
        result.code = 200;
        result.success = true;
        result.message = message;
        result.data = data;
        return result;
    }

    /**
     * 失败响应（通用）
     */
    public static <T> ResultUtil<T> fail(String message) {
        ResultUtil<T> result = new ResultUtil<>();
        result.code = 400;
        result.success = false;
        result.message = message;
        result.data = null;
        return result;
    }

    /**
     * 失败响应（带特定状态码）
     */
    public static <T> ResultUtil<T> fail(int code, String message) {
        ResultUtil<T> result = new ResultUtil<>();
        result.code = code;
        result.success = false;
        result.message = message;
        result.data = null;
        return result;
    }

    /**
     * 未登录
     */
    public static <T> ResultUtil<T> unauthorized(String message) {
        return fail(401, message);
    }

    /**
     * 转成 JSON 字符串
     */
    public String toJson() {
        return gson.toJson(this);
    }

    // Getter 方法（前端用）
    public int getCode() { return code; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}