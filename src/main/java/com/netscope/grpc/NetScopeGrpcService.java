package com.netscope.grpc;

import com.netscope.core.NetScopeInvoker;
import com.netscope.core.NetScopeScanner;
import com.netscope.grpc.proto.*;
import com.netscope.model.NetworkMethodDefinition;
import com.netscope.security.ServiceAuthValidator;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * gRPC service implementation for NetScope.
 * Exposes all @NetworkPublic and @NetworkRestricted methods via gRPC.
 */
public class NetScopeGrpcService extends NetScopeServiceGrpc.NetScopeServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(NetScopeGrpcService.class);

    private final NetScopeScanner scanner;
    private final NetScopeInvoker invoker;
    private final ServiceAuthValidator authValidator;
    private final List<NetworkMethodDefinition> exposedMethods;

    public NetScopeGrpcService(NetScopeScanner scanner, 
                               NetScopeInvoker invoker,
                               ServiceAuthValidator authValidator) {
        this.scanner = scanner;
        this.invoker = invoker;
        this.authValidator = authValidator;
        this.exposedMethods = scanner.scan();
        
        logger.info("NetScope gRPC service initialized with {} exposed methods", exposedMethods.size());
    }

    @Override
    public void invokeMethod(GenericRequest request, StreamObserver<GenericResponse> responseObserver) {
        try {
            logger.debug("gRPC InvokeMethod called: {}.{}", request.getBeanName(), request.getMethodName());

            // Find the method
            Optional<NetworkMethodDefinition> methodOpt = scanner.findMethod(
                exposedMethods, 
                request.getBeanName(), 
                request.getMethodName()
            );

            if (methodOpt.isEmpty()) {
                sendError(responseObserver, 404, 
                    "Method not found: " + request.getBeanName() + "." + request.getMethodName());
                return;
            }

            NetworkMethodDefinition method = methodOpt.get();

            // Check if gRPC is enabled for this method
            if (!method.isGrpcEnabled()) {
                sendError(responseObserver, 403, 
                    "gRPC access not enabled for this method");
                return;
            }

            // Validate authentication
            if (!authValidator.validate(method, request.getApiKey())) {
                sendError(responseObserver, 401, 
                    "Authentication failed. " + authValidator.getExpectedKeyHint(method));
                return;
            }

            // Invoke the method
            Object result = invoker.invoke(method, request.getArgumentsJson());
            String resultJson = invoker.serializeResult(result);

            // Send success response
            GenericResponse response = GenericResponse.newBuilder()
                .setResultJson(resultJson)
                .setSuccess(true)
                .setStatusCode(200)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.debug("Successfully invoked {}.{}", request.getBeanName(), request.getMethodName());

        } catch (Exception e) {
            logger.error("Error invoking method: {}.{}", request.getBeanName(), request.getMethodName(), e);
            sendError(responseObserver, 500, "Invocation error: " + e.getMessage());
        }
    }

    @Override
    public void getDocs(DocsRequest request, StreamObserver<DocsResponse> responseObserver) {
        try {
            logger.debug("gRPC GetDocs called");

            DocsResponse.Builder responseBuilder = DocsResponse.newBuilder();

            for (NetworkMethodDefinition method : exposedMethods) {
                // Only include methods that have gRPC enabled
                if (!method.isGrpcEnabled()) {
                    continue;
                }

                MethodInfo.Builder methodInfoBuilder = MethodInfo.newBuilder()
                    .setBeanName(method.getBeanName())
                    .setMethodName(method.getMethodName())
                    .setPath(method.getPath())
                    .setRestricted(method.isRestricted())
                    .setReturnType(method.getReturnType());

                // Add parameter information
                for (NetworkMethodDefinition.ParameterInfo param : method.getParameters()) {
                    ParameterInfo paramInfo = ParameterInfo.newBuilder()
                        .setName(param.getName())
                        .setType(param.getType())
                        .setIndex(param.getIndex())
                        .build();
                    methodInfoBuilder.addParameters(paramInfo);
                }

                responseBuilder.addMethods(methodInfoBuilder.build());
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

            logger.debug("Returned documentation for {} gRPC methods", responseBuilder.getMethodsCount());

        } catch (Exception e) {
            logger.error("Error getting documentation", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public StreamObserver<GenericRequest> invokeMethodStream(StreamObserver<GenericResponse> responseObserver) {
        return new StreamObserver<GenericRequest>() {
            @Override
            public void onNext(GenericRequest request) {
                try {
                    // Find and invoke the method
                    Optional<NetworkMethodDefinition> methodOpt = scanner.findMethod(
                        exposedMethods, 
                        request.getBeanName(), 
                        request.getMethodName()
                    );

                    if (methodOpt.isEmpty()) {
                        sendStreamError(responseObserver, 404, 
                            "Method not found: " + request.getBeanName() + "." + request.getMethodName());
                        return;
                    }

                    NetworkMethodDefinition method = methodOpt.get();

                    // Check if gRPC is enabled
                    if (!method.isGrpcEnabled()) {
                        sendStreamError(responseObserver, 403, 
                            "gRPC access not enabled for this method");
                        return;
                    }

                    // Validate authentication
                    if (!authValidator.validate(method, request.getApiKey())) {
                        sendStreamError(responseObserver, 401, 
                            "Authentication failed. " + authValidator.getExpectedKeyHint(method));
                        return;
                    }

                    // Invoke and send result
                    Object result = invoker.invoke(method, request.getArgumentsJson());
                    String resultJson = invoker.serializeResult(result);

                    GenericResponse response = GenericResponse.newBuilder()
                        .setResultJson(resultJson)
                        .setSuccess(true)
                        .setStatusCode(200)
                        .build();

                    responseObserver.onNext(response);

                } catch (Exception e) {
                    logger.error("Error in streaming invocation", e);
                    sendStreamError(responseObserver, 500, "Invocation error: " + e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Stream error from client", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private void sendError(StreamObserver<GenericResponse> observer, int statusCode, String message) {
        GenericResponse response = GenericResponse.newBuilder()
            .setSuccess(false)
            .setStatusCode(statusCode)
            .setErrorMessage(message)
            .build();
        observer.onNext(response);
        observer.onCompleted();
    }

    private void sendStreamError(StreamObserver<GenericResponse> observer, int statusCode, String message) {
        GenericResponse response = GenericResponse.newBuilder()
            .setSuccess(false)
            .setStatusCode(statusCode)
            .setErrorMessage(message)
            .build();
        observer.onNext(response);
    }
}
