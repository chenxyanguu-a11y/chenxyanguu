package com.example.malluser.exception;

import com.example.mallcommon.core.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserException.class)
    public Result handleUserException(UserException e) {
        return Result.error(e.getMessage());
    }
}
