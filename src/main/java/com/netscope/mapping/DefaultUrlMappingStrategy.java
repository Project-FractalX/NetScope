package com.netscope.mapping;

import java.lang.reflect.Method;

/**
 * Default URL mapping strategy: /netscope/{BeanName}/{methodName}
 */
public class DefaultUrlMappingStrategy implements UrlMappingStrategy {

    @Override
    public String buildPath(Class<?> beanClass, Method method) {
        String beanName = beanClass.getSimpleName();
        String methodName = method.getName();
        return "/netscope/" + beanName + "/" + methodName;
    }
}
