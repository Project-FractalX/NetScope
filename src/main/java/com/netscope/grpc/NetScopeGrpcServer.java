package com.netscope.grpc;

import com.netscope.core.NetScopeGrpcConfig;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Manages the gRPC server lifecycle.
 */
@Component
public class NetScopeGrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NetScopeGrpcServer.class);

    private final NetScopeGrpcConfig config;
    private final NetScopeGrpcService grpcService;
    private Server server;

    @Autowired
    public NetScopeGrpcServer(NetScopeGrpcConfig config, NetScopeGrpcService grpcService) {
        this.config = config;
        this.grpcService = grpcService;
    }

    @PostConstruct
    public void start() throws IOException {
        if (!config.isEnabled()) {
            logger.info("NetScope gRPC server is disabled");
            return;
        }

        server = ServerBuilder.forPort(config.getPort())
            .addService(grpcService)
            .addService(ProtoReflectionService.newInstance()) // Enable gRPC reflection
            .maxInboundMessageSize(config.getMaxInboundMessageSize())
            .build()
            .start();

        logger.info("NetScope gRPC server started on port {}", config.getPort());

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down NetScope gRPC server...");
            try {
                NetScopeGrpcServer.this.stop();
            } catch (InterruptedException e) {
                logger.error("Error during shutdown", e);
            }
        }));
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        if (server != null) {
            logger.info("Stopping NetScope gRPC server...");
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            logger.info("NetScope gRPC server stopped");
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
