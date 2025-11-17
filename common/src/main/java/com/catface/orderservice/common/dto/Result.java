package com.catface.orderservice.common.dto;

/**
 * 统一响应结果类
 *
 * @param <T> 响应数据类型
 */
public class Result<T> {
    
    /**
     * 响应码（0: 成功，其他: 失败）
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
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 构造函数
     */
    public Result() {
    }
    
    /**
     * 构造函数
     *
     * @param code      响应码
     * @param message   响应消息
     * @param data      响应数据
     * @param timestamp 时间戳
     */
    public Result(String code, String message, T data, Long timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }
    
    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>("0", "success", null, System.currentTimeMillis());
    }
    
    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>("0", "success", data, System.currentTimeMillis());
    }
    
    /**
     * 失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }
    
    // Getters and Setters
    
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
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
