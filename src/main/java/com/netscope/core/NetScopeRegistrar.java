package com.netscope.core;

import com.netscope.model.NetworkMethodDefinition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class NetScopeRegistrar {

    private final RequestMappingHandlerMapping handlerMapping;
    private final NetScopeInvoker invoker;

    public NetScopeRegistrar(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
        this.invoker = new NetScopeInvoker();
    }

    public void register(List<NetworkMethodDefinition> methods) {
        for (NetworkMethodDefinition def : methods) {
            // pseudo: dynamic controller registration
            // maps URL -> invoker.invoke(bean, method)
        }
    }
}
