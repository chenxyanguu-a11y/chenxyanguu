package com.example.malluser.service.impl;

import com.example.malluser.dto.RegisterRequest;
import com.example.malluser.entity.SysUser;
import com.example.malluser.exception.UserException;
import com.example.malluser.mapper.UserMapper;
import com.example.malluser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getPassword())
                || !StringUtils.hasText(request.getNickname())) {
            throw new UserException("username、password、nickname不能为空");
        }

        SysUser existedUser = userMapper.selectByUsername(request.getUsername());
        if (existedUser != null) {
            throw new UserException("用户名已存在");
        }

        SysUser sysUser = new SysUser();
        sysUser.setUsername(request.getUsername());
        sysUser.setPassword(passwordEncoder.encode(request.getPassword()));
        sysUser.setNickname(request.getNickname());
        sysUser.setPhone(request.getPhone());
        sysUser.setEmail(request.getEmail());
        sysUser.setUserType(3);
        sysUser.setStatus(1);
        sysUser.setDeleted(0);
        userMapper.insertUser(sysUser);
    }
}
