package com.netscope.mapping;

import java.lang.reflect.Method;

public interface UrlMappingStrategy {
    String buildPath(Class<?> clazz, Method method);
}
