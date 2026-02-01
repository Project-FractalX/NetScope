package com.netscope.core;

import java.lang.reflect.Method;

public class NetScopeInvoker {

    public Object invoke(Object bean, Method method, Object[] args) throws Exception {
        return method.invoke(bean, args);
    }
}
