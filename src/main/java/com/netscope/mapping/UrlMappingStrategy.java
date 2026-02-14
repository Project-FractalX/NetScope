package com.netscope.mapping;

import java.lang.reflect.Method;

/**
 * Strategy interface for generating URL paths for exposed methods.
 */
public interface UrlMappingStrategy {
    /**
     * Build a path for the given bean class and method.
     * 
     * @param beanClass The bean class
     * @param method The method to expose
     * @return The URL path
     */
    String buildPath(Class<?> beanClass, Method method);
}
