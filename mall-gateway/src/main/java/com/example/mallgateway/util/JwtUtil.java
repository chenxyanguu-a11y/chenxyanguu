package com.example.mallgateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class JwtUtil {

    private final Key signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public JwtUserInfo parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userId = getRequiredString(claims, "userId");
        String username = getRequiredString(claims, "username");
        List<String> roles = getStringList(claims.get("roles"));
        List<String> permissions = getStringList(claims.get("permissions"));

        return new JwtUserInfo(userId, username, roles, permissions);
    }

    private String getRequiredString(Claims claims, String key) {
        Object value = claims.get(key);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            throw new IllegalArgumentException("Token缺少" + key);
        }
        return String.valueOf(value);
    }

    private List<String> getStringList(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof Collection<?> collection) {
            List<String> result = new ArrayList<>();
            for (Object item : collection) {
                if (item != null && StringUtils.hasText(String.valueOf(item))) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        String text = String.valueOf(value);
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        String[] items = text.split(",");
        List<String> result = new ArrayList<>();
        for (String item : items) {
            if (StringUtils.hasText(item)) {
                result.add(item.trim());
            }
        }
        return result;
    }

    public static class JwtUserInfo {

        private final String userId;

        private final String username;

        private final List<String> roles;

        private final List<String> permissions;

        public JwtUserInfo(String userId, String username, List<String> roles, List<String> permissions) {
            this.userId = userId;
            this.username = username;
            this.roles = roles;
            this.permissions = permissions;
        }

        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public List<String> getRoles() {
            return roles;
        }

        public List<String> getPermissions() {
            return permissions;
        }
    }
}
