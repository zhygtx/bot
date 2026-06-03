package com.example.demo.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果封装类
 * @param <T> 响应数据的类型
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code;     // 业务状态码
    private String message;   // 提示信息
    private T data;           // 响应数据

    // 状态码常量定义
    public static final Integer CODE_SUCCESS = 200;           // 成功
    public static final Integer CODE_BAD_REQUEST = 400;     // 请求参数错误
    public static final Integer CODE_UNAUTHORIZED = 401;    // 未授权
    public static final Integer CODE_FORBIDDEN = 403;       // 禁止访问
    public static final Integer CODE_NOT_FOUND = 404;       // 资源未找到
    public static final Integer CODE_SERVER_ERROR = 500;    // 服务器内部错误
    public static final Integer CODE_SERVICE_UNAVAILABLE = 503; // 服务不可用

    /**
     * 快速返回操作成功响应结果(带响应数据)
     * @param data 响应数据
     * @return 封装后的成功响应结果
     * @param <E> 数据类型
     */
    public static <E> Result<E> success(E data) {
        return new Result<>(CODE_SUCCESS, "操作成功", data);
    }

    /**
     * 快速返回操作成功响应结果(自定义消息和数据)
     * @param message 提示信息
     * @param data 响应数据
     * @return 封装后的成功响应结果
     * @param <E> 数据类型
     */
    public static <E> Result<E> success(String message, E data) {
        return new Result<>(CODE_SUCCESS, message, data);
    }

    /**
     * 快速返回操作成功响应结果(仅状态码和消息)
     * @return 封装后的成功响应结果
     * @param <E> 数据类型
     */
    public static <E> Result<E> success() {
        return new Result<>(CODE_SUCCESS, "操作成功", null);
    }

    /**
     * 快速返回操作失败响应结果
     * @param message 错误信息
     * @return 封装后的失败响应结果
     * @param <E> 数据类型
     */
    public static <E> Result<E> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 快速返回操作失败响应结果
     * @param message 错误信息
     * @param data 响应数据
     * @return 封装后的失败响应结果
     * @param <E> 数据类型
     */
    public static <E> Result<E> error(Integer code, String message, E data) {
        return new Result<>(code, message, data);
    }

    /**
     * 快速返回操作失败响应结果
     * @return 封装后的失败响应结果
     * @param <E> 数据类型
     */
    public static <E> Result<E> error(Integer code) {
        return new Result<>(code, null, null);
    }
}
