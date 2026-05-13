package com.example.mallgateway.filter;

import com.example.mallgateway.config.AuthProperties;
import com.example.mallgateway.util.GatewayResponseUtil;
import com.example.mallgateway.util.JwtUtil;
import com.example.mallgateway.util.JwtUtil.JwtUserInfo;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private AuthProperties authProperties;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isWhitelist(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization)) {
            return GatewayResponseUtil.unauthorized(exchange);
        }
        if (!authorization.startsWith(BEARER_PREFIX) || authorization.length() <= BEARER_PREFIX.length()) {
            return GatewayResponseUtil.unauthorized(exchange);
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            return GatewayResponseUtil.unauthorized(exchange);
        }

        JwtUserInfo userInfo;
        try {
            userInfo = jwtUtil.parseToken(token);
        } catch (Exception e) {
            return GatewayResponseUtil.unauthorized(exchange);
        }

        String redisKey = "login:user:" + userInfo.getUserId();
        return reactiveStringRedisTemplate.hasKey(redisKey)
                .flatMap(exists -> {
                    if (!Boolean.TRUE.equals(exists)) {
                        return GatewayResponseUtil.unauthorized(exchange);
                    }
                    String requiredPermission = getRequiredPermission(path);
                    if (StringUtils.hasText(requiredPermission)
                            && !userInfo.getPermissions().contains(requiredPermission)) {
                        return GatewayResponseUtil.forbidden(exchange);
                    }
                    ServerWebExchange mutatedExchange = addUserHeaders(exchange, userInfo);
                    return chain.filter(mutatedExchange);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isWhitelist(String path) {
        for (String pattern : authProperties.getWhitelist()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String getRequiredPermission(String path) {
        for (Map.Entry<String, String> entry : authProperties.getPermissionRules().entrySet()) {
            if (pathMatcher.match(entry.getKey(), path)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private ServerWebExchange addUserHeaders(ServerWebExchange exchange, JwtUserInfo userInfo) {
        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers -> {
                    headers.set("X-User-Id", userInfo.getUserId());
                    headers.set("X-Username", encodeHeaderValue(userInfo.getUsername()));
                    headers.set("X-Roles", join(userInfo.getRoles()));
                    headers.set("X-Permissions", join(userInfo.getPermissions()));
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    private String encodeHeaderValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String join(List<String> values) {
        return String.join(",", values);
    }
}
