package com.example.malluser.service;

import com.example.malluser.dto.UserInfoResponse;

import java.util.List;

public interface AdminUserService {
    List<UserInfoResponse> listNonAdminUsers(Long currentUserId, String role);
}
