package com.example.mallauth.service;

import com.example.mallauth.Exception.LoginException;
import com.example.mallauth.dto.Role;
import com.example.mallauth.entity.SysUser;
import com.example.mallauth.mapper.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Override
    public String login(Role role) {
        SysUser sysUser=userMapper.SelectByName(role.getUsername());
        if (sysUser == null) {
            throw new LoginException(400,"登录失败，用户名错误");
        }
        if (!passwordEncoder.matches(role.getPassward(), sysUser.getPassword())) {
            throw new LoginException(400,"登录失败，密码错误");
        } else {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", sysUser.getId());           // 用户ID
            claims.put("username", sysUser.getUsername());   // 用户名
            claims.put("userType", sysUser.getUserType());   // 1管理员，2商家，3普通用户
            String token= Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(new Date()) // 设置签发时间
                    .setExpiration(new Date(System.currentTimeMillis() + 900000))
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();
            return token;
        }
    }

}
