package com.example.mallauth.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUser {

    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    /**
     * 用户类型：1管理员，2商家，3用户
     */
    private Integer userType;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0未删，1已删
     */
    private Integer deleted;
}