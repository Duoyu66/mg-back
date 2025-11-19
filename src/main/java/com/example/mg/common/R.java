package com.example.mg.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一返回结果类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    // ========== 成功返回方法 ==========

    /**
     * 成功返回（无数据）
     */
    public static <T> R<T> success() {
        return R.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功返回（有数据）
     */
    public static <T> R<T> success(T data) {
        return R.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功返回（自定义消息）
     */
    public static <T> R<T> success(String message, T data) {
        return R.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ========== 失败返回方法 ==========

    /**
     * 失败返回
     */
    public static <T> R<T> failed() {
        return failed(ResultCode.FAILED);
    }

    /**
     * 失败返回（自定义消息）
     */
    public static <T> R<T> failed(String message) {
        return R.<T>builder()
                .code(ResultCode.FAILED.getCode())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败返回（枚举状态码）
     */
    public static <T> R<T> failed(ResultCode errorCode) {
        return R.<T>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败返回（自定义状态码和消息）
     */
    public static <T> R<T> failed(Integer code, String message) {
        return R.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ========== 特定场景快捷方法 ==========

    /**
     * 参数验证失败
     */
    public static <T> R<T> validateFailed() {
        return failed(ResultCode.VALIDATE_FAILED);
    }

    public static <T> R<T> validateFailed(String message) {
        return R.<T>builder()
                .code(ResultCode.VALIDATE_FAILED.getCode())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 未登录/未授权
     */
    public static <T> R<T> unauthorized() {
        return failed(ResultCode.UNAUTHORIZED);
    }

    public static <T> R<T> forbidden() {
        return failed(ResultCode.FORBIDDEN);
    }

    /**
     * 资源不存在
     */
    public static <T> R<T> notFound(String s) {
        return failed(ResultCode.NOT_FOUND);
    }

    // ========== 分页数据专用方法 ==========

    /**
     * 分页数据成功返回
     */
    public static <T> R<PageData<T>> page(List<T> list, Long total) {
        PageData<T> pageData = PageData.create(list, total);
        return success(pageData);
    }

    public static <T> R<PageData<T>> page(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        PageData<T> pageData = PageData.create(list, total, pageNum, pageSize);
        return success(pageData);
    }

    /**
     * MyBatis-Plus分页支持
     */
    public static <T> R<PageData<T>> page(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        PageData<T> pageData = PageData.create(page);
        return success(pageData);
    }
}