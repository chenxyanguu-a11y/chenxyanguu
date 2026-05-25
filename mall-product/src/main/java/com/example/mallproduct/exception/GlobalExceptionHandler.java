package com.example.mallproduct.exception;


import com.example.mallcommon.core.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EsExcetpionHandler.class)
    public Result handleUserException(EsExcetpionHandler e) {
        return Result.error(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
}
