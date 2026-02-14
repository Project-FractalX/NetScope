package com.netscope.core;

import com.netscope.model.NetworkMethodDefinition;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Registers dynamic REST endpoints for network-exposed methods.
 */
public class NetScopeRegistrar {

    private final RequestMappingHandlerMapping handlerMapping;

    public NetScopeRegistrar(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    /**
     * Register all exposed methods as REST endpoints.
     */
    public void register(List<NetworkMethodDefinition> methods) {
        for (NetworkMethodDefinition def : methods) {
            if (!def.isRestEnabled()) {
                continue;
            }

            try {
                // Get the controller method that handles requests
                Method controllerMethod = NetScopeDynamicController.class.getMethod(
                    "handleRequest",
                    jakarta.servlet.http.HttpServletRequest.class,
                    String.class,
                    String.class
                );

                // Build request mapping info
                RequestMethod[] httpMethods = getRequestMethods(def.getHttpMethod());
                RequestMappingInfo mappingInfo = RequestMappingInfo
                    .paths(def.getPath())
                    .methods(httpMethods)
                    .build();

                // Note: In Spring Boot 3+, programmatic registration is complex
                // The dynamic controller with /** mapping handles all requests
                // This registrar is kept for potential future enhancements

            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Failed to register method: " + def.getPath(), e);
            }
        }
    }

    private RequestMethod[] getRequestMethods(String httpMethod) {
        try {
            return new RequestMethod[]{RequestMethod.valueOf(httpMethod)};
        } catch (IllegalArgumentException e) {
            return new RequestMethod[]{RequestMethod.GET};
        }
    }
}
