package com.netscope.core;

import com.netscope.model.NetworkMethodDefinition;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.List;

@RestController
@RequestMapping("/netscope")
public class NetScopeDynamicController {

    private final NetScopeScanner scanner;

    public NetScopeDynamicController(NetScopeScanner scanner) {
        this.scanner = scanner;
    }

    @RequestMapping("/**")
    public Object handle(HttpServletRequest request) throws Exception {
        String fullPath = request.getRequestURI(); // e.g. /netscope/CustomerServiceImpl/getCustomers
        List<NetworkMethodDefinition> methods = scanner.scan();

        for (NetworkMethodDefinition def : methods) {
            if (fullPath.equals(def.getPath())) {
                Object bean = def.getBean();
                Method method = def.getMethod();

                // simple: only support GET with query params for now
                Object[] args = {}; // TODO: parse request params and inject

                return method.invoke(bean, args);
            }
        }

        throw new RuntimeException("No such network API: " + fullPath);
    }
}
