package com.example.mallcommon.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    public static<T>  Result<T> success(T data){
        return new Result<> (200,"操作成功",data);
    }

    public static<T> Result<T> error(String message){
        return new Result<>(0,message,null);
    }

    public static<T> Result<T> success(){
        return new Result<>(200,"操作成功",null);
    }
}
