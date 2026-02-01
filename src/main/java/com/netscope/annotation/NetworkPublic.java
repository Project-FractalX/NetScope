package com.netscope.annotation;

import org.springframework.web.bind.annotation.RequestMethod;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NetworkPublic {
    String path() default "";
    RequestMethod method() default RequestMethod.GET;
}
