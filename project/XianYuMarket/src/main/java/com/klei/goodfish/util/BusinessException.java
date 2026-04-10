package com.klei.goodfish.util;

/**
 * 业务异常类（Service层抛出，Controller层捕获处理）
 * @author klei
 */
public class BusinessException extends RuntimeException {

    // 错误码：400参数错误、401未登录、403无权限、500系统错误
    private int code;

    // 默认构造
    public BusinessException() {
        super("业务异常");
        this.code = 400;
    }

    // 带错误信息
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    // 带错误码和信息
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}