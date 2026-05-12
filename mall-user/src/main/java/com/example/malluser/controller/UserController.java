package com.example.malluser.controller;

import com.example.mallcommon.core.Result;
import com.example.malluser.dto.RegisterRequest;
import com.example.malluser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功");
    }
}
