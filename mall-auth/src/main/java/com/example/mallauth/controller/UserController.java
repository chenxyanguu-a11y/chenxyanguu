package com.example.mallauth.controller;

import com.example.mallauth.dto.Role;
import com.example.mallauth.service.UserService;
import com.example.mallcommon.core.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody Role role) {
        String token = userService.login(role);
        return Result.success(token);
    }

    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String authorization) {
        userService.logout(authorization);
        return Result.success("退出成功");
    }
}
