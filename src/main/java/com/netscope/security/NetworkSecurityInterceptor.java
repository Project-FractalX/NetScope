package com.netscope.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class NetworkSecurityInterceptor implements HandlerInterceptor {

    private final ServiceAuthValidator validator = new ServiceAuthValidator();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (request.getRequestURI().contains("/netscope/")) {
            if (!validator.validate(request)) {
                response.setStatus(401);
                return false;
            }
        }
        return true;
    }
}
