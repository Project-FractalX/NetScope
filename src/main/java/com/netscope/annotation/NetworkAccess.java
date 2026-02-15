package com.netscope.annotation;

import java.lang.annotation.*;

/**
 * Meta-annotation that marks an annotation as providing network access.
 * Both @NetworkPublic and @NetworkSecured are marked with this.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NetworkAccess {
}
