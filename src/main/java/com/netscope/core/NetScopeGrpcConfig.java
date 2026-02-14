package com.netscope.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for NetScope gRPC server.
 */
@Configuration
@ConfigurationProperties(prefix = "netscope.grpc")
public class NetScopeGrpcConfig {

    private boolean enabled = true;
    private int port = 9090;
    private int maxInboundMessageSize = 4194304; // 4MB default

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
    }
}
