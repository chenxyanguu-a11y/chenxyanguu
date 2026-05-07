package com.example.malluser.exception;

import lombok.Data;

@Data
public class UserException extends RuntimeException {
    private Integer code;
    public UserException(Integer code,String message) {
        super(message);
        this.code=code;
    }
}
