package com.example.demo.exception;

import com.example.demo.common.Result;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        return Result.error(500, "服务器异常：" + e.getMessage());
    }
}