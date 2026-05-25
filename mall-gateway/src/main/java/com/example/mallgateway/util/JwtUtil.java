package com.example.mallgateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    public JwtUserInfo parseToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();

        String userId = getRequiredString(claims, "userId");
        String username = getRequiredString(claims, "username");
        String roleId = getRequiredString(claims, "roleId");
        String role = buildRole(roleId);
        System.out.println(userId+username+role);
        return new JwtUserInfo(userId, username, role);
    }

    private String getRequiredString(Claims claims, String key) {
        Object value = claims.get(key);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            throw new IllegalArgumentException("Token missing " + key);
        }
        return String.valueOf(value);
    }

    private String buildRole(String roleId) {
        if ("1".equals(roleId)) {
            return "ADMIN";
        }
        if ("2".equals(roleId)) {
            return "MERCHANT";
        }
        return "USER";
    }

    public static class JwtUserInfo {

        private final String userId;

        private final String username;

        private final String role;

        public JwtUserInfo(String userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
