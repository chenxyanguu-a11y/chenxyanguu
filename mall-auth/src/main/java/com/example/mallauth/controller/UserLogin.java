package com.example.mallauth.controller;

import com.example.mallauth.dto.Role;
import com.example.mallauth.service.UserService;
import com.example.mallcommon.core.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserLogin {
    private UserService userService;
    @PostMapping("/login")
    public Result login(@RequestBody Role role){
            String token = userService.login(role);
            return Result.success(token);

    }
}
