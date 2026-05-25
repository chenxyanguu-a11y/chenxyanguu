package com.example.malluser.dto;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Integer userType;
    private Integer status;
    private Integer deleted;
    private Integer roleId;
}
