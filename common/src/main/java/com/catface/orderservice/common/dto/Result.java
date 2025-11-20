package com.catface.orderservice.common.dto;

import java.time.LocalDateTime;

/**
 * 统一响应类
 * 用于封装所有 API 的响应结果
 *
 * @param <T> 响应数据类型
 */
public class Result<T> {

    /**
     * 响应码（成功或错误码）
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 成功响应码
     */
    public static final String SUCCESS_CODE = "0000";

    /**
     * 成功响应消息
     */
    public static final String SUCCESS_MESSAGE = "操作成功";

    /**
     * 无参构造函数
     */
    public Result() {
    }

    /**
     * 全参构造函数
     *
     * @param code      响应码
     * @param message   响应消息
     * @param data      响应数据
     * @param timestamp 响应时间戳
     */
    public Result(String code, String message, T data, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data, LocalDateTime.now());
    }

    /**
     * 创建成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, null, LocalDateTime.now());
    }

    /**
     * 创建错误响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(String code, String message) {
        return new Result<>(code, message, null, LocalDateTime.now());
    }

    /**
     * 创建错误响应（使用默认错误码）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(String message) {
        return new Result<>("9999", message, null, LocalDateTime.now());
    }

    // Getter 和 Setter 方法

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
