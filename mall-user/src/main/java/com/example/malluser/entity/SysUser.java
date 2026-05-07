package com.example.malluser.entity;

import lombok.Data;

@Data
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private Integer userType;
    private Integer status;
    private Integer deleted;
}
