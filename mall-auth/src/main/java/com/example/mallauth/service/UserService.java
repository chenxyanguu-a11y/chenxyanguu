package com.example.mallauth.service;

import com.example.mallauth.dto.Role;

public interface UserService {
    String login(Role role);
    void logout(String authorization);
}
