package com.example.mallgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "mall.gateway.auth")
public class AuthProperties {

    private List<String> whitelist = new ArrayList<>();

    private Map<String, String> permissionRules = new LinkedHashMap<>();

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    public Map<String, String> getPermissionRules() {
        return permissionRules;
    }

    public void setPermissionRules(Map<String, String> permissionRules) {
        this.permissionRules = permissionRules;
    }
}
