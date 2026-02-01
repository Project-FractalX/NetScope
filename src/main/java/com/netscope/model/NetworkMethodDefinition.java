package com.netscope.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.reflect.Method;

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

    public NetworkMethodDefinition(Object bean, Method method, String path, boolean restricted) {
        this.bean = bean;
        this.method = method;
        this.beanName = bean.getClass().getSimpleName();
        this.methodName = method.getName();
        this.path = path;
        this.httpMethod = "GET"; // or extract from annotation
        this.restricted = restricted;
    }

    // getters (no bean/method)
    public String getBeanName() { return beanName; }
    public String getMethodName() { return methodName; }
    public String getPath() { return path; }
    public String getHttpMethod() { return httpMethod; }
    public boolean isRestricted() { return restricted; }

    // Optional: expose the Method and Bean for internal use, but not for serialization
    @JsonIgnore
    public Object getBean() { return bean; }

    @JsonIgnore
    public Method getMethod() { return method; }
}
