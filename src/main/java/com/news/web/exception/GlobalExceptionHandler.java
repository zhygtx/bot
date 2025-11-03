package com.news.web.exception;

import com.news.web.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
        return Result.error(StringUtils.hasLength(e.getMessage())? e.getMessage() : "操作失败");
    }

    /**
     * 处理参数验证异常
     * 
     * @param ex 方法参数验证异常对象
     * @return 包含验证错误信息的响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(
            MethodArgumentNotValidException ex) {
        // 获取验证失败的第一个字段错误信息
        FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);
        return Result.error(fieldError.getDefaultMessage());
    }
}