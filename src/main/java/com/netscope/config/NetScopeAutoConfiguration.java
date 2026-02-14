package com.netscope.config;

import com.netscope.core.*;
import com.netscope.grpc.NetScopeGrpcServer;
import com.netscope.grpc.NetScopeGrpcService;
import com.netscope.mapping.*;
import com.netscope.security.ServiceAuthValidator;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Auto-configuration for NetScope.
 * Automatically sets up both REST and gRPC endpoints.
 */
@Configuration
public class NetScopeAutoConfiguration {

    private final ApplicationContext context;

    public NetScopeAutoConfiguration(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public UrlMappingStrategy urlMappingStrategy() {
        return new DefaultUrlMappingStrategy();
    }

    @Bean
    public NetScopeScanner netScopeScanner(ApplicationContext context, 
                                           UrlMappingStrategy urlMappingStrategy) {
        return new NetScopeScanner(context, urlMappingStrategy);
    }

    @Bean
    public NetScopeInvoker netScopeInvoker() {
        return new NetScopeInvoker();
    }

    @Bean
    public ServiceAuthValidator serviceAuthValidator(NetScopeSecurityConfig securityConfig) {
        return new ServiceAuthValidator(securityConfig);
    }

    @Bean
    @Lazy
    public NetScopeDocController netScopeDocController(NetScopeScanner scanner) {
        return new NetScopeDocController(scanner);
    }

    @Bean
    @Lazy
    public NetScopeDynamicController netScopeDynamicController(
            NetScopeScanner scanner,
            NetScopeSecurityConfig securityConfig) {
        return new NetScopeDynamicController(scanner, securityConfig);
    }

    @Bean
    @ConditionalOnProperty(name = "netscope.grpc.enabled", havingValue = "true", matchIfMissing = true)
    public NetScopeGrpcService netScopeGrpcService(
            NetScopeScanner scanner,
            NetScopeInvoker invoker,
            ServiceAuthValidator authValidator) {
        return new NetScopeGrpcService(scanner, invoker, authValidator);
    }

    @Bean
    @ConditionalOnProperty(name = "netscope.grpc.enabled", havingValue = "true", matchIfMissing = true)
    public NetScopeGrpcServer netScopeGrpcServer(
            NetScopeGrpcConfig config,
            NetScopeGrpcService grpcService) {
        return new NetScopeGrpcServer(config, grpcService);
    }

    @Bean
    public SmartInitializingSingleton netScopeRegistrar(NetScopeScanner scanner) {
        return () -> {
            var methods = scanner.scan();
            RequestMappingHandlerMapping mapping = 
                (RequestMappingHandlerMapping) context.getBean("requestMappingHandlerMapping");
            NetScopeRegistrar registrar = new NetScopeRegistrar(mapping);
            registrar.register(methods);
        };
    }
}
