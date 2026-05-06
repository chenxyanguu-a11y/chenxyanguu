package com.example.mallauth.Exception;

import lombok.Data;

@Data
public class LoginException extends RuntimeException {
    private Integer code;
    public LoginException(Integer code,String message){
        super(message);
        this.code=code;
    }
    public LoginException(String message){
        super(message);
    }

}
