package com.example.malluser.service.impl;

import com.example.malluser.dto.UserInfoResponse;
import com.example.malluser.exception.UserException;
import com.example.malluser.mapper.UserMapper;
import com.example.malluser.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    private static final String ROLE_ADMIN = "ADMIN";

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UserInfoResponse> listNonAdminUsers(Long currentUserId, String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new UserException(403, "无权限访问");
        }
        return userMapper.selectNonAdminUsers(currentUserId);
    }
}
