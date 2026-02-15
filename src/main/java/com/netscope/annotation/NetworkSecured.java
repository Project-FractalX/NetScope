package com.netscope.annotation;

import org.springframework.web.bind.annotation.RequestMethod;
import java.lang.annotation.*;

/**
 * Marks a method as restricted - requires API key authentication.
 * Works for both REST and gRPC endpoints.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NetworkSecured {
    /**
     * HTTP method for REST endpoint
     */
    RequestMethod method() default RequestMethod.GET;

    /**
     * Optional per-method API key. If not specified, uses global API key from configuration.
     */
    String key() default "";
    
    /**
     * Custom REST path. Default is /netscope/{BeanName}/{methodName}
     */
    String path() default "";
    
    /**
     * Enable/disable REST endpoint (default: true)
     */
    boolean enableRest() default true;
    
    /**
     * Enable/disable gRPC endpoint (default: true)
     */
    boolean enableGrpc() default true;
}
