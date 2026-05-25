package com.example.malluser.controller;

import com.example.mallcommon.core.Result;
import com.example.malluser.dto.UserInfoResponse;
import com.example.malluser.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/admin")
public class AdminUserController {
    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/list")
    public Result<List<UserInfoResponse>> listNonAdminUsers(@RequestHeader("X-User-Id") Long currentUserId,
                                                            @RequestHeader("X-Role") String role) {
        List<UserInfoResponse> users = adminUserService.listNonAdminUsers(currentUserId, role);
        return Result.success(users);
    }
}
