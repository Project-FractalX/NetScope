package com.netscope.security;

import jakarta.servlet.http.HttpServletRequest;

public class ServiceAuthValidator {

    public boolean validate(HttpServletRequest request) {
        String token = request.getHeader("X-Service-Token");
        return token != null && token.equals("trusted-token");
    }
}
