package com.example.demo.exception;

import com.example.demo.pojo.entity.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器类，用于统一处理系统中抛出的异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理系统中的通用异常
     *
     * @param e 捕获到的异常对象
     * @return 封装后的错误响应结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        log.error("操作异常",e);
        return Result.error(500,StringUtils.hasLength(e.getMessage())? e.getMessage() : "操作失败");
    }

    /**
     * 处理参数验证异常
     * @param ex 方法参数验证异常对象
     * @return 包含验证错误信息的响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(
            MethodArgumentNotValidException ex) {
        // 获取验证失败的第一个字段错误信息
        FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);
        return Result.error(400,fieldError.getDefaultMessage());
    }

    /**
     * 处理404异常
     * @param ex NoHandlerFoundException异常
     * @return 错误响应结果
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFoundException(NoHandlerFoundException ex) {
        log.warn("请求的资源不存在: {}", ex.getRequestURL());
        return Result.error(404,"请求的资源不存在");
    }

    /**
     * 处理认证异常
     * @param request HTTP请求
     * @return 错误响应结果
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Map<String, String>> handleAuthException(HttpServletRequest request) {
        log.warn("认证失败: {}", request.getRequestURI());
        Map<String, String> data = new HashMap<>();
        data.put("redirect", "/auth/login");
        return Result.success("认证失败，请重新登录", data);
    }

    /**
     * 处理文件上传大小超出限制的异常
     * @return 错误响应结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件上传大小超过限制: {}", e.getMessage());
        return Result.error(400,"文件大小超过限制，请上传小于10MB的文件");
    }

}