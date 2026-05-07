package com.example.mallauth.service;

import com.example.mallauth.Exception.LoginException;
import com.example.mallauth.dto.Role;
import com.example.mallauth.entity.SysUser;
import com.example.mallauth.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Override
    public String login(Role role) {
        SysUser sysUser = userMapper.SelectByName(role.getUsername());
        if (sysUser == null) {
            throw new LoginException(400, "登录失败，用户名错误");
        }
        if (!passwordEncoder.matches(role.getPassward(), sysUser.getPassword())) {
            throw new LoginException(400, "登录失败，密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", sysUser.getId());
        claims.put("username", sysUser.getUsername());
        claims.put("userType", sysUser.getUserType());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    @Override
    public void logout(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            throw new LoginException(400, "Authorization不能为空");
        }
        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }

        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new LoginException(400, "Token无效");
        }

        Object userId = claims.get("userId");
        if (userId == null) {
            throw new LoginException(400, "Token缺少userId");
        }

        String redisKey = "login:user:" + userId;
        stringRedisTemplate.delete(redisKey);
    }
}
