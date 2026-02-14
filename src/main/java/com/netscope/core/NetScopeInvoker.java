package com.netscope.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.netscope.model.NetworkMethodDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Handles dynamic invocation of network-exposed methods.
 */
public class NetScopeInvoker {

    private final ObjectMapper objectMapper;

    public NetScopeInvoker() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Invoke a method with JSON arguments.
     */
    public Object invoke(NetworkMethodDefinition definition, String argumentsJson) throws Exception {
        Method method = definition.getMethod();
        Object bean = definition.getBean();
        
        // Parse arguments
        Object[] args = parseArguments(method, argumentsJson);
        
        // Invoke the method
        return method.invoke(bean, args);
    }

    /**
     * Parse JSON arguments into method parameters.
     */
    private Object[] parseArguments(Method method, String argumentsJson) throws Exception {
        Parameter[] parameters = method.getParameters();
        
        if (parameters.length == 0) {
            return new Object[0];
        }

        if (argumentsJson == null || argumentsJson.trim().isEmpty() || argumentsJson.equals("null")) {
            return new Object[parameters.length];
        }

        // Parse JSON array
        Object[] values = objectMapper.readValue(argumentsJson, Object[].class);
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length && i < values.length; i++) {
            if (values[i] != null) {
                // Convert to the correct parameter type
                JavaType javaType = objectMapper.getTypeFactory().constructType(parameters[i].getType());
                args[i] = objectMapper.convertValue(values[i], javaType);
            }
        }

        return args;
    }

    /**
     * Serialize result to JSON.
     */
    public String serializeResult(Object result) throws Exception {
        if (result == null) {
            return "null";
        }
        return objectMapper.writeValueAsString(result);
    }
}
