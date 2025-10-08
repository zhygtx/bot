package com.news.web.pojo;

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
    private Integer code;     // 业务状态码  0-成功  1-失败
    private String message;   // 提示信息
    private T data;           // 响应数据

    /**
     * 快速返回操作成功响应结果(带响应数据)
     * @param data 响应数据
     * @return 封装后的成功响应结果
     * @param <E> 数据类型
     */
    public static <E> Result<E> success(E data) {
        return new Result<>(0, "操作成功", data);
    }

    /**
     * 快速返回操作成功响应结果(自定义消息和数据)
     * @param message 提示信息
     * @param data 响应数据
     * @return 封装后的成功响应结果
     * @param <E> 数据类型
     */
    public static <E> Result<E> success(String message, E data) {
        return new Result<>(0, message, data);
    }

    /**
     * 快速返回操作成功响应结果(仅状态码和消息)
     * @return 封装后的成功响应结果
     * @param <E> 数据类型
     */
    public static <E> Result<E> success() {
        return new Result<>(0, "操作成功", null);
    }

    /**
     * 快速返回操作失败响应结果
     * @param message 错误信息
     * @return 封装后的失败响应结果
     * @param <E> 数据类型
     */
    public static <E> Result<E> error(String message) {
        return new Result<>(1, message, null);
    }
}
