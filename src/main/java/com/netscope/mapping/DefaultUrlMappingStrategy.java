package com.netscope.mapping;

import java.lang.reflect.Method;

public class DefaultUrlMappingStrategy implements UrlMappingStrategy {

    @Override
    public String buildPath(Class<?> clazz, Method method) {
        return "/netscope/" +
                clazz.getSimpleName() + "/" +
                method.getName();
    }
}
