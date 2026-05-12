package com.example.mallproduct.exception;

import lombok.Data;
@Data
public class EsExcetpionHandler extends RuntimeException {
    private Integer code;
    public EsExcetpionHandler(Integer code,String message){
        super(message);
        this.code=code;
    }
}
