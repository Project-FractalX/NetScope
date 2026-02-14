package com.netscope.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Represents a method exposed via NetScope (REST and/or gRPC).
 */
public class NetworkMethodDefinition {

    @JsonIgnore
    private final Object bean;

    @JsonIgnore
    private final Method method;

    private final String beanName;
    private final String methodName;
    private final String path;
    private final String httpMethod;
    private final boolean restricted;
    private final String apiKey;
    private final boolean restEnabled;
    private final boolean grpcEnabled;
    private final ParameterInfo[] parameters;
    private final String returnType;

    public NetworkMethodDefinition(Object bean, Method method, String path, boolean restricted, 
                                    boolean restEnabled, boolean grpcEnabled) {
        this.bean = bean;
        this.method = method;
        this.beanName = bean.getClass().getSimpleName();
        this.methodName = method.getName();
        this.path = path;
        this.restricted = restricted;
        this.restEnabled = restEnabled;
        this.grpcEnabled = grpcEnabled;
        this.returnType = method.getReturnType().getName();

        // Extract HTTP method and API key from annotations
        if (method.isAnnotationPresent(com.netscope.annotation.NetworkRestricted.class)) {
            var annotation = method.getAnnotation(com.netscope.annotation.NetworkRestricted.class);
            this.httpMethod = annotation.method().name();
            this.apiKey = annotation.key();
        } else if (method.isAnnotationPresent(com.netscope.annotation.NetworkPublic.class)) {
            var annotation = method.getAnnotation(com.netscope.annotation.NetworkPublic.class);
            this.httpMethod = annotation.method().name();
            this.apiKey = "";
        } else {
            this.httpMethod = "GET";
            this.apiKey = "";
        }

        // Extract parameter information
        Parameter[] params = method.getParameters();
        this.parameters = new ParameterInfo[params.length];
        for (int i = 0; i < params.length; i++) {
            this.parameters[i] = new ParameterInfo(
                params[i].getName(),
                params[i].getType().getName(),
                i
            );
        }
    }

    // Getters
    public String getBeanName() { return beanName; }
    public String getMethodName() { return methodName; }
    public String getPath() { return path; }
    public String getHttpMethod() { return httpMethod; }
    public boolean isRestricted() { return restricted; }
    public String getApiKey() { return apiKey; }
    public boolean isRestEnabled() { return restEnabled; }
    public boolean isGrpcEnabled() { return grpcEnabled; }
    public ParameterInfo[] getParameters() { return parameters; }
    public String getReturnType() { return returnType; }

    @JsonIgnore
    public Object getBean() { return bean; }

    @JsonIgnore
    public Method getMethod() { return method; }

    /**
     * Parameter information for documentation
     */
    public static class ParameterInfo {
        private final String name;
        private final String type;
        private final int index;

        public ParameterInfo(String name, String type, int index) {
            this.name = name;
            this.type = type;
            this.index = index;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public int getIndex() { return index; }
    }
}
