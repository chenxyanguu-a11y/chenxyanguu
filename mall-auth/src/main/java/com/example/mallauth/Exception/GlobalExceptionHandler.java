package com.example.mallauth.Exception;

import com.example.mallcommon.core.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * @param e
     * @return
     */
    @ExceptionHandler(LoginException.class)
    public Result handleLoginException(LoginException e){
        return Result.error(e.getCode(),e.getMessage());
    }



}
