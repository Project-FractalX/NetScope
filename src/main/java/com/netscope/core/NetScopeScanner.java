package com.netscope.core;

import com.netscope.annotation.*;
import com.netscope.mapping.UrlMappingStrategy;
import com.netscope.model.NetworkMethodDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.*;

public class NetScopeScanner {

    private final ApplicationContext context;
    private final UrlMappingStrategy mappingStrategy;

    public NetScopeScanner(ApplicationContext context, UrlMappingStrategy mappingStrategy) {
        this.context = context;
        this.mappingStrategy = mappingStrategy;
    }

    public List<NetworkMethodDefinition> scan() {
        List<NetworkMethodDefinition> list = new ArrayList<>();

        for (String beanName : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(beanName);

            Class<?> targetClass = AopUtils.getTargetClass(bean);
            for (Method m : targetClass.getDeclaredMethods()) {

                if (m.isAnnotationPresent(NetworkPublic.class)) {
                    String path = mappingStrategy.buildPath(bean.getClass(), m);
                    list.add(new NetworkMethodDefinition(bean, m, path, false));
                }

                if (m.isAnnotationPresent(NetworkRestricted.class)) {
                    String path = mappingStrategy.buildPath(bean.getClass(), m);
                    list.add(new NetworkMethodDefinition(bean, m, path, true));
                }
            }
        }
        return list;
    }
}
