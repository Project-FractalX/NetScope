package com.netscope.annotation;

import org.springframework.web.bind.annotation.RequestMethod;
import java.lang.annotation.*;

/**
 * Marks a method as publicly accessible over the network (both REST and gRPC).
 * No authentication required.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NetworkPublic {
    /**
     * Custom REST path. Default is /netscope/{BeanName}/{methodName}
     */
    String path() default "";
    
    /**
     * HTTP method for REST endpoint
     */
    RequestMethod method() default RequestMethod.GET;
    
    /**
     * Enable/disable REST endpoint (default: true)
     */
    boolean enableRest() default true;
    
    /**
     * Enable/disable gRPC endpoint (default: true)
     */
    boolean enableGrpc() default true;
}
