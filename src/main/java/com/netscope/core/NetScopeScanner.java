package com.netscope.core;

import com.netscope.annotation.*;
import com.netscope.mapping.UrlMappingStrategy;
import com.netscope.model.NetworkMethodDefinition;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Scans Spring beans for methods annotated with @NetworkPublic or @NetworkRestricted.
 */
public class NetScopeScanner {

    private final ApplicationContext context;
    private final UrlMappingStrategy mappingStrategy;

    public NetScopeScanner(ApplicationContext context, UrlMappingStrategy mappingStrategy) {
        this.context = context;
        this.mappingStrategy = mappingStrategy;
    }

    /**
     * Scan all beans for network-exposed methods.
     */
    public List<NetworkMethodDefinition> scan() {
        List<NetworkMethodDefinition> list = new ArrayList<>();

        for (String beanName : context.getBeanDefinitionNames()) {
            try {
                Object bean = context.getBean(beanName);
                Class<?> targetClass = AopUtils.getTargetClass(bean);

                for (Method method : targetClass.getDeclaredMethods()) {
                    // Check for @NetworkPublic
                    if (method.isAnnotationPresent(NetworkPublic.class)) {
                        NetworkPublic annotation = method.getAnnotation(NetworkPublic.class);
                        String path = annotation.path().isEmpty() 
                            ? mappingStrategy.buildPath(targetClass, method)
                            : annotation.path();
                        
                        list.add(new NetworkMethodDefinition(
                            bean, 
                            method, 
                            path, 
                            false,
                            annotation.enableRest(),
                            annotation.enableGrpc()
                        ));
                    }

                    // Check for @NetworkRestricted
                    if (method.isAnnotationPresent(NetworkRestricted.class)) {
                        NetworkRestricted annotation = method.getAnnotation(NetworkRestricted.class);
                        String path = annotation.path().isEmpty() 
                            ? mappingStrategy.buildPath(targetClass, method)
                            : annotation.path();
                        
                        list.add(new NetworkMethodDefinition(
                            bean, 
                            method, 
                            path, 
                            true,
                            annotation.enableRest(),
                            annotation.enableGrpc()
                        ));
                    }
                }
            } catch (Exception e) {
                // Skip beans that can't be instantiated
            }
        }

        return list;
    }

    /**
     * Get method definition by bean name and method name.
     */
    public Optional<NetworkMethodDefinition> findMethod(List<NetworkMethodDefinition> methods, 
                                                        String beanName, 
                                                        String methodName) {
        return methods.stream()
            .filter(m -> m.getBeanName().equals(beanName) && m.getMethodName().equals(methodName))
            .findFirst();
    }
}
