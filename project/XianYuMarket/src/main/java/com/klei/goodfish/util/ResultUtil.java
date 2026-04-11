package com.klei.goodfish.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一响应工具类
 */
public class ResultUtil<T> {

    private int code;
    private boolean success;
    private String message;
    private T data;

    // ★★★ 关键修复：使用带 LocalDateTime 序列化的 Gson ★★★
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new com.google.gson.JsonSerializer<LocalDateTime>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                @Override
                public com.google.gson.JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                    return new com.google.gson.JsonPrimitive(src.format(formatter));
                }
            })
            .create();

    private ResultUtil() {}

    public static <T> ResultUtil<T> success(T data) {
        ResultUtil<T> result = new ResultUtil<>();
        result.code = 200;
        result.success = true;
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    public static <T> ResultUtil<T> success(String message, T data) {
        ResultUtil<T> result = new ResultUtil<>();
        result.code = 200;
        result.success = true;
        result.message = message;
        result.data = data;
        return result;
    }

    public static <T> ResultUtil<T> fail(String message) {
        ResultUtil<T> result = new ResultUtil<>();
        result.code = 400;
        result.success = false;
        result.message = message;
        result.data = null;
        return result;
    }

    public static <T> ResultUtil<T> fail(int code, String message) {
        ResultUtil<T> result = new ResultUtil<>();
        result.code = code;
        result.success = false;
        result.message = message;
        result.data = null;
        return result;
    }

    public static <T> ResultUtil<T> unauthorized(String message) {
        return fail(401, message);
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public int getCode() { return code; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}